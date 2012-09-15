package com.nobullshit.sensor;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;

public class LocationReader implements LocationListener, SensorReader {
	
	private static final String PROVIDER_UNKNOWN = "unknown";
	public static float NOMINAL_UPDATERATE = 1;
	
	private long start;
	private int updateCount;
	private List<SensorReaderListener> listeners;
	private Context context;
	private int numsats;
	private int updateRate;
	private String providerType;
	private int readingState;
	
	public LocationReader(Context context, int updateRate) {
		this.context = context;
		this.numsats = 0;
		this.updateRate = updateRate;
		this.providerType = PROVIDER_UNKNOWN;
		listeners = new ArrayList<SensorReaderListener>();
	}

	public void onLocationChanged(Location location) {
		for(SensorReaderListener listener: listeners)
				listener.onSensorReading(TYPE_FINE_LOCATION, location);
		broadcastReadingState(SensorReader.STATE_READING);
	}

	public void onProviderDisabled(String provider) {
		readingState = STATE_PROCRASTINATING;
		broadcastState(SensorReader.STATE_DISABLED);
	}

	public void onProviderEnabled(String provider) {
		readingState = STATE_PROCRASTINATING;
		broadcastState(SensorReader.STATE_ENABLED);
		start();
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		switch(status) {
		case LocationProvider.TEMPORARILY_UNAVAILABLE:
			broadcastReadingState(SensorReader.STATE_PROCRASTINATING);
			break;
		case LocationProvider.AVAILABLE:
			if(provider != null) this.providerType = provider;
			else this.providerType = PROVIDER_UNKNOWN;
			if(extras != null) numsats = extras.getInt("satellites",0);
			break;
		default:
			this.providerType = PROVIDER_UNKNOWN;
			numsats = 0;
		}
	}
	
	private void broadcastReadingState(int state) {
		if(state != readingState) {
			readingState = state;
			broadcastState(readingState);
		}
	}
	
	private void broadcastState(int state) {
		// TODO check how and when state is broadcasted
		for(SensorReaderListener listener: listeners)
			listener.onSensorStateChanged(TYPE_FINE_LOCATION, state);
	}
	
	public void start() {
		start = System.currentTimeMillis();
		updateCount = 0;
		
		LocationManager locationManager = (LocationManager)
				context.getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(
				LocationManager.GPS_PROVIDER, updateRate, 0, this);
		
		broadcastReadingState(SensorReader.STATE_PROCRASTINATING);
	}
	
	public void stop() {
		updateCount = 0;
		
		LocationManager locationManager = (LocationManager)
		context.getSystemService(Context.LOCATION_SERVICE);
		locationManager.removeUpdates(this);

		broadcastReadingState(SensorReader.STATE_PROCRASTINATING);
	}
	
	public int satelliteCount() {
		return numsats;
	}
	
	public String providerType() {
		return providerType;
	}
	
	public boolean isAvailable() {
	    PackageManager pm = context.getPackageManager();
	    return pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
	}
	
	public boolean isEnabled() {
		LocationManager locationManager = (LocationManager)
				context.getSystemService(Context.LOCATION_SERVICE);
		return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}
	
	@Override
	public boolean isReading() {
		return readingState == SensorReader.STATE_READING;
	}

	public float getUpdateRate() {
		return (float) updateCount / (float) (System.currentTimeMillis() - start) * 1000f;
	}
	
	public float getNominalUpdateRate() {
		return NOMINAL_UPDATERATE;
	}

	@Override
	public void registerListener(SensorReaderListener listener) {
		listeners.add(listener);
	}

	@Override
	public boolean unregisterListener(SensorReaderListener listener) {
		return listeners.remove(listener);
	}

}
