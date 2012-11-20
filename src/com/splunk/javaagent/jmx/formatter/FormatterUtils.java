package com.splunk.javaagent.jmx.formatter;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <pre>
 * Utility methods for formatting
 * </pre>
 * 
 * @author Damien Dallimore damien@dtdsoftware.com
 * 
 */
public abstract class FormatterUtils {

	/**
	 * Strip all whitespace chars and replace with a single space char
	 * 
	 * @param input
	 * @return
	 */
	public static String stripNewlines(String input) {

		if (input == null) {
			return "";
		}
		char[] chars = input.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (Character.isWhitespace(chars[i])) {
				chars[i] = ' ';
			}
		}

		return new String(chars);
	}

	/**
	 * Remove surrounding quotes from a string
	 * 
	 * @param quotedString
	 * @return
	 */
	public static String trimQuotes(String quotedString) {

		if (quotedString.startsWith("\"")) {
			quotedString = quotedString.substring(1);
		}
		if (quotedString.endsWith("\"")) {
			quotedString = quotedString.substring(0, quotedString.length() - 1);
		}

		return quotedString;
	}

	/**
	 * Remove matching pattern text from a raw JMX attribute/operation value
	 * 
	 * @param the
	 *            raw JMX attribute/operation value
	 * @return the JMX attribute/operation value with any matching patterns
	 *         stripped out
	 */
	public static String stripPattern(Pattern pattern, String rawValue) {

		try {
			Matcher m = pattern.matcher(rawValue);
			return m.replaceAll("");
		} catch (Exception e) {
		}
		return rawValue;
	}

	/**
	 * Take a canonical mbean name ie: "domain:key=value, key2=value2" , and
	 * split out the parts into individual fields.
	 * 
	 * @param mBean
	 *            the canonical mbean name
	 * @return sorted map of the name parts
	 */
	public static SortedMap<String, String> tokenizeMBeanCanonicalName(
			String mBean) {

		SortedMap<String, String> result = new TreeMap<String, String>();

		String[] parts = mBean.split(":");
		if (parts == null || parts.length != 2) {
			return result;
		}
		// the mbean domain
		result.put("mbean_domain", parts[0]);

		// the mbean properties
		String[] properties = parts[1].split(",");
		if (properties == null) {
			return result;
		}
		for (String prop : properties) {
			String[] property = prop.split("=");
			if (property == null || property.length != 2) {
				continue;
			}

			result.put("mbean_property_" + property[0],
					FormatterUtils.trimQuotes(property[1]));
		}

		return result;
	}

}
