// IRecorderService.aidl
package com.nobullshit.recorder;

// Declare any non-default types here with import statements

/** Example service interface */
interface IRecorderService {

	void startRecording();
	void stopRecording();
	void toggleRecording();
	long getRuntime();
	boolean isRecording();
	boolean getSensorAvailable(int sensor);
	boolean getSensorEnabled(int sensor);
	boolean getSensorReading(int sensor);

}