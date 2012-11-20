package com.splunk.javaagent.hprof;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HprofClassInfo {

	public HprofClassInfo(long superid, int isize) {
		this.superid = superid;
		this.isize = isize;
	}

	public void addFieldSpec(HprofFieldSpec fs) {
		if (fieldSpec == null)
			fieldSpec = new ArrayList();
		fieldSpec.add(fs);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append((new StringBuilder()).append("ClassInfo {")
				.append(Long.toHexString(superid)).append(", ").append(isize)
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
	int isize;
	List fieldSpec;
}
