package com.splunk.javaagent.hprof;

import com.splunk.javaagent.SplunkLogEvent;

public class GCRootThreadBlockSubRecord extends HprofRecord {

	private HprofIDField objectID;
	private int threadSerial;

	@Override
	public void parseRecord() {

		this.objectID = readId();
		this.threadSerial = buf.getInt();

	}

	@Override
	public SplunkLogEvent getSplunkLogEvent() {
		SplunkLogEvent event = new SplunkLogEvent("hprof_heapdump_threadblock",
				"splunkagent", false, false);
		addCommonSplunkLogEventFields(event);
		event.addPair("objectID", this.objectID.toString());
		event.addPair("threadSerial", this.threadSerial);

		return event;
	}

}
