package com.nobullshit.sensor;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.Log;

public class AccelerationReader extends BroadcastReceiver
		implements SensorEventListener, SensorReader {
	
	public static final int SCREEN_OFF_RECEIVER_DELAY = 500;
	public static final String TAG = "AccelerationReader";
	public static float NOMINAL_UPDATERATE = 5;
	
	private long start;
	private int updateCount;
	private List<SensorReaderListener> listeners;
	private Context context;
	private int readingState;
	
	public AccelerationReader(Context context, int updateRate) {
		this.context = context;		
		context.registerReceiver(this, new IntentFilter(Intent.ACTION_SCREEN_OFF));
		listeners = new ArrayList<SensorReaderListener>();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}

	@Override
	public void onSensorChanged(SensorEvent e) {
		for(SensorReaderListener listener: listeners)
				listener.onSensorReading(TYPE_ACCELEROMETER, e);
		updateCount++;
		
		if(readingState != SensorReader.STATE_READING)
			setReadingState(SensorReader.STATE_READING);
		else if(getUpdateRate() < NOMINAL_UPDATERATE / 2) {
			float rate = getUpdateRate();
			Log.v("AccelerationReader",rate + " < " + NOMINAL_UPDATERATE);
			setReadingState(SensorReader.STATE_PROCRASTINATING);
		}
	}
	
	public void start() {
		SensorManager m = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		Sensor s = m.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		m.registerListener(this, s, SensorManager.SENSOR_DELAY_NORMAL);
		
		start = System.currentTimeMillis();
		updateCount = 0;
	}
	
	public void stop() {		
		SensorManager m = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		m.unregisterListener(this);

		updateCount = 0;
		setReadingState(SensorReader.STATE_PROCRASTINATING);
	}
	
	private void setReadingState(int state) {
		int newState = readingState;
		if(state == SensorReader.STATE_READING && getUpdateRate() > NOMINAL_UPDATERATE / 2f)
			newState = SensorReader.STATE_READING;
		if(newState != readingState) {
			readingState = newState;
			for(SensorReaderListener listener: listeners)
				listener.onSensorStateChanged(TYPE_ACCELEROMETER, readingState);
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "onReceive("+intent+")");

		if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
			Runnable runnable = new Runnable() {
				public void run() {
					Log.i(TAG, "reregister listener");
					stop();
					start();
				}
			};

			new Handler().postDelayed(runnable, SCREEN_OFF_RECEIVER_DELAY);
		}
	}
	
	public boolean isAvailable() {
	    PackageManager pm = context.getPackageManager();
	    return pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER);
	}
	
	public boolean isEnabled() {
		return isAvailable();
	}
	
	public boolean isReading() {
		return readingState == SensorReader.STATE_READING;
	}
	
	public float getUpdateRate() {
		return (float) updateCount / (float) (System.currentTimeMillis() - start) * 1000f;
	}
	
	public float getNominalUpdateRate() {
		return NOMINAL_UPDATERATE;
	}

	@Override
	public void registerListener(SensorReaderListener listener) {
		listeners.add(listener);
	}

	@Override
	public boolean unregisterListener(SensorReaderListener listener) {
		return listeners.remove(listener);
	}

}
