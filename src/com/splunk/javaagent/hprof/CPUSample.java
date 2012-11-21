package com.splunk.javaagent.hprof;

public class CPUSample {

	private int numberOfSamples;
	private int stackTraceSerial;

	public String toString() {

		StringBuffer sb = new StringBuffer();
		sb.append("numberOfSamples").append("=").append(numberOfSamples)
				.append(":");
		sb.append("classSerial").append("=").append(stackTraceSerial);

		return sb.toString();

	}

	public int getNumberOfSamples() {
		return numberOfSamples;
	}

	public void setNumberOfSamples(int numberOfSamples) {
		this.numberOfSamples = numberOfSamples;
	}

	public int getStackTraceSerial() {
		return stackTraceSerial;
	}

	public void setStackTraceSerial(int stackTraceSerial) {
		this.stackTraceSerial = stackTraceSerial;
	}

}
