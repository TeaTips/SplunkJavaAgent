package com.splunk.javaagent.hprof;

import com.splunk.javaagent.SplunkLogEvent;

public class GCRootJNIGlobalSubRecord extends HprofRecord {

	private HprofIDField objectID;
	private HprofIDField jniRefID;

	@Override
	public void parseRecord() {

		this.objectID = readId();
		this.jniRefID = readId();

	}

	@Override
	public SplunkLogEvent getSplunkLogEvent() {
		SplunkLogEvent event = new SplunkLogEvent("hprof_heapdump_jniglobal",
				"splunkagent", false, false);
		addCommonSplunkLogEventFields(event);
		event.addPair("objectID", this.objectID.toString());
		event.addPair("jniRefID", this.jniRefID.toString());
		return event;
	}

}
