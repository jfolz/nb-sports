package com.nobullshit.binaryio;

import android.os.Parcel;
import android.os.Parcelable;

public class ConfigurableParcelable implements Parcelable {
	
	public ConfigurableParcelable(Parcel in) {
		String config = in.readString();
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		
	}
	
	public static final Parcelable.Creator<ConfigurableParcelable> CREATOR =
			new Parcelable.Creator<ConfigurableParcelable>() {
		public ConfigurableParcelable createFromParcel(Parcel in) {
			return new ConfigurableParcelable(in);
		}

		public ConfigurableParcelable[] newArray(int size) {
			return new ConfigurableParcelable[size];
		}
	};

}
