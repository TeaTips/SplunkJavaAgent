package com.splunk.javaagent.hprof;

import com.splunk.javaagent.SplunkLogEvent;

public class GCRootUnknownSubRecord extends HprofRecord {

	private HprofIDField id;

	@Override
	public void parseRecord() {
		this.id = readId();

	}

	@Override
	public SplunkLogEvent getSplunkLogEvent() {
		SplunkLogEvent event = new SplunkLogEvent("hprof_heapdump_unknown",
				"splunkagent", false, false);
		addCommonSplunkLogEventFields(event);
		event.addPair("id", this.id.toString());
		return event;
	}

}
