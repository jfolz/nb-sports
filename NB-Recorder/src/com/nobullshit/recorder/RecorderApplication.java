package com.nobullshit.recorder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.nobullshit.sensor.ISensorReaderCallback;
import com.nobullshit.sensor.Reading;
import com.nobullshit.sensor.SensorReaderListener;

public class RecorderApplication extends Application {
	
	private static final String TAG = "RecorderApplication";
	
	private List<SensorReaderListener> listeners;
	private boolean locked;
	private volatile IRecorderService service;
	private volatile boolean recording;
	private RecorderCallback callback;
	
	@Override
	public void onCreate() {
		super.onCreate();
		listeners = new ArrayList<SensorReaderListener>(4);
		locked = false;
		
		LockStateReceiver receiver = new LockStateReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_USER_PRESENT);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_SCREEN_ON);
		registerReceiver(receiver, filter);
		
		SharedPreferences prefs = getSharedPreferences(RecorderService.PREFERENCES, MODE_PRIVATE);
		String output = prefs.getString(RecorderService.PREF_OUTPUT_FILE, null);
		if(output != null) {
			startRecording();
		}
		else recording = false;
		
		callback = new RecorderCallback();
	}
	
	public void addListener(SensorReaderListener l) {
		if(!listeners.contains(l)) {
			listeners.add(l);
		}
	}
	
	public void removeListener(SensorReaderListener l) {
		listeners.remove(l);
	}
	
	private void registerCallback() {
		if(recording && service != null) {
			Log.v(TAG,"register sensor callback");
			try { service.registerCallback(callback); }
			catch (RemoteException e) { e.printStackTrace(); }
		}
	}
	
	private void unregisterCallback() {
		if(listeners.size() == 0 && service != null) {
			Log.v(TAG,"unregister sensor callback");
			try { service.unregisterCallback(callback); }
			catch (RemoteException e) { e.printStackTrace(); }
		}
	}
	
	public File[] getFinishedRecordings() {
		File dir = getRecordingDirectory();
		if(dir.isDirectory()) {
			File[] all = dir.listFiles();
			if(!recording && all.length > 0) {
				return all;
			}
			else if(all.length > 1) {
				File[] finished = new File[all.length-1];
				for(int i=0; i<finished.length; i++) finished[i] = all[i];
				return finished;
			}
		}
		return null;
	}
	
	public void toggleRecording() throws RemoteException {
		if(recording) stopRecording();
		else startRecording();
	}

	/**
	 * Will attempt to start the {@link RecorderService} and bind to it.
	 * If the service is already running, it will only be bound.
	 * @return true if service was started successfully
	 */
	public boolean startRecording() {
		Intent intent = new Intent(this, RecorderService.class);
		ServiceConnection conn = new RecorderConnection();
		startService(intent);
		boolean started = bindService(intent, conn, BIND_AUTO_CREATE);
		recording = started;
		return started;
	}
	
	public void stopRecording() {
		recording = false;
		if(service != null) {
			try {
				service.stopRecording();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public boolean isRecording() {
		return recording;
	}
	
	public boolean isLocked() {
		return locked;
	}
	
	public double getRuntime() {
		if(service != null) {
			try {
				return service.getRuntime();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return 0;
	}
	
	public File getRecordingDirectory() {
		return new File(getExternalFilesDir(null), RecorderService.RECORDING_DIRECTORY);
	}
	
	public File getOutgoingDirectory() {
		return new File(getExternalFilesDir(null), RecorderService.OUTGOING_DIRECTORY);
	}

	public boolean getSensorAvailable(int sensor) {
		if(service != null) {
			try {
				return service.getSensorAvailable(sensor);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}

	public boolean getSensorEnabled(int sensor) {
		if(service != null) {
			try {
				return service.getSensorEnabled(sensor);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}

	public boolean getSensorReading(int sensor) {
		if(service != null) {
			try {
				return service.getSensorReading(sensor);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}
	
	private class LockStateReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context c, Intent i) {
			if(i.getAction().equals(Intent.ACTION_USER_PRESENT)) locked = false;
			else if(i.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
				unregisterCallback();
				locked = true;
			}
			else if(i.getAction().equals(Intent.ACTION_SCREEN_ON)) {
				registerCallback();
			}
		}
		
	}
	
	private class RecorderConnection implements ServiceConnection {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			IRecorderService s = IRecorderService.Stub.asInterface(service);
			RecorderApplication.this.service = s;
			try {
				s.registerCallback(callback);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			RecorderApplication.this.service = null;
		}
		
	}
	
	private class RecorderCallback extends ISensorReaderCallback.Stub {

		@Override
		public void onSensorStateChanged(int sensor, int state)
				throws RemoteException {
			for(SensorReaderListener l: listeners) {
				l.onSensorStateChanged(sensor, state);
			}
		}

		@Override
		public void onSensorReading(Reading reading) throws RemoteException {
			for(SensorReaderListener l: listeners) {
				l.onSensorReading(reading.sensor, reading);
			}
		}

		@Override
		public int getId() throws RemoteException {
			return 0;
		}
		
	}

}
