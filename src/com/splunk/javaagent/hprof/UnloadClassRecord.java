package com.splunk.javaagent.hprof;

import com.splunk.javaagent.SplunkLogEvent;

public class UnloadClassRecord extends HprofRecord {

	int classSerial;
	
	
	@Override
	public void parseRecord() {
		
		this.classSerial = buf.getInt();
		

	}

	@Override
	public SplunkLogEvent getSplunkLogEvent() {
		SplunkLogEvent event = new SplunkLogEvent("hprof_unloadclass", "splunkagent",
				false, false);
		addCommonSplunkLogEventFields(event);
		event.addPair("classSerial", this.classSerial);
		
		
		return event;
	}


}
