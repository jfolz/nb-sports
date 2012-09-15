// IRecorderService.aidl
package com.nobullshit.recorder;

// Declare any non-default types here with import statements
import com.nobullshit.sensor.ISensorReaderCallback;

/** Example service interface */
interface IRecorderService {

	void stopRecording();
	long getRuntime();
	boolean isRecording();
	boolean getSensorAvailable(int sensor);
	boolean getSensorEnabled(int sensor);
	boolean getSensorReading(int sensor);
	void registerCallback(ISensorReaderCallback callback);
	boolean unregisterCallback(ISensorReaderCallback callback);

}