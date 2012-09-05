package com.nobullshit.gpstracker.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class LocationProvider implements LocationListener {
	private static String PROVIDER_UNKNOWN = "unknown";
	private LocationListener parent;
	private Context context;
	private int numsats;
	private String providerType;
	//private Location last;
	// current speed in ¡latitude/s
	//private double speedLat;
	// current speed in ¡longitude/s
	//private double speedLong;
	
	public LocationProvider(Context context, LocationListener parent) {
		this.parent = parent;
		this.context = context;
		this.numsats = 0;
		this.providerType = PROVIDER_UNKNOWN;
	}

	public void onLocationChanged(Location location) {
		parent.onLocationChanged(location);
	}

	public void onProviderDisabled(String provider) {
		parent.onProviderDisabled(provider);
		Log.v("LocationProvider",provider + " disabled");
	}

	public void onProviderEnabled(String provider) {
		parent.onProviderEnabled(provider);
		Log.v("LocationProvider",provider + " enabled");
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
		}
	}
	
	public void startListening() {
		LocationManager locationManager = (LocationManager)
				context.getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(
				LocationManager.GPS_PROVIDER, 1000, 0, this);
	}
	
	public void stopListening() {
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
