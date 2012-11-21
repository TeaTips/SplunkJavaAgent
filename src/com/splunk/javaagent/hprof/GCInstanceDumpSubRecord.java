package com.splunk.javaagent.hprof;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.splunk.javaagent.SplunkLogEvent;

public class GCInstanceDumpSubRecord extends HprofRecord {

	private HprofIDField objectID;
	private int stackTraceSerial;
	private HprofIDField classObjectID;
	private int size;
	private String className;
	private Map<String, String> fields;

	@Override
	public void parseRecord() {

		this.objectID = readId();
		this.stackTraceSerial = buf.getInt();
		this.classObjectID = readId();
		int numberOfBytes = buf.getInt();
		byte values[] = new byte[numberOfBytes];
		buf.get(values);

		this.className = getNameForClassId(classObjectID);
		HprofClassInfo classInfo = (HprofClassInfo) parent.classMap
				.get(classObjectID);
		if (classInfo != null)
			this.size = classInfo.size;
		if (includeHeaderSize)
			this.size += parent.getHeader().getIdentifierSize() * 2;
		this.fields = new HashMap<String, String>();

		HprofClassInfo ci;
		for (HprofIDField cid = classObjectID; cid.getValue() != 0L; cid = new HprofIDField(
				ci.superid)) {
			ci = (HprofClassInfo) parent.classMap.get(cid);
			if (ci == null) {
				break;
			}
			if (ci.fieldSpec != null) {
				int i = 0;
				while (true) {
					if (i >= ci.fieldSpec.size())
						break;
					HprofFieldSpec fs = (HprofFieldSpec) ci.fieldSpec.get(i);

					switch (fs.type) {
					case TYPE_OBJECT:
						HprofIDField val = readId();
						if (val.getValue() != 0L)
							this.fields.put(fs.name, val.toString());
						break;

					case TYPE_BOOLEAN:
						buf.get();
						break;

					case TYPE_CHAR:
						buf.getChar();
						break;

					case TYPE_FLOAT:
						buf.getFloat();
						break;

					case TYPE_DOUBLE:
						buf.getDouble();
						break;

					case TYPE_BYTE:
						buf.get();
						break;

					case TYPE_SHORT:
						buf.getShort();
						break;

					case TYPE_INT:
						buf.getInt();
						break;

					case TYPE_LONG:
						buf.getLong();
						break;

					default:
						break;
					}
					i++;
				}
			}

		}

	}

	@Override
	public SplunkLogEvent getSplunkLogEvent() {
		SplunkLogEvent event = new SplunkLogEvent(
				"hprof_heapdump_instancedump", "splunkagent", false, false);
		addCommonSplunkLogEventFields(event);
		event.addPair("objectID", this.objectID.toString());
		event.addPair("stackTraceSerial", this.stackTraceSerial);
		event.addPair("classObjectID", this.classObjectID.toString());
		event.addPair("size", this.size);
		event.addPair("className", this.className);
		Set<String> keys = fields.keySet();
		for (String key : keys) {
			event.addPair(key, fields.get(key));
		}

		return event;
	}

}
