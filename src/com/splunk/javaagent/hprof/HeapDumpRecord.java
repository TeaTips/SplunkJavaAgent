package com.splunk.javaagent.hprof;

import com.splunk.javaagent.SplunkJavaAgent;
import com.splunk.javaagent.SplunkLogEvent;

public class HeapDumpRecord extends HprofRecord {

	@Override
	public void parseRecord() {

		int endposition = buf.position() + recordlength;

		while (buf.position() < endposition) {

			byte subRecordType = buf.get();
			HprofRecord subRecord = null;

			switch (subRecordType) {
			case RECORD_GC_ROOT_UNKNOWN:

				subRecord = parseGCRootUnknown();
				break;

			case RECORD_GC_ROOT_JNI_GLOBAL:

				subRecord = parseGCRootJNIGlobal();
				break;

			case RECORD_GC_ROOT_JNI_LOCAL:

				subRecord = parseGCRootJNILocal();
				break;

			case RECORD_GC_ROOT_JAVA_FRAME:

				subRecord = parseGCRootJavaFrame();
				break;

			case RECORD_GC_ROOT_NATIVE_STACK:

				subRecord = parseGCRootNativeStack();
				break;

			case RECORD_GC_ROOT_STICKY_CLASS:

				subRecord = parseGCRootStickyClass();
				break;

			case RECORD_GC_ROOT_THREAD_BLOCK:

				subRecord = parseGCRootThreadBlock();
				break;

			case RECORD_GC_ROOT_MONITOR_USED:

				subRecord = parseGCRootMonitorUsed();
				break;

			case RECORD_GC_ROOT_THREAD_OBJ:

				subRecord = parseGCRootThreadObject();
				break;

			case RECORD_GC_CLASS_DUMP:
				subRecord = parseGCCLassDump();
				break;

			case RECORD_GC_INSTANCE_DUMP:
				subRecord = parseGCInstanceDump();

				break;

			case RECORD_GC_OBJ_ARRAY_DUMP:
				subRecord = parseGCObjectArrayDump();

				break;

			case RECORD_GC_PRIM_ARRAY_DUMP:
				subRecord = parseGCPrimitiveArrayDump();

				break;

			default:

				break;
			}
			if (subRecord != null)
				SplunkJavaAgent.hprofRecordEvent(this.recordType,
						subRecord.recordType, subRecord.getSplunkLogEvent());

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

}
