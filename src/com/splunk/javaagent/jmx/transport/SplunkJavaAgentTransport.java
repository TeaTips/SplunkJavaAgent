package com.splunk.javaagent.jmx.transport;

import java.util.Map;

import com.splunk.javaagent.SplunkJavaAgent;
import com.splunk.javaagent.SplunkLogEvent;

/**
 * <pre>
 * Default transport.
 * 
 * 
 * </pre>
 * 
 * @author Damien Dallimore damien@dtdsoftware.com
 * 
 */
public class SplunkJavaAgentTransport implements Transport {

	public SplunkJavaAgentTransport() {
	}

	@Override
	/**
	 * this transport doesn't use params
	 */
	public void setParameters(Map<String, String> parameters) {
		// do nothing
	}

	@Override
	public void transport(SplunkLogEvent event) {

		SplunkJavaAgent.jmxEvent(event);

	}

	@Override
	public void close() {
		// do nothing

	}

	@Override
	public void open() {
		// do nothing

	}

}
