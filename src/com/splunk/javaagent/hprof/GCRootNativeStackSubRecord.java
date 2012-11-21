package com.splunk.javaagent.hprof;

import com.splunk.javaagent.SplunkLogEvent;

public class GCRootNativeStackSubRecord extends HprofRecord {

	private HprofIDField objectID;
	private int threadSerial;

	@Override
	public void parseRecord() {

		this.objectID = readId();
		this.threadSerial = buf.getInt();

	}

	@Override
	public SplunkLogEvent getSplunkLogEvent() {
		SplunkLogEvent event = new SplunkLogEvent("hprof_heapdump_nativestack",
				"splunkagent", false, false);
		addCommonSplunkLogEventFields(event);
		event.addPair("objectID", this.objectID.toString());
		event.addPair("threadSerial", this.threadSerial);

		return event;
	}

}
