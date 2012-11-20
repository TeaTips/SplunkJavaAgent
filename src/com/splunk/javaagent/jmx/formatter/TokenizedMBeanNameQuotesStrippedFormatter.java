package com.splunk.javaagent.jmx.formatter;

import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import com.splunk.javaagent.SplunkLogEvent;

/**
 * <pre>
 * 
 * Custom formatter implementation that outputs the mbean canonical name as
 * split up tokens. 
 * 
 * Has some extra formatting specifics to deal with MBean
 * property values that are sometimes quoted.
 * </pre>
 * 
 * @author Damien Dallimore damien@dtdsoftware.com
 * 
 */
public class TokenizedMBeanNameQuotesStrippedFormatter extends SplunkFormatter
		implements Formatter {

	public TokenizedMBeanNameQuotesStrippedFormatter() {

		this.outputPrefix = new StringBuffer();
	}

	@Override
	public void setParameters(Map<String, String> parameters) {

		setCommonSplunkParameters(parameters);

	}

	@Override
	public SplunkLogEvent format(String mBean, Map<String, String> attributes,
			long timestamp) {

		SplunkLogEvent event = new SplunkLogEvent("jmx", "splunkagent", true,
				false);

		SortedMap<String, String> mbeanNameParts = FormatterUtils
				.tokenizeMBeanCanonicalName(mBean);

		Set<String> mBeanNameKeys = mbeanNameParts.keySet();

		for (String key : mBeanNameKeys) {

			event.addPair(key, mbeanNameParts.get(key));

		}

		// add mbean attributes
		Set<String> keys = attributes.keySet();
		for (String key : keys) {

			String value = attributes.get(key);
			value = FormatterUtils.stripNewlines(value);
			value = stripPatterns(value);
			event.addPair(key, value);
		}

		return event;

	}

	@Override
	public void setMetaData(Map<String, String> metaData) {

		setCommonSplunkMetaData(metaData);

	}

}
