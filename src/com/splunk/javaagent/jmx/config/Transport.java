package com.splunk.javaagent.jmx.config;

import java.util.HashMap;
import java.util.Map;

/**
 * POJO for Transport config
 * 
 * @author Damien Dallimore damien@dtdsoftware.com
 * 
 */
public class Transport extends ParameterizedConfig {

	// default transport implementation
	public final static String DEFAULT = "com.splunk.javaagent.jmx.transport.SplunkJavaAgentTransport";

	// class name of the transport implementation
	public String className = DEFAULT;

	public Transport() {
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	/**
	 * Obtain a transport instance via reflection
	 * 
	 * @return
	 * @throws Exception
	 */
	public com.splunk.javaagent.jmx.transport.Transport getTransportInstance()
			throws Exception {

		com.splunk.javaagent.jmx.transport.Transport obj = (com.splunk.javaagent.jmx.transport.Transport) Class
				.forName(className).newInstance();
		Map<String, String> parameters = this.getParameters();
		if (parameters == null)
			parameters = new HashMap<String, String>();

		obj.setParameters(parameters);
		obj.open();
		return obj;
	}

}
