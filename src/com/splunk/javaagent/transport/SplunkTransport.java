package com.splunk.javaagent.transport;

import java.util.Map;

import com.splunk.javaagent.SplunkLogEvent;

public interface SplunkTransport {

	public void init(Map<String, String> args) throws Exception;

	public void start() throws Exception;

	public void stop() throws Exception;

	public void send(SplunkLogEvent event);

}
