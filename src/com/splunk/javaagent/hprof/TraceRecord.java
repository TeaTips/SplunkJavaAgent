package com.splunk.javaagent.hprof;

import java.util.ArrayList;
import java.util.List;

import com.splunk.javaagent.SplunkLogEvent;

public class TraceRecord extends HprofRecord {

	private int stackTraceSerialNumber;
	private int threadSerialNumber;
	private int numberOfFrames;
	private List <HprofIDField> stackFrameIDs;
	
	@Override
	public void parseRecord() {
		
		this.stackTraceSerialNumber = buf.getInt();
		this.threadSerialNumber = buf.getInt();
		this.numberOfFrames = buf.getInt();
		this.stackFrameIDs = new ArrayList<HprofIDField>();		
		int idsLength = recordlength -12;
		
		while(idsLength > 0) {
			try {
				
				stackFrameIDs.add(readId());
				idsLength -= parent.getHeader().getIdentifierSize();
				
			} catch (Throwable t) {
			}
		}
		
	}

	@Override
	public SplunkLogEvent getSplunkLogEvent() {
		SplunkLogEvent event = new SplunkLogEvent("hprof_trace", "splunkagent",
				false, false);
		addCommonSplunkLogEventFields(event);
		
		event.addPair("stackTraceSerialNumber", this.stackTraceSerialNumber);
		event.addPair("threadSerialNumber", this.threadSerialNumber);
		event.addPair("numberOfFrames", this.numberOfFrames);
		event.addPair("stackFrameIDs", toSplunkMVString(stackFrameIDs));
		
		return event;
	}

	

}
