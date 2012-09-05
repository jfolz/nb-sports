package com.nobullshit.gpstracker.location;

import android.location.Location;

public interface Locator {
	public void update(Location newlocation);
	public boolean isAccurate();
	public Location getEstimate();
	public void reset();
}
