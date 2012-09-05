package com.nobullshit.gpstracker;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.location.Location;
import android.util.Log;

public class LocationWriter {
	private static long HEADER = LocationReader.HEADER_V2;
	private DataOutputStream os;
	
	public LocationWriter(File outfile) {
        try {
        	os = new DataOutputStream(new FileOutputStream(outfile));
        	os.writeLong(HEADER);
        }
        catch (IOException e) {
        	String msg = String.format("could not open file %s: %s",
    				outfile.getName(), e.getMessage());
    		Log.e("SportsTracker",msg);
        }
	}
	
	public void close() {
		try {
			os.close();
		}
    	catch (IOException e) {
    		String msg = String.format("could not close output stream: %s",
    				e.getMessage());
    		Log.e("SportsTracker", msg);
		}
	}
	
	public void write(Location loc) {
		try{
			os.writeLong(loc.getTime());
			os.writeDouble(loc.getLongitude());
			os.writeDouble(loc.getLatitude());
			os.writeDouble(loc.getAltitude());
			os.writeFloat(loc.getAccuracy());
			os.writeFloat(loc.getSpeed());
		}
		catch (IOException e) {
			String msg = String.format("could not write Location: %s",
					e.getMessage());
			Log.e("SportsTracker",msg);
		}
	}
}
