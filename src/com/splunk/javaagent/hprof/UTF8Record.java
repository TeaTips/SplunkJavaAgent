package com.splunk.javaagent.hprof;

import com.splunk.javaagent.SplunkLogEvent;

public class UTF8Record extends HprofRecord {

	private HprofIDField idField;
	private String name = "";


	@Override
	public void parseRecord() {

		this.idField = readId();
		
		int charsLength = recordlength - parent.getHeader().getIdentifierSize();
		if (charsLength > 0) {
			try {
				byte chars[] = new byte[charsLength];
				buf.get(chars);
				this.name = new String(chars, "UTF8");
			} catch (Throwable t) {
			}
		}
		parent.nameMap.put(idField, this.name);

	}

	@Override
	public SplunkLogEvent getSplunkLogEvent() {
		SplunkLogEvent event = new SplunkLogEvent("hprof_utf8", "splunkagent",
				false, false);
		addCommonSplunkLogEventFields(event);
		event.addPair("id", this.idField.toString());
		event.addPair("name", this.name);
		return event;
	}
	
	

}
