package com.splunk.javaagent.hprof;

import com.splunk.javaagent.SplunkLogEvent;

public class StartThreadRecord extends HprofRecord {

	private int threadSerial;
	private int stackTraceSerial;
	private HprofIDField threadObjectID;
	private HprofIDField threadNameID;
	private HprofIDField threadGroupNameID;
	private HprofIDField threadGroupParentID;

	@Override
	public void parseRecord() {

		this.threadSerial = buf.getInt();
		this.threadObjectID = readId();
		this.stackTraceSerial = buf.getInt();
		this.threadNameID = readId();
		this.threadGroupNameID = readId();
		this.threadGroupParentID = readId();

	}

	@Override
	public SplunkLogEvent getSplunkLogEvent() {
		SplunkLogEvent event = new SplunkLogEvent("hprof_startthread",
				"splunkagent", false, false);
		addCommonSplunkLogEventFields(event);
		event.addPair("threadSerial", this.threadSerial);
		event.addPair("threadObjectID", this.threadObjectID.toString());
		event.addPair("stackTraceSerial", this.stackTraceSerial);
		event.addPair("threadNameID", this.threadNameID.toString());
		event.addPair("threadGroupNameID", this.threadGroupNameID.toString());
		event.addPair("threadGroupParentID",
				this.threadGroupParentID.toString());

		return event;
	}

}
