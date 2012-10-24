package com.splunk.javaagent.transport;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.Map;

import com.splunk.javaagent.SplunkLogEvent;

public class SplunkTCPTransport extends SplunkInput implements SplunkTransport {

	// connection props
	private String host = "";
	private int port;

	// streaming objects
	private Socket streamSocket = null;
	private OutputStream ostream;
	private Writer writerOut = null;

	@Override
	public void init(Map<String, String> args) throws Exception {

		this.host = args.get("splunk.transport.tcp.host");
		this.port = Integer.parseInt(args.get("splunk.transport.tcp.port"));
		setDropEventsOnQueueFull(Boolean.parseBoolean(args
				.get("splunk.transport.tcp.dropEventsOnQueueFull")));
		setMaxQueueSize(args.get("splunk.transport.tcp.maxQueueSize"));
	}

	@Override
	public void start() throws Exception {
		streamSocket = new Socket(host, port);
		if (streamSocket.isConnected()) {
			ostream = streamSocket.getOutputStream();
			writerOut = new OutputStreamWriter(ostream, "UTF8");
		}

	}

	@Override
	public void stop() throws Exception {
		try {

			if (writerOut != null) {
				writerOut.flush();
				writerOut.close();
				if (streamSocket != null)
					streamSocket.close();
			}
		} catch (Exception e) {
		}

	}

	@Override
	public void send(SplunkLogEvent event) {
		String currentMessage = event.toString();
		try {

			if (writerOut != null) {

				// send the message
				writerOut.write(currentMessage + "\n");

				// flush the queue
				while (queueContainsEvents()) {
					String messageOffQueue = dequeue();
					currentMessage = messageOffQueue;
					writerOut.write(currentMessage + "\n");
				}
				writerOut.flush();
			}

		} catch (IOException e) {

			// something went wrong , put message on the queue for retry
			enqueue(currentMessage);
			try {
				stop();
			} catch (Exception e1) {
			}

			try {
				start();
			} catch (Exception e2) {
			}
		}

	}

}
