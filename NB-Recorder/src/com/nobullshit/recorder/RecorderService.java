package com.nobullshit.recorder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.SensorEvent;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.RemoteException;
import android.util.Log;

import com.nobullshit.binaryio.BinaryWriter;
import com.nobullshit.sensor.AccelerationReader;
import com.nobullshit.sensor.LocationReader;
import com.nobullshit.sensor.SensorReader;
import com.nobullshit.sensor.SensorReaderListener;

public class RecorderService extends Service implements SensorReaderListener {

	public static final String RECORDING_DIRECTORY = "recordings";
	public static final String OUTGOING_DIRECTORY = "outgoing";
	public static final String RETURN_VALUE = "RETURN_VALUE";
	public static final String ARGUMENT_1 = "ARGUMENT_1";
	public static final int CALL_START_RECORDING = 1;
	public static final int CALL_STOP_RECORDING = 2;
	public static final int CALL_TOGGLE_RECORDING = 3;
	public static final int CALL_GET_RUNTIME = 4;
	public static final int CALL_GET_SENSOR_AVAILABLE = 5;
	public static final int CALL_GET_SENSOR_ENABLED = 6;
	public static final int CALL_GET_SENSOR_READING = 7;
	public static final int CALL_IS_RECORDING = 8;
	public static final int INDEX_ACCELERATION = 1;
	public static final int INDEX_LOCATION = 2;
	
	private static final String PREFERENCES = "RECORDER_SERVICE_PREFERENCES";
	private static final String PREF_RECORDING = "PREF_RECORDING";
	private static final String PREF_OUTPUT_FILE = "PREF_OUTPUT_FILE";
	private static final String TAG = "RecorderService";
	private static final int NOTIFICATION_ID = 1;
	
	private BinaryWriter writer;
	private AccelerationReader accreader;
	private LocationReader locreader;
	private List<SensorReaderListener> listeners;
	private boolean recording;
	private PowerManager.WakeLock lock;
	private long start;
	private final IRecorderService.Stub binder = new RecorderBinder();
	private RecorderRunnable run;
	
	@Override
	public void onCreate() {
		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
		lock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "RecorderApplication");
		listeners = new ArrayList<SensorReaderListener>();
		run = new RecorderRunnable();
		new Thread(run).start();
		
		// TODO make service thread-safe
	}
	
	@Override
	public void onDestroy() {
		
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		SharedPreferences prefs = getApplication().getApplicationContext().getSharedPreferences(
				PREFERENCES, MODE_PRIVATE);
		
		if(intent == null) {
			recording = prefs.getBoolean(PREF_RECORDING, false);
			String name = prefs.getString(PREF_OUTPUT_FILE, null);
			if(name != null && recording) {
				File out = new File(name);
				startRecording(out);
			}
		}
		
		return START_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}
	
	@Override
	public boolean onUnbind (Intent intent) {
		if(!isRecording()) stopSelf();
		return false;
	}
	
	private synchronized void toggleRecording() {
		if(!recording) {
			startRecording(null);
			Log.v(TAG,"recording started");
		}
		else {
			stopRecording();
			Log.v(TAG,"recording stopped");
		}
	}

	private synchronized void startRecording(File out) {
		lock.acquire();
		
		showNotification();
		
		long now = System.currentTimeMillis();

		if(out == null) {
			File dir = new File(getExternalFilesDir(null),RECORDING_DIRECTORY);
			if(!dir.isDirectory()) dir.mkdir();
			out = new File(dir, "rec_"+now);
		}

		try {
			writer = new BinaryWriter(out,
					new String[] {"time","x","y","z"},
					new String[] {"long","float","float","float"},
					new String[] {"time","lat","long","alt","acc"},
					new String[] {"long","double","double","double","float"});

			if(accreader == null) {
				accreader = new AccelerationReader(this, 100);
				accreader.registerListener(this);
			}
			if(locreader == null) {
				locreader = new LocationReader(this, 1000);
				locreader.registerListener(this);
			}
			accreader.start();
			locreader.start();
			start = System.currentTimeMillis();
			recording = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private synchronized void stopRecording() {
		recording = false;
		lock.release();
		
		hideNotification();
		
		if(accreader != null) {
			accreader.stop();
		}
		if(locreader != null) {
			locreader.stop();
		}
		synchronized(this) {
			if(writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					// TODO broadcast error
					e.printStackTrace();
				}
				writer = null;
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	private void showNotification(){
		NotificationManager m =
				(NotificationManager) getSystemService(NOTIFICATION_SERVICE);	
		
	    Notification not = new Notification(
	    		R.drawable.ic_launcher, "Aufnahme", System.currentTimeMillis());
	    Intent startingIntent = new Intent(Intent.ACTION_MAIN);
	    PendingIntent contentIntent = PendingIntent.getActivity(
	    		this, 0, startingIntent, Notification.FLAG_ONGOING_EVENT);        
	    not.flags = Notification.FLAG_ONGOING_EVENT;
	    not.setLatestEventInfo(this, "Application Name", "Application Description", contentIntent);
	    m.notify(NOTIFICATION_ID, not);
	}
	
	private void hideNotification(){
		NotificationManager m =
				(NotificationManager) getSystemService(NOTIFICATION_SERVICE);	
	    m.cancel(NOTIFICATION_ID);
	}
	
	private boolean isRecording() {
		return recording;
	}
	
	private long getRuntime() {
		return recording ? System.currentTimeMillis() - start : 0;
	}


	private boolean getSensorAvailable(int sensor) {
		switch(sensor) {
		case SensorReader.TYPE_ACCELEROMETER:
			return accreader.isAvailable();
		case SensorReader.TYPE_FINE_LOCATION:
			return locreader.isAvailable();
		default:
			return false;
		}
	}

	private boolean getSensorEnabled(int sensor) {
		switch(sensor) {
		case SensorReader.TYPE_ACCELEROMETER:
			return accreader != null && accreader.isEnabled();
		case SensorReader.TYPE_FINE_LOCATION:
			return locreader != null && locreader.isEnabled();
		default: return false;
		}
	}

	private boolean getSensorReading(int sensor) {
		switch(sensor) {
		case SensorReader.TYPE_ACCELEROMETER:
			return accreader != null && accreader.isReading();
		case SensorReader.TYPE_FINE_LOCATION:
			return locreader != null && locreader.isReading();
		default:
			return false;
		}
	}

	@Override
	public void onSensorStateChanged(int sensor, int state) {
		Log.v(TAG, "sensor " + sensor + " now in state " + state);
		if(recording) for(SensorReaderListener l: listeners)
				l.onSensorStateChanged(sensor, state);
	}

	@Override
	public void onSensorReading(int sensor, Object reading) {
		switch(sensor) {
		case SensorReader.TYPE_ACCELEROMETER:
			onAcceleration((SensorEvent) reading);
			break;
		case SensorReader.TYPE_FINE_LOCATION:
			onLocation((Location) reading);
			break;
		}
		if(recording) for(SensorReaderListener l: listeners)
				l.onSensorReading(sensor,reading);
	}

	private void onAcceleration(SensorEvent event) {
		if(recording) {
			try {
				synchronized (this) {
					if(writer != null) {
						writer.writeByte(INDEX_ACCELERATION);
						writer.writeLong(System.currentTimeMillis());
						writer.writeFloat(event.values[0]);
						writer.writeFloat(event.values[1]);
						writer.writeFloat(event.values[2]);
					}
				}
			} catch (IOException e) {
				// TODO broadcast error
				e.printStackTrace();
			}
		}
	}

	private void onLocation(Location location) {
		// time, lat, long, alt, acc
		if(recording) {
			try {
				synchronized (this) {
					if(writer != null) {
						writer.writeByte(INDEX_LOCATION);
						writer.writeLong(location.getTime());
						writer.writeDouble(location.getLatitude());
						writer.writeDouble(location.getLongitude());
						writer.writeDouble(location.getAltitude());
						writer.writeFloat(location.getAccuracy());
					}
				}
			} catch (IOException e) {
				// TODO broadcast error
				e.printStackTrace();
			}
		}
	}
	
	private class RecorderBinder extends IRecorderService.Stub {
		
		
		public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
		    try {
		        return super.onTransact(code, data, reply, flags);
		    } catch (RuntimeException e) {
		        e.printStackTrace();
		        throw e;
		    }
		}

		@Override
		public void startRecording() throws RemoteException {
			Message msg = Message.obtain();
			msg.what = CALL_START_RECORDING;
			run.handler.sendMessage(msg);
		}

		@Override
		public void stopRecording() throws RemoteException {
			Message msg = Message.obtain();
			msg.what = CALL_STOP_RECORDING;
			run.handler.sendMessage(msg);
		}

		@Override
		public void toggleRecording() throws RemoteException {
			Message msg = Message.obtain();
			msg.what = CALL_TOGGLE_RECORDING;
			run.handler.sendMessage(msg);
		}

		@Override
		public long getRuntime() throws RemoteException {
			return RecorderService.this.getRuntime();
		}

		@Override
		public boolean isRecording() throws RemoteException {
			return RecorderService.this.isRecording();
		}

		@Override
		public boolean getSensorAvailable(int sensor) throws RemoteException {
			return RecorderService.this.getSensorAvailable(sensor);
		}

		@Override
		public boolean getSensorEnabled(int sensor) throws RemoteException {
			return RecorderService.this.getSensorEnabled(sensor);
		}

		@Override
		public boolean getSensorReading(int sensor) throws RemoteException {
			return RecorderService.this.getSensorReading(sensor);
		}
		
	}
	
	private class RecorderRunnable implements Runnable {
		
		public RecorderHandler handler;

		@Override
		public void run() {
			Looper.prepare();
			
			handler = new RecorderHandler();
			
			Looper.loop();
		}
		
	}
	
	private class RecorderHandler extends Handler {
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case CALL_START_RECORDING:
				startRecording(null);
				break;
			case CALL_STOP_RECORDING:
				stopRecording();
				break;
			case CALL_TOGGLE_RECORDING:
				toggleRecording();
				break;
			}
			msg.recycle();
		}
	}

}
