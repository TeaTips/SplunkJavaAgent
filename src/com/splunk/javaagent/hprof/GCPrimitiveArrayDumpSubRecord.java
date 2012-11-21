package com.splunk.javaagent.hprof;

import com.splunk.javaagent.SplunkLogEvent;

public class GCPrimitiveArrayDumpSubRecord extends HprofRecord {

	private HprofIDField objectID;
	private int stackTraceSerial;
	private int numberOfElements;
	private byte elementType;
	private String elementTypeString;
	private int size;

	@Override
	public void parseRecord() {

		this.objectID = readId();
		this.stackTraceSerial = buf.getInt();
		this.numberOfElements = buf.getInt();
		this.elementType = buf.get();

		switch (this.elementType) {
		case TYPE_BOOLEAN:
			this.elementTypeString = "boolean";
			this.size = this.numberOfElements;
			break;

		case TYPE_CHAR:
			this.elementTypeString = "char";
			this.size = this.numberOfElements * 2;
			break;

		case TYPE_FLOAT:
			this.elementTypeString = "float";
			this.size = this.numberOfElements * 4;
			break;

		case TYPE_DOUBLE:
			this.elementTypeString = "double";
			this.size = this.numberOfElements * 8;
			break;

		case TYPE_BYTE:
			this.elementTypeString = "byte";
			this.size = this.numberOfElements;
			break;

		case TYPE_SHORT:
			this.elementTypeString = "short";
			this.size = this.numberOfElements * 2;
			break;

		case TYPE_INT:
			this.elementTypeString = "int";
			this.size = this.numberOfElements * 4;
			break;

		case TYPE_LONG:
			this.elementTypeString = "long";
			this.size = this.numberOfElements * 8;
			break;

		default:
			break;
		}
		byte values[] = new byte[this.size];
		buf.get(values);

		if (includeHeaderSize)
			this.size += parent.getHeader().getIdentifierSize() * 2 + 4;

	}

	@Override
	public SplunkLogEvent getSplunkLogEvent() {
		SplunkLogEvent event = new SplunkLogEvent(
				"hprof_heapdump_primitivearraydump", "splunkagent", false,
				false);
		addCommonSplunkLogEventFields(event);
		event.addPair("objectID", this.objectID.toString());
		event.addPair("stackTraceSerial", this.stackTraceSerial);
		event.addPair("numberOfElements", this.numberOfElements);
		event.addPair("elementTypeString", this.elementTypeString);
		event.addPair("size", this.size);

		return event;
	}

}
