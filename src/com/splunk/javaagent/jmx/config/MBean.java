package com.splunk.javaagent.jmx.config;

import java.util.List;

/**
 * POJO for an MBean
 * 
 * For MBean definitions , standard JMX object name wildcard patterns * and ?
 * supported for the domain and properties string attributes
 * http://download.oracle
 * .com/javase/1,5.0/docs/api/javax/management/ObjectName.html
 * 
 * 
 * @author Damien Dallimore damien@dtdsoftware.com
 * 
 */
public class MBean {

	// MBean domain literal string or pattern
	public String domain = "";

	// MBean properties list string or pattern in "key=value, key2=value2"
	// format
	public String propertiesList = "";

	// if true, will dump all of the attributes for the MBean
	public boolean dumpAllAttributes;

	public List<Attribute> attributes;

	public Notification notification;

	public List<Operation> operations;

	public MBean() {
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getPropertiesList() {
		return propertiesList;
	}

	public void setPropertiesList(String propertiesList) {
		this.propertiesList = propertiesList;
	}

	public List<Attribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<Attribute> attributes) {
		this.attributes = attributes;
	}

	public Notification getNotification() {
		return notification;
	}

	public void setNotification(Notification notification) {
		this.notification = notification;
	}

	public List<Operation> getOperations() {
		return operations;
	}

	public void setOperations(List<Operation> operations) {
		this.operations = operations;
	}

	public boolean isDumpAllAttributes() {
		return dumpAllAttributes;
	}

	public void setDumpAllAttributes(boolean dumpAllAttributes) {
		this.dumpAllAttributes = dumpAllAttributes;
	}

}
