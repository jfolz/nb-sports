package com.nobullshit.binaryio;

import java.io.File;
import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

import com.nobullshit.sensor.AccelerationReader;
import com.nobullshit.sensor.LocationReader;

public class WriterService extends Service implements SensorEventListener, LocationListener {

	public static final int MESSAGE_START_RECORDING = 1;
	public static final int MESSAGE_STOP_RECORDING = 2;
	public static final String KEY_PATH = "KP";
	public static final String KEY_DATA = "KD";
	public static final String KEY_DEF_PREFIX = "DEF_";
	public static final String KEY_SERIES_INDEX = "SI";
	//public static final String KEY_ATTRIBUTE_TYPES = "KAT";
	//public static final String KEY_ATTRIBUTE_NAMES = "KAN";
	private Messenger messenger;
	private BinaryWriter writer;
	private boolean recording;
	private int count;
	private AccelerationReader accreader;
	private LocationReader locreader;
	
	@Override
	public void onCreate() {
		messenger = new Messenger(new DataHandler());
	}
	
	@Override
	public void onDestroy() {
		
	}

	@Override
	public IBinder onBind(Intent intent) {
		return messenger.getBinder();
	}
	
	private class DataHandler extends Handler {
		
        @Override
        public void handleMessage(Message msg) {
        	try {
        		Bundle b = msg.getData();
        		switch (msg.what) {
        		case MESSAGE_START_RECORDING:
        			startRecording(new File(b.getString(KEY_PATH)));
        			break;
        		case MESSAGE_STOP_RECORDING:
        			stopRecording();
        			break;
        		default:
        			super.handleMessage(msg);
        		}
        	} catch(Exception e) {
        		e.printStackTrace();
        	}
        }
	}

	public void startRecording(File out) throws IOException {
		count = 0;
		
		openFile(out,
				new String[] {"x","y","z"},
				new String[] {"float","float","float"},
				new String[] {"time","lat","long","alt","acc"},
				new String[] {"long","double","double","double","float"});

		if(accreader == null) {
			accreader = new AccelerationReader(this, 100);
		}
		if(locreader == null) {
			locreader = new LocationReader(this, 1000);
		}
		accreader.start();
		locreader.start();
	}
	
	public synchronized void stopRecording() throws Exception {
		count = 0;
		if(accreader != null) accreader.stop();
		if(locreader != null) locreader.stop();
		if(writer != null) writer.close();
	}
	
	private void openFile(File out, CharSequence[] ... defs) throws IOException {
		writer = new BinaryWriter(out, defs);
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
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
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		// time, lat, long, alt, acc
		synchronized (this) {
			if(recording) {
				count++;
				try {
					writer.writeLong(location.getTime());
					writer.writeDouble(location.getLatitude());
					writer.writeDouble(location.getLongitude());
					writer.writeDouble(location.getAltitude());
					writer.writeFloat(location.getAccuracy());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

}
