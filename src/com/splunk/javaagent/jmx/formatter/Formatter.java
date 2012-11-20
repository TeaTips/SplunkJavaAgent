package com.splunk.javaagent.jmx.formatter;

import java.util.Map;

import com.splunk.javaagent.SplunkLogEvent;

/**
 * <pre>
 * This interface can be implemented to provide custom formatting logic
 * 
 * The custom implementation class can then be placed on the classpath and
 * declared in the configuration xml file.
 * </pre>
 * 
 * @author Damien Dallimore damien@dtdsoftware.com
 * 
 */
public interface Formatter {

	/**
	 * Key for the host value
	 */
	public static final String META_HOST = "meta_host";
	/**
	 * Key for the JVM Description
	 */
	public static final String META_JVM_DESCRIPTION = "meta_description";
	/**
	 * Key for the process ID
	 */
	public static final String META_PROCESS_ID = "meta_processid";

	/**
	 * Data that is common to each mbean line of attributes
	 * 
	 * @param metaData
	 *            map of meta data using Formatter constants as keys
	 */
	public void setMetaData(Map<String, String> metaData);

	/**
	 * Parameters that can be declared in the config xml file
	 * 
	 * @param parameters
	 *            map of key value pairs from the config xml file
	 */
	public void setParameters(Map<String, String> parameters);

	/**
	 * This method is called to format each Mbean attribute line
	 * 
	 * @param Mbean
	 *            the canonical mbean name
	 * @param attributes
	 *            map of mbean attributes
	 * @param timestamp
	 *            internal timestamp that can optionally be used in the output
	 *            line
	 * @return the event to send to Splunk
	 */
	public SplunkLogEvent format(String Mbean, Map<String, String> attributes,
			long timestamp);

}
