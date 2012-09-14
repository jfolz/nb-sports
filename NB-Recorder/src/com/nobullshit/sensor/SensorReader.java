package com.nobullshit.sensor;


public interface SensorReader {

	public static int TYPE_ACCELEROMETER = 0;
	public static int TYPE_FINE_LOCATION = 1;

	public static int STATE_AVAILABLE = 0;
	public static int STATE_UNAVAILABLE = 1;
	public static int STATE_ENABLED = 2;
	public static int STATE_DISABLED = 4;
	public static int STATE_READING = 8;
	public static int STATE_PROCRASTINATING = 16;

	public boolean isEnabled();
	public boolean isAvailable();
	public boolean isReading();
	public float getUpdateRate();
	public float getNominalUpdateRate();
	public void registerListener(SensorReaderListener listener);
	public boolean unregisterListener(SensorReaderListener listener);
	
}
