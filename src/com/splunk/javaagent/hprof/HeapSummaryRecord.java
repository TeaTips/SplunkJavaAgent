package com.splunk.javaagent.hprof;

import com.splunk.javaagent.SplunkLogEvent;

public class HeapSummaryRecord extends HprofRecord {

	private int totalLiveBytes;
	private int totalLiveInstances;
	private long totalBytesAllocated;
	private long totalInstancesAllocated;

	@Override
	public void parseRecord() {

		this.totalLiveBytes = buf.getInt();
		this.totalLiveInstances = buf.getInt();
		this.totalBytesAllocated = buf.getLong();
		this.totalInstancesAllocated = buf.getLong();

	}

	@Override
	public SplunkLogEvent getSplunkLogEvent() {
		SplunkLogEvent event = new SplunkLogEvent("hprof_heapsummary",
				"splunkagent", false, false);
		addCommonSplunkLogEventFields(event);
		event.addPair("totalLiveBytes", this.totalLiveBytes);
		event.addPair("totalLiveInstances", this.totalLiveInstances);
		event.addPair("totalBytesAllocated", this.totalBytesAllocated);
		event.addPair("totalInstancesAllocated", this.totalInstancesAllocated);

		return event;
	}

}
