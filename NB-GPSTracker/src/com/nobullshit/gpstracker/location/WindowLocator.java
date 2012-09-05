package com.nobullshit.gpstracker.location;

import android.location.Location;

public class WindowLocator implements Locator {
	public static int WINDOW_SIZE = 5;
	private Location[] buffer;
	private int pos;
	private Location estimate;
	private boolean isWindowFull;
	
	public WindowLocator(Location start) {
		estimate = start;
		reset();
		buffer[pos] = start;
		pos = (pos + 1) % WINDOW_SIZE;
	}

	public Location getEstimate() {
		return estimate;
	}

	public boolean isAccurate() {
		// TODO Auto-generated method stub
		return false;
	}

	public void reset() {
		buffer = new Location[WINDOW_SIZE];
		pos = 0;
	}

	public void update(Location newlocation) {
		Location E = new Location(newlocation);
		buffer[pos] = newlocation;
		int max;
		if(!isWindowFull) {
			if(pos + 1 == WINDOW_SIZE) isWindowFull = true;
			max = pos + 1;
		}
		else max = WINDOW_SIZE;
		
		pos = (pos + 1) % WINDOW_SIZE;
		double norm = 0;
		double[] p = new double[max];
		for(int i=0; i<max; i++) {
			p[i] = 1. / buffer[i].getAccuracy();
			norm += p[i];
		}
		for(int i=0; i<max; i++) p[i] = p[i] / norm;
		double longitude = 0;
		double latitude = 0;
		double altitude = 0;
		for(int i=0; i<max; i++) {
			longitude += p[i] * buffer[i].getLongitude();
			latitude += p[i] * buffer[i].getLatitude();
			altitude += p[i] * buffer[i].getAltitude();
		}
		E.setLongitude(longitude);
		E.setLatitude(latitude);
		E.setAltitude(altitude);
		double accuracy = 0;
		for(int i=0; i<max; i++) {
			accuracy += Math.pow(E.distanceTo(buffer[i]), 2);
		}
		E.setAccuracy((float) Math.sqrt(accuracy) * 3);
		double deltat = (E.getTime()-estimate.getTime())/1000.;
		double distance = estimate.distanceTo(E);
		E.setSpeed((float) (distance / deltat));
		estimate = E;
	}

}
