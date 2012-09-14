package com.nobullshit.sensor;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

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
	
	public LocationReader(Context context, int updateRate) {
		this.context = context;
		this.numsats = 0;
		this.updateRate = updateRate;
		this.providerType = PROVIDER_UNKNOWN;
		listeners = new ArrayList<SensorReaderListener>();
	}

	public void onLocationChanged(Location location) {
		for(SensorReaderListener listener: listeners)
				listener.onLocationChanged(location);
	}

	public void onProviderDisabled(String provider) {
		for(SensorReaderListener listener: listeners) {
			listener.onProviderDisabled(provider);
			listener.onSensorStateChanged(
					SensorReader.TYPE_FINE_LOCATION,
					SensorReader.STATE_DISABLED);
		}
		Log.v("LocationReader","GPS disabled");
	}

	public void onProviderEnabled(String provider) {
		for(SensorReaderListener listener: listeners) {
			listener.onProviderEnabled(provider);
			listener.onSensorStateChanged(
					SensorReader.TYPE_FINE_LOCATION,
					SensorReader.STATE_ENABLED);
		}
		Log.v("LocationReader","GPS enabled");
		start();
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		for(SensorReaderListener listener: listeners)
				listener.onStatusChanged(provider, status, extras);
		switch(status) {
		case android.location.LocationProvider.AVAILABLE:
			if(provider != null) this.providerType = provider;
			else this.providerType = PROVIDER_UNKNOWN;
			if(extras != null) numsats = extras.getInt("satellites",0);
			break;
		default:
			this.providerType = PROVIDER_UNKNOWN;
			numsats = 0;
		}
	}
	
	public void start() {
		start = System.currentTimeMillis();
		updateCount = 0;
		
		LocationManager locationManager = (LocationManager)
				context.getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(
				LocationManager.GPS_PROVIDER, updateRate, 0, this);
	}
	
	public void stop() {
		updateCount = 0;
		
		LocationManager locationManager = (LocationManager)
		context.getSystemService(Context.LOCATION_SERVICE);
		locationManager.removeUpdates(this);
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
