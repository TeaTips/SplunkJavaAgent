package com.splunk.javaagent.jmx.transport;

import java.util.Map;

import com.splunk.javaagent.SplunkLogEvent;

/**
 * <pre>
 * 
 * This interface can be implemented to provide custom transport logic to send
 * data to Splunk
 * 
 * The custom implementation class can then be placed on the classpath and
 * declared in the configuration xml file.
 * 
 * </pre>
 * 
 * @author Damien Dallimore damien@dtdsoftware.com
 * 
 */
public interface Transport {

	/**
	 * Parameters that can be declared in the config xml file
	 * 
	 * @param parameters
	 *            map of key value pairs from the config xml file
	 */
	public void setParameters(Map<String, String> parameters);

	/**
	 * Implement this method with the logic to transport the event to Splunk
	 * 
	 * @param event
	 *            the event to be transported to Splunk
	 */
	public void transport(SplunkLogEvent event);

	/**
	 * Lifecycle method invoked after paramaters have been set
	 */
	public void open();

	/**
	 * Lifecycle method invoked when transport is no longer needed
	 */
	public void close();

}
