package com.splunk.javaagent.trace;

public class FilterListItem {

	String className;
	String methodName;

	public FilterListItem() {
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

}
