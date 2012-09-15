package com.nobullshit.recorder.io;

import android.os.Parcel;
import android.os.Parcelable;

public class LocationParcelable implements Parcelable {
	
	long time;
	float x;
	float y;
	float z;
	
	public LocationParcelable(Parcel in) {
		time = in.readLong();
		x = in.readFloat();
		y = in.readFloat();
		z = in.readFloat();
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(time);
		dest.writeFloat(x);
		dest.writeFloat(y);
		dest.writeFloat(z);
	}
	
	public static final Parcelable.Creator<LocationParcelable> CREATOR =
			new Parcelable.Creator<LocationParcelable>() {
		public LocationParcelable createFromParcel(Parcel in) {
			return new LocationParcelable(in);
		}

		public LocationParcelable[] newArray(int size) {
			return new LocationParcelable[size];
		}
	};

}
