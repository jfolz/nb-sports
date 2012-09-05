package com.nobullshit.text;

public class SimpleFormatter implements Formatter {
	
	private String format;
	
	public SimpleFormatter(String format) {
		this.format = format;
	}

	public String format(double val) {
		return String.format(format, val);
	}

}
