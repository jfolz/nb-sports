package com.nobullshit.gpstracker.location;

import android.location.Location;

public class AccurateLocator implements Locator {
	public static float DEFAULT_MIN_ACCURACY = 2;
	private float minAccuracy;
	private Location lastloc;
	
	public AccurateLocator() {
		minAccuracy = DEFAULT_MIN_ACCURACY;
	}
	
	public void setMinAccuracy(float accuracy) {
		minAccuracy = accuracy;
	}
	
	public float getMinAccuracy(float accuracy) {
		return minAccuracy;
	}
	
	public boolean isAccurate() {
		if(lastloc == null) return false;
		else return lastloc.getAccuracy() < minAccuracy;
	}
	
	public Location getEstimate() {
		return new Location(lastloc);
	}

	public void update(Location newloc) {
		if(lastloc == null) {
			lastloc = newloc;
		}
		else {
			Location estimate = new Location(lastloc);
			double pstart = 1 / lastloc.getAccuracy();
			double ploc = 1 / newloc.getAccuracy();
			double norm = pstart+ploc;
			pstart = pstart / norm;
			ploc = ploc / norm;
			estimate.setLongitude(
				lastloc.getLongitude()*pstart + newloc.getLongitude()*ploc);
			estimate.setLatitude(
				lastloc.getLatitude()*pstart + newloc.getLatitude()*ploc);
			// compute standard deviation in meters
			double sig = Math.sqrt(
				Math.pow(estimate.distanceTo(lastloc)*pstart,2) + 
				Math.pow(estimate.distanceTo(newloc)*ploc,2));
			// use 3 sigma as a conversative accuracy value
			estimate.setAccuracy(3 * (float) sig);
			
			lastloc = estimate;
		}
	}
	
	public void reset() {
		lastloc = null;
	}
}
