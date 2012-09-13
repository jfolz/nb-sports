package com.nobullshit.recorder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;

import com.nobullshit.binaryio.BinaryWriter;
import com.nobullshit.sensor.AccelerationReader;
import com.nobullshit.sensor.LocationReader;

public class RecorderApplication extends Application implements SensorEventListener, LocationListener {
	
	public static final String RECORDING_DIRECTORY = "recordings";
	public static final String OUTGOING_DIRECTORY = "outgoing";
	public static final int INDEX_ACCELERATION = 1;
	public static final int INDEX_LOCATION = 2;
	
	private BinaryWriter writer;
	private AccelerationReader accreader;
	private LocationReader locreader;
	private List<ListenerListener> listeners;
	private Exception e;
	private boolean recording;
	private PowerManager.WakeLock lock;
	private long start;
	private int count;
	private boolean locked;
	
	@Override
	public void onCreate() {
		super.onCreate();
		listeners = new ArrayList<ListenerListener>(4);
		recording = false;
		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
		lock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "RecorderApplication");
		locked = false;
		
		LockStateReceiver receiver = new LockStateReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_USER_PRESENT);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		registerReceiver(receiver, filter);
	}
	
	public void addListener(ListenerListener e) {
		listeners.add(e);
	}
	
	public void removeListener(ListenerListener e) {
		listeners.remove(e);
	}
	
	public void toggleRecording() throws IOException {
		if(!recording) {
			startRecording();
			Log.v("RecorderApplication","recording started");
		}
		else {
			stopRecording();
			Log.v("RecorderApplication","recording stopped");
		}
	}

	public void startRecording() throws IOException {
		lock.acquire();
		count = 0;
		long now = System.currentTimeMillis();
		
		File dir = new File(getExternalFilesDir(null),RECORDING_DIRECTORY);
		if(!dir.isDirectory()) dir.mkdir();
		
		File out = new File(dir, "rec_"+now);
		writer = new BinaryWriter(out,
				new String[] {"time","x","y","z"},
				new String[] {"long","float","float","float"},
				new String[] {"time","lat","long","alt","acc"},
				new String[] {"long","double","double","double","float"});
		
		if(accreader == null) accreader = new AccelerationReader(this, this, 100);
		if(locreader == null) locreader = new LocationReader(this, this, 1000);
		accreader.start();
		locreader.start();
		start = System.currentTimeMillis();
		recording = true;
		
		//flusher = new OutputFlusher();
		//new Thread(flusher).start();
	}
	
	public synchronized void stopRecording() throws IOException {
		recording = false;
		lock.release();
		count = 0;
		
		if(accreader != null) {
			accreader.stop();
		}
		if(locreader != null) {
			locreader.stop();
		}
		synchronized(this) {
			if(writer != null) {
				writer.close();
				writer = null;
			}
		}
	}

	public Exception getError() {
		return e;
	}
	
	public boolean isRecording() {
		return recording;
	}
	
	public boolean isLocked() {
		return locked;
	}
	
	public double getRuntime() {
		return recording ? (System.currentTimeMillis() - start) / 1000D : 0;
	}
	
	public int getCount() {
		return count;
	}
	
	public File getRecordingDirectory() {
		return new File(getExternalFilesDir(null), RECORDING_DIRECTORY);
	}
	
	public File getOutgoingDirectory() {
		return new File(getExternalFilesDir(null), OUTGOING_DIRECTORY);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		for(SensorEventListener l: listeners) l.onAccuracyChanged(sensor, accuracy);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if(recording) {
			try {
				synchronized (this) {
					if(writer != null) {
						count++;
						writer.writeByte(INDEX_ACCELERATION);
						writer.writeLong(System.currentTimeMillis());
						writer.writeFloat(event.values[0]);
						writer.writeFloat(event.values[1]);
						writer.writeFloat(event.values[2]);
					}
				}
			} catch (IOException e) {
				this.e = e;
				e.printStackTrace();
			}
		}
		if(recording) for(SensorEventListener l: listeners) l.onSensorChanged(event);
	}

	@Override
	public void onLocationChanged(Location location) {
		// time, lat, long, alt, acc
		if(recording) {
			try {
				synchronized (this) {
					if(writer != null) {
						count++;
						writer.writeByte(INDEX_LOCATION);
						writer.writeLong(location.getTime());
						writer.writeDouble(location.getLatitude());
						writer.writeDouble(location.getLongitude());
						writer.writeDouble(location.getAltitude());
						writer.writeFloat(location.getAccuracy());
					}
				}
			} catch (IOException e) {
				this.e = e;
				e.printStackTrace();
			}
		}
		if(recording) for(LocationListener l: listeners) l.onLocationChanged(location);
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
	
	private class LockStateReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context c, Intent i) {
			if(i.getAction().equals(Intent.ACTION_USER_PRESENT)) locked = false;
			else if(i.getAction().equals(Intent.ACTION_SCREEN_OFF)) locked = true;
		}
		
	}

}
