package com.nobullshit.gpstracker;

import android.location.Location;

public class Statistics {
	private Location lastloc;
	private int numupdates;
	private double distance;
	private double speed;
	private double altitude;
	private long starttime;
	
	public Statistics(Location start) {
		lastloc = start;
		numupdates = 0;
		distance = 0;
		speed = 0;
		altitude = 0;
		starttime = System.currentTimeMillis();
	}
	
	public void update(Location newloc) {
		numupdates++;
		// if speed is very low, it is very likely that any
		// "movement" is just error margin
		if(newloc.getSpeed() > .3) {
			distance += lastloc.distanceTo(newloc);
			speed += newloc.getSpeed();
			altitude += newloc.getAltitude();
			lastloc = newloc;
		}
	}
	
	public double getDistance() {
		return distance;
	}
	
	public double getAvgSpeed() {
		return speed / numupdates;
	}
	
	public double getAvgAltitude() {
		return altitude / numupdates;
	}
	
	public long getDuration() {
		return System.currentTimeMillis() - starttime;
	}
}
