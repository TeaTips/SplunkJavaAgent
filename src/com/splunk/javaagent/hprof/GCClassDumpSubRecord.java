package com.splunk.javaagent.hprof;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.splunk.javaagent.SplunkLogEvent;

public class GCClassDumpSubRecord extends HprofRecord {

	private HprofIDField classObjectID;
	private int stackTraceSerial;
	private HprofIDField superClassObjectID;
	private HprofIDField classLoaderObjectID;
	private HprofIDField signersObjectID;
	private HprofIDField protectionDomainObjectID;
	private int instanceSize;
	private short sizeConstantPool;
	private String className;
	private Map<String, String> staticFields;

	@Override
	public void parseRecord() {

		this.classObjectID = readId();
		this.stackTraceSerial = buf.getInt();
		this.superClassObjectID = readId();
		this.classLoaderObjectID = readId();
		this.signersObjectID = readId();
		this.protectionDomainObjectID = readId();
		readId();
		readId();
		this.instanceSize = buf.getInt();
		this.sizeConstantPool = buf.getShort();
		this.className = getNameForClassId(this.classObjectID);

		HprofClassInfo cci = new HprofClassInfo(
				this.superClassObjectID.getValue(), instanceSize);
		parent.classMap.put(this.classObjectID, cci);

		int constantPoolIndex = buf.getShort();
		int constantPoolType = buf.get();
		for (int i = 0; i < this.sizeConstantPool; i++) {
			switch (constantPoolType) {
			case TYPE_OBJECT:
				readId();
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
		}

		int numberStaticFields = buf.getShort();
		this.staticFields = new HashMap<String, String>();
		for (int i = 0; i < numberStaticFields; i++) {

			String staticFieldName = (String) parent.nameMap.get(readId());
			byte type = buf.get();
			switch (type) {
			case TYPE_OBJECT:
				HprofIDField staticFieldID = readId();

				if (staticFieldID.getValue() != 0L)

					staticFields.put("STATIC_" + staticFieldName,
							staticFieldID.toString());
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
		}

		int numberInstanceFields = buf.getShort();
		for (int i = 0; i < numberInstanceFields; i++) {

			String instanceFieldName = (String) parent.nameMap.get(readId());
			byte fieldType = buf.get();

			cci.addFieldSpec(new HprofFieldSpec(fieldType, instanceFieldName));
		}

	}

	@Override
	public SplunkLogEvent getSplunkLogEvent() {
		SplunkLogEvent event = new SplunkLogEvent("hprof_heapdump_classdump",
				"splunkagent", false, false);
		addCommonSplunkLogEventFields(event);
		event.addPair("classObjectID", this.classObjectID.toString());
		event.addPair("className", this.className);
		event.addPair("stackTraceSerial", this.stackTraceSerial);
		if (superClassObjectID.getValue() != 0L)
			event.addPair("superClassObjectID",
					this.superClassObjectID.toString());
		if (classLoaderObjectID.getValue() != 0L)
			event.addPair("classLoaderObjectID",
					this.classLoaderObjectID.toString());
		if (signersObjectID.getValue() != 0L)
			event.addPair("signersObjectID", this.signersObjectID.toString());
		if (protectionDomainObjectID.getValue() != 0L)
			event.addPair("protectionDomainObjectID",
					this.protectionDomainObjectID.toString());
		Set<String> keys = staticFields.keySet();
		for (String key : keys) {
			event.addPair(key, staticFields.get(key));
		}
		return event;
	}

}
