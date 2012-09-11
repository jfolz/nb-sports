package com.nobullshit.sensor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.Log;

public class AccelerationReader extends BroadcastReceiver implements SensorEventListener {
	
	public static final int SCREEN_OFF_RECEIVER_DELAY = 500;
	public static final String TAG = "AccelerationReader";
	
	private SensorEventListener listener;
	private Context context;
	
	public AccelerationReader(SensorEventListener listener, Context context, int updateRate) {
		this.listener = listener;
		this.context = context;
		context.registerReceiver(this, new IntentFilter(Intent.ACTION_SCREEN_OFF));
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		listener.onAccuracyChanged(sensor, accuracy);
	}

	@Override
	public void onSensorChanged(SensorEvent e) {
		// TODO Auto-generated method stub
		listener.onSensorChanged(e);
	}
	
	public void start() {
		SensorManager m = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		Sensor s = m.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		m.registerListener(this, s, SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	public void stop() {
		SensorManager m = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		m.unregisterListener(this);
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

}
