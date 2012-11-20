package com.splunk.javaagent.hprof;

import com.splunk.javaagent.SplunkLogEvent;

public class ControlSettingsRecord extends HprofRecord {

	private int settings;
	private short stackTraceDepth;
	
	@Override
	public void parseRecord() {
		
		this.settings = buf.getInt();
		this.stackTraceDepth = buf.getShort();
		

	}

	@Override
	public SplunkLogEvent getSplunkLogEvent() {
		SplunkLogEvent event = new SplunkLogEvent("hprof_controlsettings", "splunkagent",
				false, false);
		addCommonSplunkLogEventFields(event);
		event.addPair("settings", this.settings);
		event.addPair("stackTraceDepth", this.stackTraceDepth);
		
		
		
		return event;
	}

}
