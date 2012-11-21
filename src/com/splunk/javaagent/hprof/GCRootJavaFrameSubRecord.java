package com.splunk.javaagent.hprof;

import com.splunk.javaagent.SplunkLogEvent;

public class GCRootJavaFrameSubRecord extends HprofRecord {

	private HprofIDField objectID;
	private int threadSerial;
	private int frameNumber;

	@Override
	public void parseRecord() {

		this.objectID = readId();
		this.threadSerial = buf.getInt();
		this.frameNumber = buf.getInt();
	}

	@Override
	public SplunkLogEvent getSplunkLogEvent() {
		SplunkLogEvent event = new SplunkLogEvent("hprof_heapdump_javaframe",
				"splunkagent", false, false);
		addCommonSplunkLogEventFields(event);
		event.addPair("objectID", this.objectID.toString());
		event.addPair("threadSerial", this.threadSerial);
		event.addPair("frameNumber", this.frameNumber);
		return event;
	}

}
