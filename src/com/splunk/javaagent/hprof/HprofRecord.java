package com.splunk.javaagent.hprof;

import java.nio.MappedByteBuffer;
import java.util.List;

import com.splunk.javaagent.SplunkLogEvent;

public abstract class HprofRecord {

	public static final byte RECORD_UTF8 = 1;
	public static final byte RECORD_LOAD_CLASS = 2;
	public static final byte RECORD_UNLOAD_CLASS = 3;
	public static final byte RECORD_FRAME = 4;
	public static final byte RECORD_TRACE = 5;
	public static final byte RECORD_ALLOC_SITES = 6;
	public static final byte RECORD_HEAP_SUMMARY = 7;
	public static final byte RECORD_START_THREAD = 10;
	public static final byte RECORD_END_THREAD = 11;
	public static final byte RECORD_CPU_SAMPLES = 13;
	public static final byte RECORD_CONTROL_SETTINGS = 14;

	public static final byte RECORD_HEAP_DUMP = 12;
	public static final byte RECORD_HEAPDUMP_SEGMENT_START = 28;
	public static final byte RECORD_HEAPDUMP_SEGMENT_END = 44;

	public static final byte RECORD_GC_ROOT_UNKNOWN = -1;
	public static final byte RECORD_GC_ROOT_JNI_GLOBAL = 1;
	public static final byte RECORD_GC_ROOT_JNI_LOCAL = 2;
	public static final byte RECORD_GC_ROOT_JAVA_FRAME = 3;
	public static final byte RECORD_GC_ROOT_NATIVE_STACK = 4;
	public static final byte RECORD_GC_ROOT_STICKY_CLASS = 5;
	public static final byte RECORD_GC_ROOT_THREAD_BLOCK = 6;
	public static final byte RECORD_GC_ROOT_MONITOR_USED = 7;
	public static final byte RECORD_GC_ROOT_THREAD_OBJ = 8;
	public static final byte RECORD_GC_CLASS_DUMP = 32;
	public static final byte RECORD_GC_INSTANCE_DUMP = 33;
	public static final byte RECORD_GC_OBJ_ARRAY_DUMP = 34;
	public static final byte RECORD_GC_PRIM_ARRAY_DUMP = 35;

	public static final byte TYPE_ARRAY_OBJECT = 1;
	public static final byte TYPE_OBJECT = 2;
	public static final byte TYPE_BOOLEAN = 4;
	public static final byte TYPE_CHAR = 5;
	public static final byte TYPE_FLOAT = 6;
	public static final byte TYPE_DOUBLE = 7;
	public static final byte TYPE_BYTE = 8;
	public static final byte TYPE_SHORT = 9;
	public static final byte TYPE_INT = 10;
	public static final byte TYPE_LONG = 11;

	byte recordType;
	long timeMicrosecondsSinceHeaderTimeStamp;
	int recordlength;
	HprofDump parent;
	MappedByteBuffer buf;

	public abstract void parseRecord();

	public abstract SplunkLogEvent getSplunkLogEvent();

	public static HprofRecord create(HprofDump parent, byte tag,
			int elapsedTimeMicroSeconds, int recordLength, MappedByteBuffer buf) {

		HprofRecord record = null;

		switch (tag) {

		case RECORD_UTF8:record = new UTF8Record();break;
		case RECORD_LOAD_CLASS:record = new LoadClassRecord();break;
		case RECORD_UNLOAD_CLASS:record = new UnloadClassRecord();break;
		case RECORD_FRAME:record = new FrameRecord();break;		
		case RECORD_TRACE:record = new TraceRecord();break;		
		case RECORD_ALLOC_SITES:record = new AllocSitesRecord();break;		
		case RECORD_HEAP_SUMMARY:record = new HeapSummaryRecord();break;
		case RECORD_START_THREAD:record = new StartThreadRecord();break;
		case RECORD_END_THREAD:record = new EndThreadRecord();break;		
		case RECORD_CPU_SAMPLES:record = new CPUSamplesRecord();break;
		case RECORD_CONTROL_SETTINGS:record = new ControlSettingsRecord();break;
		
		/**
        case RECORD_HEAPDUMP_SEGMENT_START:record = new HeapDumpRecord();break;
		case RECORD_HEAP_DUMP:record = new HeapDumpRecord();break;
		**/
		
		case RECORD_HEAPDUMP_SEGMENT_END:break;

		default:
			byte unknown[] = new byte[recordLength];
			buf.get(unknown);
			break;

		}

		if (record != null) {
			record.parent = parent;
			record.timeMicrosecondsSinceHeaderTimeStamp = elapsedTimeMicroSeconds;
			record.recordType = tag;
			record.recordlength = recordLength;
			record.buf = buf;
			record.parseRecord();
		}
		return record;
	}

	protected HprofIDField readId() {
		int size = parent.getHeader().getIdentifierSize();
		if (size == 4) {
			int v = buf.getInt();
			return new HprofIDField(size, (long) v);
		}
		if (size == 8) {
			long v = buf.getLong();
			return new HprofIDField(size, v);
		} else {
			return new HprofIDField(size, 0);
		}
	}

	protected String getNameForClassId(HprofIDField classID) {
		String name = "undetermined";
		HprofIDField nameid = (HprofIDField) parent.classNameMap.get(classID);
		if (nameid != null)
			name = (String) parent.nameMap.get(nameid);
		return name;
	}

	public byte getRecordType() {
		return recordType;
	}

	public long getTimeMicrosecondsSinceHeaderTimeStamp() {
		return timeMicrosecondsSinceHeaderTimeStamp;
	}

	public long getRecordlength() {
		return recordlength;
	}
	
	protected void addCommonSplunkLogEventFields(SplunkLogEvent event){
		event.addPair("hprof_master_timestamp_millis", parent.getHeader()
				.getTimestampMillis());
		event.addPair("record_timestamp_offset_micros",
				this.timeMicrosecondsSinceHeaderTimeStamp);
	}
	protected String toSplunkMVString(List items) {
		
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<items.size();i++){
			sb.append(items.get(i).toString());
			if(i<items.size()-1)
				sb.append(",");
		}
			
		return sb.toString();
	}

}
