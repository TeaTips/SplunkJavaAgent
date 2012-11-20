package com.splunk.javaagent.jmx.config;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * POJO for an MBean attribute
 * 
 * @author Damien Dallimore damien@dtdsoftware.com
 * 
 */
public class Attribute {

	// Attribute name. Deep attributes can be delimited with a ":"
	// Example , A:B:C
	public String name;

	// name that should be written out to SPLUNK
	public String outputname;

	private List<String> tokens;

	private static final String ATTRIBUTE_DELIMITER = ":";

	public Attribute() {
	}

	public String getName() {
		return name;
	}

	/**
	 * Sets name and breaks up ":" delimited parts of the attribute name
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
		this.tokens = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(this.name, ATTRIBUTE_DELIMITER);
		while (st.hasMoreTokens()) {
			this.tokens.add(st.nextToken());
		}

	}

	public String getOutputname() {
		return outputname;
	}

	public void setOutputname(String outputname) {
		this.outputname = outputname;
	}

	public List<String> getTokens() {
		return tokens;
	}

}
