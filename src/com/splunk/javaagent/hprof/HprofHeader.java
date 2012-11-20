package com.splunk.javaagent.hprof;

import java.nio.MappedByteBuffer;

public class HprofHeader {

	private int identifierSize;
	private long timestampMillis;
	private String headerText;

	public HprofHeader(MappedByteBuffer buf) {

		byte headerBytes[] = new byte[19];
		buf.get(headerBytes);
		this.headerText = new String(headerBytes);
		this.identifierSize = buf.getInt();
		this.timestampMillis = buf.getLong();
	}

	public String getHeaderText() {
		return headerText;
	}

	public int getIdentifierSize() {
		return identifierSize;
	}

	public long getTimestampMillis() {
		return timestampMillis;
	}

}
