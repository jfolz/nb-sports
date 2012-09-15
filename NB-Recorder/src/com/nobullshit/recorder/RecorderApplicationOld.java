package com.nobullshit.recorder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorEvent;
import android.location.Location;
import android.os.PowerManager;
import android.util.Log;

import com.nobullshit.binaryio.BinaryWriter;
import com.nobullshit.sensor.AccelerationReader;
import com.nobullshit.sensor.LocationReader;
import com.nobullshit.sensor.SensorReader;
import com.nobullshit.sensor.SensorReaderListener;

public class RecorderApplicationOld extends Application implements SensorReaderListener {
	
	private BinaryWriter writer;
	private AccelerationReader accreader;
	private LocationReader locreader;
	private List<SensorReaderListener> listeners;
	private Exception e;
	private boolean recording;
	private PowerManager.WakeLock lock;
	private long start;
	private boolean locked;
	
	@Override
	public void onCreate() {
		super.onCreate();
		listeners = new ArrayList<SensorReaderListener>(4);
		recording = false;
		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
		lock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "RecorderApplication");
		locked = false;
		
		LockStateReceiver receiver = new LockStateReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_USER_PRESENT);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		registerReceiver(receiver, filter);
	}
	
	public void addListener(SensorReaderListener e) {
		listeners.add(e);
	}
	
	public void removeListener(SensorReaderListener e) {
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
	}

	public synchronized void startRecording() throws IOException {
		lock.acquire();
		long now = System.currentTimeMillis();
		
		File dir = getRecordingDirectory();
		if(!dir.isDirectory()) dir.mkdir();
		
		File out = new File(dir, "rec_"+now);
		writer = new BinaryWriter(out,
				new String[] {"time","x","y","z"},
				new String[] {"long","float","float","float"},
				new String[] {"time","lat","long","alt","acc"},
				new String[] {"long","double","double","double","float"});
		
		if(accreader == null) {
			accreader = new AccelerationReader(this, 100);
			accreader.registerListener(this);
		}
		if(locreader == null) {
			locreader = new LocationReader(this, 1000);
			locreader.registerListener(this);
		}
		accreader.start();
		locreader.start();
		start = System.currentTimeMillis();
		recording = true;
		
		//flusher = new OutputFlusher();
		//new Thread(flusher).start();
	}
	
	public synchronized void stopRecording() throws IOException {
		recording = false;
		lock.release();
		
		if(accreader != null) {
			accreader.stop();
		}
		if(locreader != null) {
			locreader.stop();
		}
		synchronized(this) {
			if(writer != null) {
				writer.close();
				writer = null;
			}
		}
	}

	public Exception getError() {
		return e;
	}
	
	public boolean isRecording() {
		return recording;
	}
	
	public boolean isLocked() {
		return locked;
	}
	
	public double getRuntime() {
		return recording ? (System.currentTimeMillis() - start) / 1000D : 0;
	}
	
	public File getRecordingDirectory() {
		return new File(getExternalFilesDir(null), RecorderService.RECORDING_DIRECTORY);
	}
	
	public File getOutgoingDirectory() {
		return new File(getExternalFilesDir(null), RecorderService.OUTGOING_DIRECTORY);
	}

	public boolean getSensorAvailable(int sensor) {
		switch(sensor) {
		case SensorReader.TYPE_ACCELEROMETER:
			return accreader.isAvailable();
		case SensorReader.TYPE_FINE_LOCATION:
			return locreader.isAvailable();
		default:
			return false;
		}
	}

	public boolean getSensorEnabled(int sensor) {
		switch(sensor) {
		case SensorReader.TYPE_ACCELEROMETER:
			return accreader != null && accreader.isEnabled();
		case SensorReader.TYPE_FINE_LOCATION:
			return locreader != null && locreader.isEnabled();
		default: return false;
		}
	}

	public boolean getSensorReading(int sensor) {
		switch(sensor) {
		case SensorReader.TYPE_ACCELEROMETER:
			return accreader != null && accreader.isReading();
		case SensorReader.TYPE_FINE_LOCATION:
			return locreader != null && locreader.isReading();
		default:
			return false;
		}
	}

	@Override
	public void onSensorStateChanged(int sensor, int state) {
		Log.v("RecorderApplication", "sensor " + sensor + " now in state " + state);
		if(recording) for(SensorReaderListener l: listeners)
				l.onSensorStateChanged(sensor, state);
	}

	@Override
	public void onSensorReading(int sensor, Object reading) {
		switch(sensor) {
		case SensorReader.TYPE_ACCELEROMETER:
			onAcceleration((SensorEvent) reading);
			break;
		case SensorReader.TYPE_FINE_LOCATION:
			onLocation((Location) reading);
			break;
		}
		if(recording) for(SensorReaderListener l: listeners)
				l.onSensorReading(sensor,reading);
	}

	private void onAcceleration(SensorEvent event) {
		if(recording) {
			try {
				synchronized (this) {
					if(writer != null) {
						writer.writeByte(RecorderService.INDEX_ACCELERATION);
						writer.writeLong(System.currentTimeMillis());
						writer.writeFloat(event.values[0]);
						writer.writeFloat(event.values[1]);
						writer.writeFloat(event.values[2]);
					}
				}
			} catch (IOException e) {
				this.e = e;
				e.printStackTrace();
			}
		}
	}

	private void onLocation(Location location) {
		// time, lat, long, alt, acc
		if(recording) {
			try {
				synchronized (this) {
					if(writer != null) {
						writer.writeByte(RecorderService.INDEX_LOCATION);
						writer.writeLong(location.getTime());
						writer.writeDouble(location.getLatitude());
						writer.writeDouble(location.getLongitude());
						writer.writeDouble(location.getAltitude());
						writer.writeFloat(location.getAccuracy());
					}
				}
			} catch (IOException e) {
				this.e = e;
				e.printStackTrace();
			}
		}
	}
	
	private class LockStateReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context c, Intent i) {
			if(i.getAction().equals(Intent.ACTION_USER_PRESENT)) locked = false;
			else if(i.getAction().equals(Intent.ACTION_SCREEN_OFF)) locked = true;
		}
		
	}

}