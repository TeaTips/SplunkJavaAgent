package com.splunk.javaagent.hprof;

import com.splunk.javaagent.SplunkLogEvent;

public class GCRootThreadObjectSubRecord extends HprofRecord {

	private HprofIDField id;
	private int threadSequenceNumber;
	private int stackTraceSequenceNumber;

	@Override
	public void parseRecord() {

		this.id = readId();
		this.threadSequenceNumber = buf.getInt();
		this.stackTraceSequenceNumber = buf.getInt();
	}

	@Override
	public SplunkLogEvent getSplunkLogEvent() {
		SplunkLogEvent event = new SplunkLogEvent(
				"hprof_heapdump_threadobject", "splunkagent", false, false);
		addCommonSplunkLogEventFields(event);
		event.addPair("id", this.id.toString());
		event.addPair("threadSequenceNumber", this.threadSequenceNumber);
		event.addPair("stackTraceSequenceNumber", this.stackTraceSequenceNumber);
		return event;
	}

}
