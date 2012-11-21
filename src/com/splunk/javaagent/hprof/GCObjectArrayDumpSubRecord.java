package com.splunk.javaagent.hprof;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.splunk.javaagent.SplunkLogEvent;

public class GCObjectArrayDumpSubRecord extends HprofRecord {

	private HprofIDField objectID;
	private int stackTraceSerial;
	private int numberOfElements;
	private HprofIDField arrayClassID;
	private int size;
	private String className;
	private Map<String, String> fields;
	
	@Override
	public void parseRecord() {
		
		
		this.objectID = readId();
		this.stackTraceSerial = buf.getInt();
		this.numberOfElements = buf.getInt();
		this.arrayClassID = readId();
		
		int idSize = parent.getHeader().getIdentifierSize();
		
		this.size = (4 * idSize) + (idSize * numberOfElements);
		this.className = getNameForClassId(arrayClassID);
		
		this.fields = new HashMap<String, String>();
		
		if (includeHeaderSize)
			this.size += idSize * 4;

		for (int i = 0; i < this.numberOfElements; i++) {
			HprofIDField val = readId();
			if (val.getValue() != 0L)
				this.fields.put("VAL" + i, val.toString());

		}
	}

	@Override
	public SplunkLogEvent getSplunkLogEvent() {
		SplunkLogEvent event = new SplunkLogEvent(
				"hprof_heapdump_objectarraydump", "splunkagent", false, false);
		addCommonSplunkLogEventFields(event);
		event.addPair("objectID", this.objectID.toString());
		event.addPair("stackTraceSerial", this.stackTraceSerial);
		event.addPair("numberOfElements", this.numberOfElements);
		event.addPair("arrayClassID", this.arrayClassID.toString());
		event.addPair("size", this.size);
		event.addPair("className", this.className);
		Set<String> keys = fields.keySet();
		for (String key : keys) {
			event.addPair(key, fields.get(key));
		}
		return event;
	}

}
