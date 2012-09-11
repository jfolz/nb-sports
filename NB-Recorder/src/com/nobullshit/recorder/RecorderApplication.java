package com.nobullshit.recorder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Application;
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
	
	public static final String APP_DIRECTORY = "nb-recordings";
	
	private BinaryWriter accwriter;
	private AccelerationReader accreader;
	private BinaryWriter locwriter;
	private LocationReader locreader;
	private List<ListenerListener> listeners;
	private Exception e;
	private boolean recording;
	private PowerManager.WakeLock lock;
	private long start;
	private int count;
	
	@Override
	public void onCreate() {
		super.onCreate();
		listeners = new ArrayList<ListenerListener>(4);
		recording = false;
		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
		lock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "RecorderApplication");
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
		recording = !recording;
	}

	public void startRecording() throws IOException {
		lock.acquire();
		count = 0;
		long now = System.currentTimeMillis();
		
		File dir = new File(getExternalFilesDir(null),APP_DIRECTORY);
		if(!dir.isDirectory()) dir.mkdir();
		
		// acceleration
		File out = new File(dir, "acc_"+now);
		accwriter = new BinaryWriter(out,new String[] {"x","y","z"}, new Object[] {1D,1D,1D});
		if(accreader == null) accreader = new AccelerationReader(this, this, 100);
		
		// location
		out = new File(dir, "gps_"+now);
		locwriter = new BinaryWriter(out,new String[] {"time","lat","long","alt","acc"}, new Object[] {1L,1D,1D,1D,1F});
		if(locreader == null) locreader = new LocationReader(this, this, 1000);

		accreader.start();
		locreader.start();
		start = System.currentTimeMillis();
	}
	
	public synchronized void stopRecording() throws IOException {
		lock.release();
		count = 0;
		
		if(accreader != null) {
			accreader.stop();
		}
		if(locreader != null) {
			locreader.stop();
		}
		if(accwriter != null) {
			accwriter.close();
			accwriter = null;
		}
		if(locwriter != null) {
			locwriter.close();
			locwriter = null;
		}
	}

	public Exception getError() {
		return e;
	}
	
	public boolean isRecording() {
		return recording;
	}
	
	public double getRuntime() {
		return recording ? (System.currentTimeMillis() - start) / 1000D : 0;
	}
	
	public int getCount() {
		return count;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		for(SensorEventListener l: listeners) l.onAccuracyChanged(sensor, accuracy);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		synchronized (this) {
			if(recording) {
				count++;
				try {
					accwriter.writeLong(System.currentTimeMillis());
					for(float v: event.values) {
						accwriter.writeFloat(v);
					}
				} catch (IOException e) {
					this.e = e;
					e.printStackTrace();
				}
			}
		}
		if(recording) for(SensorEventListener l: listeners) l.onSensorChanged(event);
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		// time, lat, long, alt, acc
		synchronized (this) {
			if(recording) {
				try {
					locwriter.writeLong(location.getTime());
					locwriter.writeDouble(location.getLatitude());
					locwriter.writeDouble(location.getLongitude());
					locwriter.writeDouble(location.getAltitude());
					locwriter.writeFloat(location.getAccuracy());
				} catch (IOException e) {
					this.e = e;
					e.printStackTrace();
				}
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

}
