package com.nobullshit.sensor;

import android.hardware.SensorEvent;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

public class Reading implements Parcelable {
	
	public int sensor;
	public float[] values;
	public Location location;
	
	public Reading(int sensor, Object reading) {
		this.sensor = sensor;
		switch(sensor) {
		case SensorReader.TYPE_FINE_LOCATION:
			location = (Location) reading;
			break;
		case SensorReader.TYPE_ACCELEROMETER:
		default:
			SensorEvent event = (SensorEvent) reading;
			values = event.values;
		}
	}
	
	public Reading(Location location, int sensor) {
		this.sensor = sensor;
		this.location = location;
	}
	
	public Reading(Parcel in) {
		sensor = in.readInt();
		switch(sensor) {
		case SensorReader.TYPE_FINE_LOCATION:
			location = (Location) in.readParcelable(getClass().getClassLoader());
			break;
		case SensorReader.TYPE_ACCELEROMETER:
		default:
			values = in.createFloatArray();
		}
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(sensor);
		switch(sensor) {
		case SensorReader.TYPE_FINE_LOCATION:
			dest.writeParcelable(location, 0);
			break;
		case SensorReader.TYPE_ACCELEROMETER:
		default:
			dest.writeFloatArray(values);
		}
	}
	
	public static final Parcelable.Creator<Reading> CREATOR =
			new Parcelable.Creator<Reading>() {
				public Reading createFromParcel(Parcel in) {
					return new Reading(in);
				}
		
				public Reading[] newArray(int size) {
					return new Reading[size];
				}
			};

}
