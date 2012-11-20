package com.splunk.javaagent.hprof;

import java.util.ArrayList;
import java.util.List;

import com.splunk.javaagent.SplunkLogEvent;

public class AllocSitesRecord extends HprofRecord {

	private short flags;
	private int cutoffRatio;
	private int totalLiveBytes;
	private int totalLiveInstances;
	private long totalBytesAllocated;
	private long totalInstancesAllocated;
	private int numberOfSites;
	private List <AllocSite> sites;
	
	@Override
	public void parseRecord() {
		
		this.flags = buf.getShort();
		this.cutoffRatio = buf.getInt();
		this.totalLiveBytes = buf.getInt();
		this.totalLiveInstances = buf.getInt();
		this.totalBytesAllocated = buf.getLong();
		this.totalInstancesAllocated = buf.getLong();
		this.numberOfSites = buf.getInt();
		
		
		this.sites = new ArrayList<AllocSite>();		
		int sitesLength = recordlength - 34;
		
		while (sitesLength > 0) {
			try {
				AllocSite site = new AllocSite();
				
				site.setType(buf.get());
				site.setClassSerial(buf.getInt());
				site.setStackTraceSerial(buf.getInt());
				site.setNumberOfBytesAlive(buf.getInt());
				site.setNumberOfInstancesAlive(buf.getInt());
				site.setNumberOfBytesAllocated(buf.getInt());
				site.setNumberOfInstancesAllocated(buf.getInt());
				sites.add(site);
				sitesLength -= 25;
				
			} catch (Throwable t) {
			}
		}
		
		
	}

	@Override
	public SplunkLogEvent getSplunkLogEvent() {
		SplunkLogEvent event = new SplunkLogEvent("hprof_allocsites", "splunkagent",
				false, false);
		addCommonSplunkLogEventFields(event);
		
		event.addPair("flags", this.flags);
		event.addPair("cutoffRatio", this.cutoffRatio);
		event.addPair("totalLiveBytes", this.totalLiveBytes);
		event.addPair("totalLiveInstances", this.totalLiveInstances);
		event.addPair("totalBytesAllocated", this.totalBytesAllocated);
		event.addPair("totalInstancesAllocated", this.totalInstancesAllocated);
		event.addPair("numberOfSites", this.numberOfSites);
		event.addPair("sites", toSplunkMVString(sites));
		
		return event;
		
	}

}
