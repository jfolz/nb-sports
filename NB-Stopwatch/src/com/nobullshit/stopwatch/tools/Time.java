package com.nobullshit.stopwatch.tools;

public class Time {
	public static CharSequence secondsToString(long seconds) {
		StringBuilder b = new StringBuilder();
		long h,m,s;
		
		s = seconds % 60;
		seconds /= 60;
		m = seconds % 60;
		seconds /= 60;
		h = seconds;

		b.append((h >= 10 ? "" : "0") + h);
		b.append(':');
		b.append((m >= 10 ? "" : "0") + m);
		b.append(':');
		b.append((s >= 10 ? "" : "0") + s);
		
		return b.toString();
	}
	
	public static CharSequence millisToString(long millis) {
		return secondsToString(millis / 1000);
	}
}
