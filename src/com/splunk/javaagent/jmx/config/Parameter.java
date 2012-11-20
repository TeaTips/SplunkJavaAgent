package com.splunk.javaagent.jmx.config;

/**
 * POJO for an MBean operation parameter
 * 
 * @author Damien Dallimore damien@dtdsoftware.com
 * 
 */
public class Parameter {

	// parameter value
	public String value;

	// parameter type
	// int,float,double,long,short,byte,boolean,char,string
	public String type;

	public static final String INT = "int";
	public static final String FLOAT = "float";
	public static final String DOUBLE = "double";
	public static final String LONG = "long";
	public static final String SHORT = "short";
	public static final String BYTE = "byte";
	public static final String BOOLEAN = "boolean";
	public static final String CHAR = "char";
	public static final String STRING = "string";

	public Parameter() {
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
