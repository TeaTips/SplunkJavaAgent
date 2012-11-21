package com.splunk.javaagent.hprof;

public class AllocSite {

	private byte type;
	private int classSerial;
	private int stackTraceSerial;
	private int numberOfBytesAlive;
	private int numberOfInstancesAlive;
	private int numberOfBytesAllocated;
	private int numberOfInstancesAllocated;

	public String toString() {

		StringBuffer sb = new StringBuffer();
		sb.append("type").append("=").append(getTypeString()).append(":");
		sb.append("classSerial").append("=").append(classSerial).append(":");
		sb.append("stackTraceSerial").append("=").append(stackTraceSerial)
				.append(":");
		sb.append("numberOfBytesAlive").append("=").append(numberOfBytesAlive)
				.append(":");
		sb.append("numberOfInstancesAlive").append("=")
				.append(numberOfInstancesAlive).append(":");
		sb.append("numberOfBytesAllocated").append("=")
				.append(numberOfBytesAllocated).append(":");
		sb.append("numberOfInstancesAllocated").append("=")
				.append(numberOfInstancesAllocated);

		return sb.toString();

	}

	public byte getType() {
		return type;
	}

	public String getTypeString() {

		switch (type) {

		case 0:
			return "normal object";
		case 2:
			return "object array";
		case 4:
			return "boolean array";
		case 5:
			return "char array";
		case 6:
			return "float array";
		case 7:
			return "double array";
		case 8:
			return "byte array";
		case 9:
			return "short array";
		case 10:
			return "int array";
		case 11:
			return "long array";
		default:
			return "unknown";
		}
	}

	public void setType(byte type) {
		this.type = type;
	}

	public int getClassSerial() {
		return classSerial;
	}

	public void setClassSerial(int classSerial) {
		this.classSerial = classSerial;
	}

	public int getStackTraceSerial() {
		return stackTraceSerial;
	}

	public void setStackTraceSerial(int stackTraceSerial) {
		this.stackTraceSerial = stackTraceSerial;
	}

	public int getNumberOfBytesAlive() {
		return numberOfBytesAlive;
	}

	public void setNumberOfBytesAlive(int numberOfBytesAlive) {
		this.numberOfBytesAlive = numberOfBytesAlive;
	}

	public int getNumberOfInstancesAlive() {
		return numberOfInstancesAlive;
	}

	public void setNumberOfInstancesAlive(int numberOfInstancesAlive) {
		this.numberOfInstancesAlive = numberOfInstancesAlive;
	}

	public int getNumberOfBytesAllocated() {
		return numberOfBytesAllocated;
	}

	public void setNumberOfBytesAllocated(int numberOfBytesAllocated) {
		this.numberOfBytesAllocated = numberOfBytesAllocated;
	}

	public int getNumberOfInstancesAllocated() {
		return numberOfInstancesAllocated;
	}

	public void setNumberOfInstancesAllocated(int numberOfInstancesAllocated) {
		this.numberOfInstancesAllocated = numberOfInstancesAllocated;
	}

}
