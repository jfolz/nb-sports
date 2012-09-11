package com.nobullshit.sensor;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class LocationReader implements LocationListener {
	
	private static final String PROVIDER_UNKNOWN = "unknown";
	
	private LocationListener parent;
	private Context context;
	private int numsats;
	private int updateRate;
	private String providerType;
	
	public LocationReader(Context context, LocationListener parent, int updateRate) {
		this.parent = parent;
		this.context = context;
		this.numsats = 0;
		this.updateRate = updateRate;
		this.providerType = PROVIDER_UNKNOWN;
	}

	public void onLocationChanged(Location location) {
		parent.onLocationChanged(location);
	}

	public void onProviderDisabled(String provider) {
		parent.onProviderDisabled(provider);
	}

	public void onProviderEnabled(String provider) {
		parent.onProviderEnabled(provider);
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		parent.onStatusChanged(provider, status, extras);
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
		LocationManager locationManager = (LocationManager)
				context.getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(
				LocationManager.GPS_PROVIDER, updateRate, 0, this);
	}
	
	public void stop() {
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

}
