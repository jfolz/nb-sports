package com.nobullshit.text;

import java.text.DecimalFormat;

/**
 * A simple wrapper-class for {@link DecimalFormat}.
 * @author riDDi
 *
 */
public class DecimalFormatter implements Formatter {
	
	private DecimalFormat f;
	
	/**
	 * Wraps a {@link DecimalFormat}. Pattern is passed on without modifications.
	 * @param pattern passed to a new {@link DecimalFormat} instance
	 */
	public DecimalFormatter(String pattern) {
		f = new DecimalFormat(pattern);
	}

	public String format(double val) {
		return StringUtil.trim(f.format(val));
	}

}
