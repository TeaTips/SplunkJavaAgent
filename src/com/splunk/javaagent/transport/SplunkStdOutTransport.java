package com.splunk.javaagent.transport;

import java.util.Map;

import com.splunk.javaagent.SplunkLogEvent;

public class SplunkStdOutTransport implements SplunkTransport {

	@Override
	public void init(Map<String, String> args) throws Exception {
		System.out.println("Init SplunkStdOutTransport");

	}

	@Override
	public void start() throws Exception {
		System.out.println("Start SplunkStdOutTransport");

	}

	@Override
	public void stop() throws Exception {
		System.out.println("Stop SplunkStdOutTransport");

	}

	@Override
	public void send(SplunkLogEvent event) {
		System.out.println(event.toString());

	}

}
