package com.splunk.javaagent.hprof;

public class HprofIDField {

	private int size;
	private long value;

	public HprofIDField(int size, long value) {
		this.size = size;
		this.value = value;
	}

	public HprofIDField(long value) {

		this.value = value;
	}

	public long getValue() {
		if (size == 4)
			return value & 0xffffffffL;
		if (size == 8)
			return value;
		else
			return 0;
	}

	public String toHexString() {
		return Long.toHexString(getValue());
	}

	public String toString() {
		return toHexString();
	}

	public boolean equals(Object obj) {
		HprofIDField x = (HprofIDField) obj;
		return value == x.value;
	}

	public int hashCode() {
		return (int) value;
	}

}
