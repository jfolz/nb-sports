package com.nobullshit.gpstracker;

public class Time {
	public static CharSequence format(long delta) {
		long seconds = delta / 1000;
		int hours = (int) seconds / 3600;
		seconds -= hours * 3600;
		int minutes = (int) seconds / 60;
		seconds -= minutes * 60;

		StringBuilder time = new StringBuilder();
		time.append(hours < 10 ? "0" : "");
		time.append(hours);
		time.append(":");
		time.append(minutes < 10 ? "0" : "");
		time.append(minutes);
		time.append(":");
		time.append(seconds < 10 ? "0" : "");
		time.append(seconds);
		return time;
	}
}
