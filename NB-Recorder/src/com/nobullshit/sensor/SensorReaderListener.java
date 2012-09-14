package com.nobullshit.sensor;


import android.hardware.SensorEventListener;
import android.location.LocationListener;

public interface SensorReaderListener extends LocationListener, SensorEventListener {
	
	/**
	 * Notify listeners of 
	 * @param sensor type as defined by {@link SensorReader}
	 * @param state any state from {@link SensorReader}
	 */
	public void onSensorStateChanged(int sensor, int state);

}
