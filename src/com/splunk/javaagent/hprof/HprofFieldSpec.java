package com.splunk.javaagent.hprof;

public class HprofFieldSpec {

	HprofFieldSpec(byte ftype, String fname) {
		type = ftype;
		name = fname;
	}

	public String toString() {
		return (new StringBuilder()).append("FieldSpec{").append(type)
				.append(", ").append(name).append("}").toString();
	}

	byte type;
	String name;
}
