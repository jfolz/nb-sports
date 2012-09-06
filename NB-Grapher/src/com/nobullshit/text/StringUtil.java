package com.nobullshit.text;

public class StringUtil {

	public static String trim(String s) {
		char[] c = new char[s.length()];
		s.getChars(0, c.length, c, 0);
		return new String(c);
	}
	
}
