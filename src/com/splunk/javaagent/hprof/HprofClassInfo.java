package com.splunk.javaagent.hprof;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HprofClassInfo {

	public HprofClassInfo(long superid, int size) {
		this.superid = superid;
		this.size = size;
	}

	public void addFieldSpec(HprofFieldSpec fs) {
		if (fieldSpec == null)
			fieldSpec = new ArrayList();
		fieldSpec.add(fs);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append((new StringBuilder()).append("ClassInfo {")
				.append(Long.toHexString(superid)).append(", ").append(size)
				.append(", ").toString());
		if (fieldSpec != null) {
			Iterator i = fieldSpec.iterator();
			do {
				if (!i.hasNext())
					break;
				sb.append(i.next().toString());
				if (i.hasNext())
					sb.append(", ");
			} while (true);
		}
		sb.append("}");
		return sb.toString();
	}

	long superid;
	int size;
	List fieldSpec;
}
