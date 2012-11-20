package com.splunk.javaagent.jmx.config;

import java.util.Map;

/**
 * Superclass for config POJOs that take parameters
 * 
 * @author Damien Dallimore damien@dtdsoftware.com
 * 
 */
public class ParameterizedConfig {

	private Map<String, String> parameters;

	public ParameterizedConfig() {
	}

	public String getParameter(String key) {
		if (parameters == null) {
			return "";
		} else
			return parameters.get(key);
	}

	public Map<String, String> getParameters() {

		return parameters;
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

}
