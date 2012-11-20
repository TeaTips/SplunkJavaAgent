package com.splunk.javaagent.jmx.config;

import java.util.HashMap;
import java.util.Map;

/**
 * POJO for Formatter config
 * 
 * @author Damien Dallimore damien@dtdsoftware.com
 * 
 */
public class Formatter extends ParameterizedConfig {

	// default formatter implementation
	public final static String DEFAULT = "com.splunk.javaagent.jmx.formatter.TokenizedMBeanNameQuotesStrippedFormatter";

	// class name of the Formatter implementation
	public String className = DEFAULT;

	public Formatter() {
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	/**
	 * Obtain a formatter instance via reflection
	 * 
	 * @return
	 * @throws Exception
	 */
	public com.splunk.javaagent.jmx.formatter.Formatter getFormatterInstance()
			throws Exception {

		com.splunk.javaagent.jmx.formatter.Formatter obj = (com.splunk.javaagent.jmx.formatter.Formatter) Class
				.forName(className).newInstance();

		Map<String, String> parameters = this.getParameters();
		if (parameters == null)
			parameters = new HashMap<String, String>();

		obj.setParameters(parameters);
		return obj;
	}

}
