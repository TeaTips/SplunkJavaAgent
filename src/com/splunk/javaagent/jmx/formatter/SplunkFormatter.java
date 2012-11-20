package com.splunk.javaagent.jmx.formatter;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * <pre>
 * 
 * Abstract out a common methods and params for similar formatter implementations
 * 
 * <h3>Configurable Parameters</h3>
 * 
 * kvdelim : the key value pair delimeter.An equals is the default.
 * pairdelim : the delimter to use between pairs.A comma is the default.
 * prependDate : if true, then prepend an internal date to the event.false is the default.
 * dateformat : specify a date format as per http://docs.oracle.com/javase/6/docs/api/java/text/SimpleDateFormat.html
 *              yyyy-MM-dd HH:mm:ss:SSSZ is the default.
 * quotevalues : if true the put quotes around values.true is the default.
 * quotechar : the quote character to use. A doublequote is the default.
 * stripPattern.n : a numbered list of regex patterns that can be declared to strip the matching pattern from MBean attribute and operation values.
 * 
 * </pre>
 * 
 * @author Damien Dallimore damien@dtdsoftware.com
 * 
 */
public abstract class SplunkFormatter {

	// first part of the SPLUNK output String, common to all MBeans
	StringBuffer outputPrefix;

	// configurable parameters
	static final String KEY_VAL_DELIMITER_PARAM = "kvdelim";
	static final String PAIR_DELIMITER_PARAM = "pairdelim";
	static final String QUOTE_CHAR_PARAM = "quotechar";
	static final String QUOTEVALUES_PARAM = "quotevalues";
	static final String PREPENDED_DATE_FORMAT_PARAM = "dateformat";
	static final String PREPEND_DATE_PARAM = "prependDate";
	static final String STRIP_PATTERN = "stripPattern";

	String kvdelim = "=";// default
	String pairdelim = ",";// default
	char quotechar = '"';// default
	boolean prependDate = false;// default
	boolean quotevalues = true;// default
	List<Pattern> stripPatterns = new ArrayList<Pattern>();// collection of
															// strip patterns

	/**
	 * Default date format is using internal generated date
	 */
	private static final String DEFAULT_DATEFORMATPATTERN = "yyyy-MM-dd HH:mm:ss:SSSZ";
	/**
	 * Date Formatter instance
	 */
	private SimpleDateFormat DATEFORMATTER = new SimpleDateFormat(
			DEFAULT_DATEFORMATPATTERN);

	/**
	 * Build a key value pair based on configured delimiters
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	String buildPair(String key, String value) {

		return buildPair(key, value, quotevalues);
	}

	/**
	 * Build a key value pair based on configured delimiters
	 * 
	 * @param key
	 * @param value
	 * @param quote
	 *            whether or not to quote values
	 * @return
	 */
	String buildPair(String key, String value, boolean quoteVal) {

		StringBuffer result = new StringBuffer();
		if (quoteVal)
			result.append(key).append(kvdelim).append(quotechar).append(value)
					.append(quotechar).append(pairdelim);
		else
			result.append(key).append(kvdelim).append(value).append(pairdelim);
		return result.toString();

	}

	/**
	 * Set configurable parameters
	 * 
	 * @param parameters
	 */
	void setCommonSplunkParameters(Map<String, String> parameters) {

		String kv = parameters.get(KEY_VAL_DELIMITER_PARAM);
		if (kv != null && kv.length() > 0)
			kvdelim = kv;

		String pair = parameters.get(PAIR_DELIMITER_PARAM);
		if (pair != null && pair.length() > 0)
			pairdelim = pair;

		String qc = parameters.get(QUOTE_CHAR_PARAM);
		if (qc != null && qc.length() == 1)
			quotechar = qc.toCharArray()[0];

		String quote = parameters.get(QUOTEVALUES_PARAM);
		if (quote != null && quote.length() > 0) {
			quotevalues = Boolean.parseBoolean(quote);
		}

		String prependDateOption = parameters.get(PREPEND_DATE_PARAM);
		if (prependDateOption != null && prependDateOption.length() > 0) {
			prependDate = Boolean.parseBoolean(prependDateOption);
		}

		String predateformat = parameters.get(PREPENDED_DATE_FORMAT_PARAM);
		if (predateformat != null && predateformat.length() > 0) {
			DATEFORMATTER = new SimpleDateFormat(predateformat);
		}

		Set<String> keys = parameters.keySet();
		for (String key : keys) {
			if (key.startsWith(STRIP_PATTERN)) {
				String pattern = parameters.get(key);
				if (pattern != null && pattern.length() > 0)
					try {
						stripPatterns.add(Pattern.compile(pattern));
					} catch (Exception e) {
					}
			}
		}

	}

	/**
	 * Format common meta data
	 * 
	 * @param metaData
	 */
	void setCommonSplunkMetaData(Map<String, String> metaData) {

		String outputHostName = "";
		String configuredHostName = metaData.get(Formatter.META_HOST);
		String pid = metaData.get(Formatter.META_PROCESS_ID);
		String jvmDescription = metaData.get(Formatter.META_JVM_DESCRIPTION);

		// replace localhost names with actual hostname
		if (configuredHostName.equalsIgnoreCase("localhost")
				|| configuredHostName.equals("127.0.0.1") || pid != null) {
			try {
				outputHostName = InetAddress.getLocalHost().getHostName();
			} catch (Exception e) {
			}
		} else {
			outputHostName = configuredHostName;
		}

		this.outputPrefix.append(buildPair("host", outputHostName, false));
		this.outputPrefix.append(buildPair("jvmDescription", jvmDescription));

		if (pid != null) {

			this.outputPrefix.append(buildPair("pid", pid));
		}

	}

	/**
	 * Optionally prepend a date to the event String
	 * 
	 * @param timestamp
	 *            the event epoch time
	 * @param output
	 *            the event buffer
	 */
	void prependDate(long timestamp, StringBuffer output) {

		if (prependDate) {
			output.append(DATEFORMATTER.format(new Date(timestamp)))
					.append(" ");
		}

	}

	/**
	 * Optionally strip regex matched text from the attribute/operation values
	 * 
	 * @param rawValue
	 *            the raw input String
	 * @return the result of the search/replace
	 */
	String stripPatterns(String rawValue) {

		if (!stripPatterns.isEmpty()) {

			String stripped = rawValue;
			for (Pattern pattern : stripPatterns) {
				stripped = FormatterUtils.stripPattern(pattern, stripped);
			}
			return stripped;
		} else {
			return rawValue;
		}

	}

}
