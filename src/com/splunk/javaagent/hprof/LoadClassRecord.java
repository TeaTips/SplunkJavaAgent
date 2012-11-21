package com.splunk.javaagent.hprof;

import com.splunk.javaagent.SplunkLogEvent;

public class LoadClassRecord extends HprofRecord {

	int classSerial;
	HprofIDField classObjectID;
	int stackTraceSerial;
	HprofIDField classNameID;

	@Override
	public void parseRecord() {

		this.classSerial = buf.getInt();
		this.classObjectID = readId();
		this.stackTraceSerial = buf.getInt();
		this.classNameID = readId();

		parent.classNameMap.put(classObjectID, classNameID);

	}

	@Override
	public SplunkLogEvent getSplunkLogEvent() {
		SplunkLogEvent event = new SplunkLogEvent("hprof_loadclass",
				"splunkagent", false, false);
		addCommonSplunkLogEventFields(event);
		event.addPair("classSerial", this.classSerial);
		event.addPair("classObjectID", this.classObjectID.toString());
		event.addPair("stackTraceSerial", this.stackTraceSerial);
		event.addPair("classNameID", this.classNameID.toString());

		return event;
	}

}
