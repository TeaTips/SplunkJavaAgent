package com.splunk.javaagent.hprof;

import com.splunk.javaagent.SplunkLogEvent;

public class GCRootMonitorUsedSubRecord extends HprofRecord {

private HprofIDField objectID;
	

	@Override
	public void parseRecord() {

		this.objectID = readId();
		
	}

	@Override
	public SplunkLogEvent getSplunkLogEvent() {
		SplunkLogEvent event = new SplunkLogEvent("hprof_heapdump_monitorused",
				"splunkagent", false, false);
		addCommonSplunkLogEventFields(event);
		event.addPair("objectID", this.objectID.toString());
		
		return event;
	}
	
}
