package com.splunk.javaagent.hprof;

import com.splunk.javaagent.SplunkLogEvent;

public class FrameRecord extends HprofRecord {

	private HprofIDField stackFrameID;
	private HprofIDField methodNameID;
	private HprofIDField methodSignatureID;
	private HprofIDField sourceFileNameID;
	private int classSerial;
	private int lineNumber;
	
	@Override
	public void parseRecord() {
				
		this.stackFrameID = readId();
		this.methodNameID = readId();
		this.methodSignatureID = readId();
		this.sourceFileNameID = readId();
		this.classSerial = buf.getInt();
		this.lineNumber = buf.getInt();

	}

	@Override
	public SplunkLogEvent getSplunkLogEvent() {
		SplunkLogEvent event = new SplunkLogEvent("hprof_frame", "splunkagent",
				false, false);
		addCommonSplunkLogEventFields(event);
		event.addPair("stackFrameID", this.stackFrameID.toString());
		event.addPair("methodNameID", this.methodNameID.toString());
		event.addPair("methodSignatureID", this.methodSignatureID.toString());
		event.addPair("sourceFileNameID", this.sourceFileNameID.toString());
		event.addPair("classSerial", this.classSerial);
		event.addPair("lineNumber", this.lineNumber);
		
		return event;
	}

}
