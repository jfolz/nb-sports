package com.nobullshit.text;

import android.text.format.DateFormat;

public class DateFormatter implements Formatter {
	
	CharSequence format;
	
	public DateFormatter(CharSequence format) {
		this.format = format;
	}

	public String format(double val) {
		return StringUtil.trim(DateFormat.format(format, (long) Math.round(val)).toString());
	}

}
