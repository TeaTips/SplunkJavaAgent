package com.splunk.javaagent.hprof;

import com.splunk.javaagent.SplunkLogEvent;

public class GCClassDumpSubRecord extends HprofRecord {

	@Override
	public void parseRecord() {
		// TODO Auto-generated method stub

	}

	@Override
	public SplunkLogEvent getSplunkLogEvent() {
		SplunkLogEvent event = new SplunkLogEvent("hprof_heapdump_classdump",
				"splunkagent", false, false);
		addCommonSplunkLogEventFields(event);

		return event;
	}

}
