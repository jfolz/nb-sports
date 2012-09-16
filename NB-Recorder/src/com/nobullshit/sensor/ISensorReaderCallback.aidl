// ISensorReaderCallBack.aidl
package com.nobullshit.sensor;

// Declare any non-default types here with import statements
import com.nobullshit.sensor.Reading;
import android.location.Location;

/** Example service interface */
interface ISensorReaderCallback {
	
	int getId();

	/**
	 * Notify listeners of a changed sensor state.
	 * @param sensor type as defined by {@link SensorReader}
	 * @param state any state from {@link SensorReader}
	 */
	void onSensorStateChanged(int sensor, int state);
	
	/**
	 * Notify listeners of a new sensor reading.
	 * @param sensor type as defined by {@link SensorReader}
	 * @param reading The reading, if sensor is {@link SensorReader.TYPE_ACCELEROMETER}
	 * 		this is a {@link SensorEvent}, if sensor is
	 * 		{@link SensorReader.TYPE_FINE_LOCATION} this is a {@link Location}
	 */
	void onSensorReading(in Reading reading);
	
	/**
	 * Notify listeners of an error they might need to know of.
	 * @param type the type of error as defined by {@link SensorReader}
	 * @param msg a message associated with the error
	 */
	void onError(int type, in String msg);

}