package org.hfoss.posit;

public class StringUtils {

	/**
	 * Check if a String is blank
	 * @param text
	 * @return
	 */
	public static boolean isBlank(String text) {
		return (text.equals(null)|| text.equals(""));
	}

}
