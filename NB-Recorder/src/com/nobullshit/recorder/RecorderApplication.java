package com.nobullshit.recorder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Application;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.PowerManager;
import android.util.Log;

import com.nobullshit.binaryio.BinaryWriter;
import com.nobullshit.sensor.AccelerationReader;

public class RecorderApplication extends Application implements SensorEventListener {
	
	public static final String APP_DIRECTORY = "nb-recordings";
	
	private BinaryWriter writer;
	private AccelerationReader reader;
	private List<SensorEventListener> listeners;
	private Exception e;
	private boolean recording;
	private PowerManager.WakeLock lock;
	private long start;
	private int count;
	
	@Override
	public void onCreate() {
		super.onCreate();
		listeners = new ArrayList<SensorEventListener>(4);
		recording = false;
		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
		lock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "RecorderApplication");
	}
	
	public void addListener(SensorEventListener e) {
		listeners.add(e);
	}
	
	public void removeListener(SensorEventListener e) {
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

	public String startRecording() throws IOException {
		lock.acquire();
		count = 0;
		
		File dir = new File(getExternalFilesDir(null),APP_DIRECTORY);
		if(!dir.isDirectory()) dir.mkdir();
		File out = new File(dir, "rec_"+System.currentTimeMillis());
		writer = new BinaryWriter(out,new String[] {"x","y","z"}, new Object[] {1D,1D,1D});
		if(reader == null) reader = new AccelerationReader(this, this, 100);
		reader.start();

		start = System.currentTimeMillis();
		return out.getAbsolutePath();
	}
	
	public synchronized void stopRecording() throws IOException {
		lock.release();
		count = 0;
		
		if(reader != null) {
			reader.stop();
		}
		if(writer != null) {
			writer.close();
			writer = null;
		}
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
					writer.writeLong(System.currentTimeMillis());
					for(float v: event.values) {
						writer.writeFloat(v);
					}
				} catch (IOException e) {
					this.e = e;
					e.printStackTrace();
				}
			}
		}
		if(recording) for(SensorEventListener l: listeners) l.onSensorChanged(event);
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

}
