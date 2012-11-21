package com.splunk.javaagent.hprof;

import java.util.ArrayList;
import java.util.List;

import com.splunk.javaagent.SplunkLogEvent;

public class CPUSamplesRecord extends HprofRecord {

	private int totalNumberOfSamples;
	private int numberOfTraces;
	private List<CPUSample> samples;

	@Override
	public void parseRecord() {

		this.totalNumberOfSamples = buf.getInt();
		this.numberOfTraces = buf.getInt();

		this.samples = new ArrayList<CPUSample>();
		int samplesLength = recordlength - 8;

		while (samplesLength > 0) {
			try {
				CPUSample sample = new CPUSample();

				sample.setNumberOfSamples(buf.getInt());
				sample.setStackTraceSerial(buf.getInt());
				samples.add(sample);
				samplesLength -= 8;

			} catch (Throwable t) {
			}
		}

	}

	@Override
	public SplunkLogEvent getSplunkLogEvent() {
		SplunkLogEvent event = new SplunkLogEvent("hprof_cpusamples",
				"splunkagent", false, false);
		addCommonSplunkLogEventFields(event);
		event.addPair("totalNumberOfSamples", this.totalNumberOfSamples);
		event.addPair("numberOfTraces", this.numberOfTraces);
		event.addPair("samples", toSplunkMVString(samples));

		return event;
	}

}
