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

import com.nobullshit.sensor.SensorReaderListener;

public class RecorderApplication extends Application {
	
	private List<SensorReaderListener> listeners;
	private volatile boolean locked;
	private volatile IRecorderService service;
	private volatile boolean recording;
	
	@Override
	public void onCreate() {
		super.onCreate();
		listeners = new ArrayList<SensorReaderListener>(4);
		locked = false;
		
		LockStateReceiver receiver = new LockStateReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_USER_PRESENT);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		registerReceiver(receiver, filter);
		
		SharedPreferences prefs = getSharedPreferences(RecorderService.PREFERENCES, MODE_PRIVATE);
		String output = prefs.getString(RecorderService.PREF_OUTPUT_FILE, null);
		if(output != null) {
			startRecording();
		}
		else recording = false;
	}
	
	public void addListener(SensorReaderListener e) {
		listeners.add(e);
	}
	
	public void removeListener(SensorReaderListener e) {
		listeners.remove(e);
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
			else if(i.getAction().equals(Intent.ACTION_SCREEN_OFF)) locked = true;
		}
		
	}
	
	private class RecorderConnection implements ServiceConnection {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			RecorderApplication.this.service = IRecorderService.Stub.asInterface(service);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			RecorderApplication.this.service = null;
		}
		
	}

}
