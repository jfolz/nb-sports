package com.nobullshit.recorder.io;

import android.os.Parcel;
import android.os.Parcelable;

public class AccelerationParcelable implements Parcelable {
	
	long time;
	float x;
	float y;
	float z;
	
	public AccelerationParcelable(Parcel in) {
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
	
	public static final Parcelable.Creator<AccelerationParcelable> CREATOR =
			new Parcelable.Creator<AccelerationParcelable>() {
		public AccelerationParcelable createFromParcel(Parcel in) {
			return new AccelerationParcelable(in);
		}

		public AccelerationParcelable[] newArray(int size) {
			return new AccelerationParcelable[size];
		}
	};

}
