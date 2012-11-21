package com.splunk.javaagent.hprof;

import java.util.ArrayList;
import java.util.List;

import com.splunk.javaagent.SplunkLogEvent;

public class HeapDumpRecord extends HprofRecord {

	private List<HprofRecord> subRecords;

	@Override
	public void parseRecord() {

		this.subRecords = new ArrayList<HprofRecord>();

		int endposition = buf.position() + recordlength;

		while (buf.position() < endposition) {

			byte subRecordType = buf.get();

			switch (subRecordType) {
			case RECORD_GC_ROOT_UNKNOWN:

				this.subRecords.add(parseGCRootUnknown());
				break;

			case RECORD_GC_ROOT_JNI_GLOBAL:

				this.subRecords.add(parseGCRootJNIGlobal());
				break;

			case RECORD_GC_ROOT_JNI_LOCAL:

				this.subRecords.add(parseGCRootJNILocal());
				break;

			case RECORD_GC_ROOT_JAVA_FRAME:

				this.subRecords.add(parseGCRootJavaFrame());
				break;

			case RECORD_GC_ROOT_NATIVE_STACK:

				this.subRecords.add(parseGCRootNativeStack());
				break;

			case RECORD_GC_ROOT_STICKY_CLASS:

				this.subRecords.add(parseGCRootStickyClass());
				break;

			case RECORD_GC_ROOT_THREAD_BLOCK:

				this.subRecords.add(parseGCRootThreadBlock());
				break;

			case RECORD_GC_ROOT_MONITOR_USED:

				this.subRecords.add(parseGCRootMonitorUsed());
				break;

			case RECORD_GC_ROOT_THREAD_OBJ:

				this.subRecords.add(parseGCRootThreadObject());
				break;

			case RECORD_GC_CLASS_DUMP:
				this.subRecords.add(parseGCCLassDump());
				break;

			case RECORD_GC_INSTANCE_DUMP:
				this.subRecords.add(parseGCInstanceDump());

				break;

			case RECORD_GC_OBJ_ARRAY_DUMP:
				this.subRecords.add(parseGCObjectArrayDump());

				break;

			case RECORD_GC_PRIM_ARRAY_DUMP:
				this.subRecords.add(parseGCPrimitiveArrayDump());

				break;

			default:

				break;
			}

		}

	}

	/**
	 * Sub record inherit some traits from main record
	 * 
	 * @param record
	 */
	private void process(HprofRecord record, byte subrecordType) {
		if (record != null) {
			record.parent = this.parent;
			record.timeMicrosecondsSinceHeaderTimeStamp = this.timeMicrosecondsSinceHeaderTimeStamp;
			record.recordType = subrecordType;
			record.buf = this.buf;
			record.parseRecord();
		}

	}

	private HprofRecord parseGCPrimitiveArrayDump() {
		GCPrimitiveArrayDumpSubRecord record = new GCPrimitiveArrayDumpSubRecord();
		process(record, RECORD_GC_PRIM_ARRAY_DUMP);

		return record;

	}

	private HprofRecord parseGCObjectArrayDump() {
		GCObjectArrayDumpSubRecord record = new GCObjectArrayDumpSubRecord();
		process(record, RECORD_GC_OBJ_ARRAY_DUMP);

		return record;

	}

	private HprofRecord parseGCInstanceDump() {
		GCInstanceDumpSubRecord record = new GCInstanceDumpSubRecord();
		process(record, RECORD_GC_INSTANCE_DUMP);

		return record;

	}

	private HprofRecord parseGCCLassDump() {
		GCClassDumpSubRecord record = new GCClassDumpSubRecord();
		process(record, RECORD_GC_CLASS_DUMP);

		return record;

	}

	private HprofRecord parseGCRootThreadObject() {
		GCRootThreadObjectSubRecord record = new GCRootThreadObjectSubRecord();
		process(record, RECORD_GC_ROOT_THREAD_OBJ);

		return record;

	}

	private HprofRecord parseGCRootMonitorUsed() {
		GCRootMonitorUsedSubRecord record = new GCRootMonitorUsedSubRecord();
		process(record, RECORD_GC_ROOT_MONITOR_USED);

		return record;

	}

	private HprofRecord parseGCRootThreadBlock() {
		GCRootThreadBlockSubRecord record = new GCRootThreadBlockSubRecord();
		process(record, RECORD_GC_ROOT_THREAD_BLOCK);

		return record;

	}

	private HprofRecord parseGCRootStickyClass() {
		GCRootStickyClassSubRecord record = new GCRootStickyClassSubRecord();
		process(record, RECORD_GC_ROOT_STICKY_CLASS);

		return record;

	}

	private HprofRecord parseGCRootNativeStack() {
		GCRootNativeStackSubRecord record = new GCRootNativeStackSubRecord();
		process(record, RECORD_GC_ROOT_NATIVE_STACK);

		return record;

	}

	private HprofRecord parseGCRootJavaFrame() {
		GCRootJavaFrameSubRecord record = new GCRootJavaFrameSubRecord();
		process(record, RECORD_GC_ROOT_JAVA_FRAME);

		return record;

	}

	private HprofRecord parseGCRootJNILocal() {
		GCRootJNILocalSubRecord record = new GCRootJNILocalSubRecord();
		process(record, RECORD_GC_ROOT_JNI_LOCAL);

		return record;

	}

	private HprofRecord parseGCRootJNIGlobal() {
		GCRootJNIGlobalSubRecord record = new GCRootJNIGlobalSubRecord();
		process(record, RECORD_GC_ROOT_JNI_GLOBAL);

		return record;

	}

	private HprofRecord parseGCRootUnknown() {
		GCRootUnknownSubRecord record = new GCRootUnknownSubRecord();
		process(record, RECORD_GC_ROOT_UNKNOWN);

		return record;

	}

	@Override
	public SplunkLogEvent getSplunkLogEvent() {
		SplunkLogEvent event = new SplunkLogEvent("hprof_heapdump",
				"splunkagent", false, false);
		addCommonSplunkLogEventFields(event);

		return event;
	}

	public List<HprofRecord> getSubRecords() {
		return subRecords;
	}

}
