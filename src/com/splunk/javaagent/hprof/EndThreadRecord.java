package com.splunk.javaagent.hprof;

import com.splunk.javaagent.SplunkLogEvent;

public class EndThreadRecord extends HprofRecord {

	private int threadSerial;

	@Override
	public void parseRecord() {
		this.threadSerial = buf.getInt();

	}

	@Override
	public SplunkLogEvent getSplunkLogEvent() {
		SplunkLogEvent event = new SplunkLogEvent("hprof_endthread",
				"splunkagent", false, false);
		addCommonSplunkLogEventFields(event);
		event.addPair("threadSerial", this.threadSerial);

		return event;
	}

}
