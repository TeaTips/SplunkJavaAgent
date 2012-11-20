package com.splunk.javaagent.jmx;

import java.util.Set;
import java.util.SortedMap;

import javax.management.Notification;
import javax.management.NotificationListener;

import com.splunk.javaagent.SplunkJavaAgent;
import com.splunk.javaagent.SplunkLogEvent;
import com.splunk.javaagent.jmx.formatter.FormatterUtils;

public class SplunkNotificationListener implements NotificationListener {

	private String mBeanName;

	public SplunkNotificationListener(String mBeanName) {
		this.mBeanName = mBeanName;
	}

	@Override
	public void handleNotification(Notification notification, Object handback) {

		if (notification != null) {
			SplunkLogEvent event = new SplunkLogEvent("jmx-notification",
					"splunkagent", true, false);

			SortedMap<String, String> mbeanNameParts = FormatterUtils
					.tokenizeMBeanCanonicalName(mBeanName);

			Set<String> mBeanNameKeys = mbeanNameParts.keySet();

			for (String key : mBeanNameKeys) {

				event.addPair(key, mbeanNameParts.get(key));

			}
			event.addPair("type", notification.getType());
			event.addPair("message", notification.getMessage());
			event.addPair("seqNum", notification.getSequenceNumber());
			event.addPair("timestamp", notification.getTimeStamp());
			event.addPair("userData", notification.getUserData().toString());
			event.addPair("source", notification.getSource().toString());
			event.addPair("class", notification.getClass().getCanonicalName());
			SplunkJavaAgent.jmxEvent(event);
		}

	}

}
