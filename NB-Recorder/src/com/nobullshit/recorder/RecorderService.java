package com.nobullshit.recorder;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.util.LinkedList;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.SensorEvent;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.nobullshit.binaryio.BinaryWriter;
import com.nobullshit.sensor.AccelerationReader;
import com.nobullshit.sensor.ISensorReaderCallback;
import com.nobullshit.sensor.LocationReader;
import com.nobullshit.sensor.Reading;
import com.nobullshit.sensor.SensorReader;
import com.nobullshit.sensor.SensorReaderListener;

public class RecorderService extends Service implements SensorReaderListener {

	public static final String RECORDING_DIRECTORY = "recordings";
	public static final String OUTGOING_DIRECTORY = "outgoing";
	public static final int CALL_START_RECORDING = 1;
	public static final int CALL_STOP_RECORDING = 2;
	public static final int CALL_TOGGLE_RECORDING = 3;
	public static final byte INDEX_ACCELERATION = 1;
	public static final byte INDEX_LOCATION = 2;
	public static final String PREFERENCES = "RECORDER_SERVICE_PREFERENCES";
	public static final String PREF_OUTPUT_FILE = "PREF_OUTPUT_FILE";
	public static final String OUTPUT_FILE_PREFIX = "rec_";
	
	private static final String TAG = "RecorderService";
	private static final String SENSOR_THREAD_NAME = "RecorderSensorThread";
	private static final int NOTIFICATION_ID = 1;
	
	private BinaryWriter writer;
	private AccelerationReader accreader;
	private LocationReader locreader;
	private volatile LinkedList<ISensorReaderCallback> listeners;
	private volatile boolean recording;
	private PowerManager.WakeLock lock;
	private long start;
	private final IRecorderService.Stub binder = new RecorderBinder();
	private volatile RecorderRunnable run;
	
	@Override
	public void onCreate() {
		recording = false;
		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
		lock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "RecorderApplication");
		listeners = new LinkedList<ISensorReaderCallback>();
	}
	
	@Override
	public void onDestroy() {
		if(lock != null && lock.isHeld()) lock.release();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(!recording) {
			run = new RecorderRunnable();
			new Thread(run, SENSOR_THREAD_NAME).start();
			// wait for handler to be created
			int i=0;
			do {
				try { synchronized(run) {
					run.wait(10);
					i++;
				} } catch (InterruptedException e) {}
			} while(i<100 && run.handler == null);

			// check if handler is available, else stop service
			if(run.handler != null) {
				run.handler.sendEmptyMessage(CALL_START_RECORDING);
				startForeground(NOTIFICATION_ID, makeNotification());
			}
			else {
				Log.e(TAG, "recording handler was not created in time");
				stopSelf();
			}
		}
		
		return START_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}
	
	private synchronized void toggleRecording() {
		if(!recording) {
			startRecording();
		}
		else {
			stopRecording();
		}
	}

	private synchronized void startRecording() {
		Log.v(TAG, "recording started");
		
		lock.acquire();
		
		SharedPreferences prefs = getApplication().getSharedPreferences(PREFERENCES, MODE_PRIVATE);
		String name = OUTPUT_FILE_PREFIX + System.currentTimeMillis();
		name = prefs.getString(PREF_OUTPUT_FILE, name);
		Editor editor = prefs.edit();
		editor.putString(PREF_OUTPUT_FILE, name);
		editor.commit();
		File dir = new File(getExternalFilesDir(null),RECORDING_DIRECTORY);
		if(!dir.isDirectory()) dir.mkdir();
		File out = new File(dir, name);

		try {
			writer = new BinaryWriter(out,
					new String[] {"acceleration","location"},
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
			broadcastError(e);
			e.printStackTrace();
		}
	}
	
	private synchronized void stopRecording() {
		if(recording) {
			recording = false;
			
			// remove the output file hint
			SharedPreferences prefs = getApplication().getSharedPreferences(PREFERENCES, MODE_PRIVATE);
			Editor editor = prefs.edit();
			editor.remove(PREF_OUTPUT_FILE);
			editor.commit();
			
			if(run.handler != null) {
				run.handler.removeCallbacks(run);
				run.handler.getLooper().quit();
			}
			
			if(accreader != null) {
				accreader.stop();
				accreader = null;
			}
			if(locreader != null) {
				locreader.stop();
				accreader = null;
			}
			synchronized(this) {
				if(writer != null) {
					try {
						writer.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					writer = null;
				}
			}
			
			Log.v(TAG, "recording stopped");

			
			// release the resources
			if(listeners != null) listeners.clear();
			if(lock != null && lock.isHeld()) lock.release();
		}
		
		stopForeground(true);
		stopSelf();
	}
	
	private Notification makeNotification(){
	    Intent startingIntent = new Intent(Intent.ACTION_MAIN);
	    startingIntent.setClass(getApplicationContext(), MainActivity.class);
	    PendingIntent contentIntent = PendingIntent.getActivity(
	    		this, 0, startingIntent, Notification.FLAG_ONGOING_EVENT);
		
	    Notification not = new NotificationCompat.Builder(this)
	    	.setContentTitle("NB-Recorder")
			.setContentText("Aufnahme l�uft")
			.setSmallIcon(R.drawable.ic_launcher)
			.setContentIntent(contentIntent)
			.build();
	    not.flags = Notification.FLAG_ONGOING_EVENT;
	    
	    return not;
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
	
	private void broadcastError(Exception e) {
		if(listeners != null) {
			StringWriter w = new StringWriter();
			e.printStackTrace(new PrintWriter(w));
			String msg = e.getMessage();
			if(msg != null) msg += "\n" + w.toString();
			else msg = w.toString();
			
			for(ISensorReaderCallback l: listeners) {
				try { l.onError(0, msg); }
				catch (RemoteException e1) { e1.printStackTrace(); }
			}
		}
	}

	@Override
	public void onSensorStateChanged(int sensor, int state) {
		Log.v(TAG, "sensor " + sensor + " now in state " + state);
		
		if(recording && listeners != null) {
			for(ISensorReaderCallback l: listeners) {
				try { 
					l.onSensorStateChanged(sensor, state);
				} catch (RemoteException e) {
					e.printStackTrace();
					try { binder.unregisterCallback(l); }
					catch (RemoteException e1) { e1.printStackTrace(); }
				}
			}
		}
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
		
		if(recording && listeners != null) {
			Reading r = new Reading(sensor, reading);
			for(ISensorReaderCallback l: listeners) {
				try {
					l.onSensorReading(r);
				} catch (RemoteException e) {
					e.printStackTrace();
					try {
						binder.unregisterCallback(l);
					} catch (RemoteException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
	}

	private void onAcceleration(SensorEvent event) {
		if(recording) {
			try {
				synchronized (this) {
					if(writer != null) {
						writer.startEntry(INDEX_ACCELERATION);
						writer.writeLong(System.currentTimeMillis());
						writer.writeFloat(event.values[0]);
						writer.writeFloat(event.values[1]);
						writer.writeFloat(event.values[2]);
						writer.endEntry();
					}
				}
			} catch (IOException e) {
				broadcastError(e);
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
						writer.startEntry(INDEX_LOCATION);
						writer.writeLong(location.getTime());
						writer.writeDouble(location.getLatitude());
						writer.writeDouble(location.getLongitude());
						writer.writeDouble(location.getAltitude());
						writer.writeFloat(location.getAccuracy());
						writer.endEntry();
					}
				}
			} catch (IOException e) {
				broadcastError(e);
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
		public void stopRecording() throws RemoteException {
			//RecorderService.this.stopRecording();
			run.handler.sendEmptyMessage(CALL_STOP_RECORDING);
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

		@Override
		public void registerCallback(ISensorReaderCallback callback)
				throws RemoteException {
			if(listeners != null) {
				Log.v(TAG,"register callback #" + callback.getId());
				for(ISensorReaderCallback l: listeners) {
					try { if(l.getId() == callback.getId()) return; }
					catch(RemoteException e) { unregisterCallback(callback); }
				}
				listeners.add(callback);
			}
		}

		@Override
		public boolean unregisterCallback(ISensorReaderCallback callback)
				throws RemoteException {
			if(listeners != null) {
				Log.v(TAG,"unregister callback");
				if(listeners.remove(callback)) {
					return true;
				}
				else {
					for(ISensorReaderCallback l: listeners) {
						try {
							if(l.getId() == callback.getId()) {
								listeners.remove(l);
								return true;
							}
						} catch(RemoteException e) {
							listeners.remove(l);
						}
					}
				}
			}
			return false;
		}
		
	}
	
	private class RecorderRunnable implements Runnable {
		
		public RecorderHandler handler;

		@Override
		public void run() {
			Looper.prepare();
			
			handler = new RecorderHandler(RecorderService.this);
			
			synchronized(this) {
				this.notifyAll();
			}
			
			Looper.loop();
		}
		
	}
	
	private static class RecorderHandler extends Handler {
		
		private WeakReference<RecorderService> service;
		
		public RecorderHandler(RecorderService service) {
			this.service = new WeakReference<RecorderService>(service);
		}
		
		public void handleMessage(Message msg) {
			RecorderService service = this.service.get();
			if(service == null) return;
			
			switch(msg.what) {
			case CALL_START_RECORDING:
				service.startRecording();
				break;
			case CALL_STOP_RECORDING:
				service.stopRecording();
				break;
			case CALL_TOGGLE_RECORDING:
				service.toggleRecording();
				break;
			}
		}
	}

}
