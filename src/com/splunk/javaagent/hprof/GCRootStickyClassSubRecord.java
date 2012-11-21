package com.splunk.javaagent.hprof;

import com.splunk.javaagent.SplunkLogEvent;

public class GCRootStickyClassSubRecord extends HprofRecord {

	private HprofIDField objectID;
	

	@Override
	public void parseRecord() {

		this.objectID = readId();
		
	}

	@Override
	public SplunkLogEvent getSplunkLogEvent() {
		SplunkLogEvent event = new SplunkLogEvent("hprof_heapdump_stickyclass",
				"splunkagent", false, false);
		addCommonSplunkLogEventFields(event);
		event.addPair("objectID", this.objectID.toString());
		String name = "null";
		HprofIDField nameID = (HprofIDField) parent.classNameMap.get(objectID);
		if (nameID!= null)
			name = (String) parent.nameMap.get(nameID);
		event.addPair("name", name);
		return event;
	}

}
