package com.nobullshit.gpstracker;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import android.location.Location;

public class LocationReader {
	/** Data stored as HEADER[TIME/LONG/LAT/ALT/ACC] */
	public static long HEADER_V1 = 1;
	/** Data stored as HEADER[TIME/LONG/LAT/ALT/ACC/SPD] */
	public static long HEADER_V2 = 2;
	private DataInputStream is;
	private long version;
	
	public LocationReader(File infile) throws IOException {
		is = new DataInputStream(new FileInputStream(infile));
		version = is.readLong();
		if(version == HEADER_V1) {}
		else if(version == HEADER_V2) {}
		else {
			throw new IOException(
					infile.getName() + 
					" has unknown header version " + 
					version);
		}
	}
	
	public Location readLocation() throws IOException {
		Location loc = new Location("SportsTracker");
		loc.setTime(is.readLong());
		loc.setLongitude(is.readDouble());
		loc.setLatitude(is.readDouble());
		loc.setAltitude(is.readDouble());
		loc.setAccuracy(is.readFloat());
		if(version == HEADER_V2) loc.setSpeed(is.readFloat());
		return loc;
	}
}
