package com.nobullshit.sensor;


import android.hardware.SensorEvent;
import android.location.Location;

public interface SensorReaderListener {
	
	/**
	 * Notify listeners of a changed sensor state.
	 * @param sensor type as defined by {@link SensorReader}
	 * @param state any state from {@link SensorReader}
	 */
	public void onSensorStateChanged(int sensor, int state);
	
	/**
	 * Notify listeners of a new sensor reading.
	 * @param sensor type as defined by {@link SensorReader}
	 * @param reading The reading, if sensor is {@link SensorReader.TYPE_ACCELEROMETER}
	 * 		this is a {@link SensorEvent}, if sensor is
	 * 		{@link SensorReader.TYPE_FINE_LOCATION} this is a {@link Location}
	 */
	public void onSensorReading(int sensor, Object reading);

}
