package com.splunk.javaagent;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ArrayBlockingQueue;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import com.splunk.javaagent.hprof.HprofDump;
import com.splunk.javaagent.jmx.JMXMBeanPoller;
import com.splunk.javaagent.trace.FilterListItem;
import com.splunk.javaagent.trace.SplunkClassFileTransformer;
import com.splunk.javaagent.transport.SplunkTransport;

public class SplunkJavaAgent {

	private static SplunkJavaAgent agent;

	private Properties props;
	private SplunkTransport transport;
	private List<FilterListItem> whiteList;
	private List<FilterListItem> blackList;
	private boolean traceMethodExited;
	private boolean traceMethodEntered;
	private boolean traceClassLoaded;
	private boolean traceErrors;
	private boolean traceJMX;
	private boolean traceHprof;
	private Map<String, Integer> jmxConfigFiles;
	private List<Byte> hprofRecordFilter;
	private Map<Byte, List<Byte>> hprofHeapDumpSubRecordFilter;
	private String hprofFile;
	private int hprofFrequency = 600;// seconds
	private Map<String, String> userTags;
	private ArrayBlockingQueue<SplunkLogEvent> eventQueue;
	private int queueSize = 10000;
	private String appName;
	private String appID;

	private TransporterThread transporterThread;

	public SplunkJavaAgent() {

		this.whiteList = new ArrayList<FilterListItem>();
		this.blackList = new ArrayList<FilterListItem>();

	}

	public static void premain(String agentArgument,
			Instrumentation instrumentation) {

		try {
			agent = new SplunkJavaAgent();

			if (!agent.loadProperties())
				return;
			if (!agent.initCommonProperties())
				return;
			if (!agent.initTransport())
				return;
			if (!agent.initTracing())
				return;
			if (!agent.initFilters())
				return;
			if (!agent.initJMX())
				return;
			if (!agent.initHprof())
				return;

			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					try {
						if (agent.transport != null)
							agent.transport.stop();
					} catch (Exception e) {
					}
				}
			});

			instrumentation.addTransformer(new SplunkClassFileTransformer());

		} catch (Throwable t) {

		}

	}

	private boolean initTracing() {

		this.traceClassLoaded = Boolean.parseBoolean(agent.props.getProperty(
				"trace.classLoaded", "true"));
		this.traceMethodEntered = Boolean.parseBoolean(agent.props.getProperty(
				"trace.methodEntered", "true"));
		this.traceMethodExited = Boolean.parseBoolean(agent.props.getProperty(
				"trace.methodExited", "true"));
		this.traceErrors = Boolean.parseBoolean(agent.props.getProperty(
				"trace.errors", "true"));

		return true;
	}

	private boolean initHprof() {

		this.traceHprof = Boolean.parseBoolean(agent.props.getProperty(
				"trace.hprof", "false"));
		if (this.traceHprof) {
			this.hprofFile = props.getProperty("trace.hprof.tempfile", "");
			try {
				this.hprofFrequency = Integer.parseInt(props.getProperty(
						"trace.hprof.frequency", "600"));
			} catch (NumberFormatException e) {

			}
			String hprofRecordFilterString = props.getProperty(
					"trace.hprof.recordtypes", "");
			if (hprofRecordFilterString.length() >= 1) {
				this.hprofRecordFilter = new ArrayList<Byte>();
				this.hprofHeapDumpSubRecordFilter = new HashMap<Byte, List<Byte>>();
				StringTokenizer st = new StringTokenizer(
						hprofRecordFilterString, ",");
				while (st.hasMoreTokens()) {
					StringTokenizer st2 = new StringTokenizer(st.nextToken(),
							":");

					byte val = Byte.parseByte(st2.nextToken());
					this.hprofRecordFilter.add(val);
					// subrecords
					if (st2.hasMoreTokens()) {
						byte subVal = Byte.parseByte(st2.nextToken());
						List<Byte> list = this.hprofHeapDumpSubRecordFilter
								.get(val);
						if (list == null) {
							list = new ArrayList<Byte>();
						}
						list.add(subVal);
						this.hprofHeapDumpSubRecordFilter.put(val, list);
					}

				}
			}
			try {
				HprofThread thread = new HprofThread(Thread.currentThread(),
						this.hprofFrequency, this.hprofFile);
				thread.start();
			} catch (Exception e) {
			}
		}

		return true;
	}

	private boolean initJMX() {

		this.traceJMX = Boolean.parseBoolean(agent.props.getProperty(
				"trace.jmx", "false"));
		if (this.traceJMX) {

			this.jmxConfigFiles = new HashMap<String, Integer>();
			String configFiles = props.getProperty("trace.jmx.configfiles", "");
			String defaultFrequency = props.getProperty(
					"trace.jmx.default.frequency", "60");
			StringTokenizer st = new StringTokenizer(configFiles, ",");
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				String frequency = props.getProperty("trace.jmx." + token
						+ ".frequency", defaultFrequency);
				this.jmxConfigFiles.put(token + ".xml",
						Integer.parseInt(frequency));
			}

			Set<String> configFileNames = this.jmxConfigFiles.keySet();
			for (String configFile : configFileNames) {
				JMXThread thread = new JMXThread(Thread.currentThread(),
						this.jmxConfigFiles.get(configFile), configFile);
				thread.start();
			}
		}

		return true;
	}

	private boolean initCommonProperties() {

		this.appName = props.getProperty("agent.app.name", "");
		this.appID = props.getProperty("agent.app.instance", "");

		String tags = (String) props.getProperty("agent.userEventTags", "");
		this.userTags = new HashMap<String, String>();

		StringTokenizer st = new StringTokenizer(tags, ",");
		while (st.hasMoreTokens()) {
			String item = st.nextToken();
			StringTokenizer st2 = new StringTokenizer(item, "=");
			String key = st2.nextToken();
			String value = st2.nextToken();
			this.userTags.put(key, value);

		}

		return true;
	}

	class TransporterThread extends Thread {

		Thread parent;

		TransporterThread(Thread parent) {
			this.parent = parent;
		}

		public void run() {

			while (parent.isAlive()) {

				try {
					while (!agent.eventQueue.isEmpty()) {
						SplunkLogEvent event = agent.eventQueue.poll();

						if (event != null) {
							agent.transport.send(event);
						}
					}
				} catch (Throwable t) {

				}
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {

				}
			}
		}

	}

	class JMXThread extends Thread {

		Thread parent;
		int frequencySeconds;
		String configFile;
		JMXMBeanPoller poller;

		JMXThread(Thread parent, int frequencySeconds, String configFile) {
			this.parent = parent;
			this.configFile = configFile;
			this.frequencySeconds = frequencySeconds;
			this.poller = new JMXMBeanPoller(configFile);

		}

		public void run() {

			while (parent.isAlive()) {

				try {

					Thread.sleep(frequencySeconds * 1000);
				} catch (InterruptedException e) {
				}

				try {
					poller.execute();

				} catch (Throwable t) {

				}

			}
		}

	}

	class HprofThread extends Thread {

		Thread parent;
		int frequencySeconds;
		String hprofFile;
		MBeanServerConnection serverConnection;
		ObjectName mbean;
		String operationName;
		Object[] params;
		String[] signature;

		HprofThread(Thread parent, int frequencySeconds, String hprofFile)
				throws Exception {
			this.parent = parent;
			this.hprofFile = hprofFile;
			this.frequencySeconds = frequencySeconds;
			this.serverConnection = ManagementFactory.getPlatformMBeanServer();
			this.mbean = new ObjectName(
					"com.sun.management:type=HotSpotDiagnostic");
			this.operationName = "dumpHeap";
			this.params = new Object[2];
			this.params[0] = hprofFile;
			this.params[1] = new Boolean(true);
			this.signature = new String[2];
			this.signature[0] = "java.lang.String";
			this.signature[1] = "boolean";
		}

		public void run() {

			while (parent.isAlive()) {
				try {

					Thread.sleep(frequencySeconds * 1000);
				} catch (InterruptedException e) {
				}
				try {
					// do some housekeeping
					File file = new File(this.hprofFile);
					if (file.exists())
						file.delete();

					// do the dump via JMX
					serverConnection.invoke(mbean, operationName, params,
							signature);

					// process the dump
					file = new File(this.hprofFile);
					HprofDump hprof = new HprofDump(file);
					hprof.process();

					// delete the dump files
					if (file.exists())
						file.delete();

				} catch (Throwable e) {

				}

			}
		}

	}

	private boolean initFilters() {

		try {
			String white = (String) props.getProperty("trace.whitelist", "");
			String black = (String) props.getProperty("trace.blacklist", "");

			addToList(white, this.whiteList);
			addToList(black, this.blackList);
			return true;

		} catch (Exception e) {

			return false;
		}
	}

	private void addToList(String items, List<FilterListItem> list) {

		StringTokenizer st = new StringTokenizer(items, ",");
		while (st.hasMoreTokens()) {
			String item = st.nextToken();
			StringTokenizer st2 = new StringTokenizer(item, ":");
			FilterListItem fli = new FilterListItem();
			String className = st2.nextToken();
			fli.setClassName(className);
			if (st2.hasMoreTokens()) {
				String methodName = st2.nextToken();
				fli.setMethodName(methodName);

			}
			list.add(fli);
		}

	}

	public static boolean isWhiteListed(String className) {

		if (agent.whiteList.isEmpty())
			return true;
		for (FilterListItem item : agent.whiteList) {
			if (className.startsWith(item.getClassName()))
				return true;
		}
		return false;
	}

	public static boolean isWhiteListed(String className, String methodName) {

		if (agent.whiteList.isEmpty())
			return true;
		for (FilterListItem item : agent.whiteList) {
			if (className.startsWith(item.getClassName())
					&& methodName.equals(item.getMethodName()))
				return true;
			else if (className.startsWith(item.getClassName())
					&& item.getMethodName() == null) {
				return true;
			}
		}
		return false;

	}

	public static boolean isBlackListed(String className) {

		if (agent.blackList.isEmpty())
			return false;
		for (FilterListItem item : agent.blackList) {
			if (className.startsWith(item.getClassName()))
				return true;
		}
		return false;
	}

	public static boolean isBlackListed(String className, String methodName) {

		if (agent.blackList.isEmpty())
			return true;
		for (FilterListItem item : agent.blackList) {
			if (className.startsWith(item.getClassName())
					&& methodName.equals(item.getMethodName()))
				return true;
			else if (className.startsWith(item.getClassName())
					&& item.getMethodName() == null) {
				return true;
			}
		}
		return false;

	}

	private boolean initTransport() {

		try {
			this.transport = (SplunkTransport) Class
					.forName(
							props.getProperty("splunk.transport.impl",
									"com.splunk.javaagent.transport.SplunkTCPTransport"))
					.newInstance();
		} catch (Exception e) {

			return false;
		}
		Map<String, String> args = new HashMap<String, String>();
		Set<Object> keys = props.keySet();
		for (Object key : keys) {
			String keyString = (String) key;
			if (keyString.startsWith("splunk."))
				args.put(keyString, props.getProperty(keyString));
		}

		try {
			this.queueSize = Integer.parseInt(props.getProperty(
					"splunk.transport.internalQueueSize", "10000"));
		} catch (NumberFormatException e) {

		}

		try {

			this.eventQueue = new ArrayBlockingQueue<SplunkLogEvent>(queueSize);
			this.transport.init(args);
			this.transport.start();
			this.transporterThread = new TransporterThread(
					Thread.currentThread());
			this.transporterThread.start();

		} catch (Exception e) {

			return false;
		}
		return true;

	}

	private boolean loadProperties() {

		this.props = new Properties();
		InputStream in = ClassLoader
				.getSystemResourceAsStream("splunkagent.properties");
		try {
			this.props.load(in);
		} catch (IOException e) {
			return false;
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				return false;
			}
		}
		return true;

	}

	public static void classLoaded(String className) {

		if (agent.traceClassLoaded) {
			SplunkLogEvent event = new SplunkLogEvent("class_loaded",
					"splunkagent", true, false);
			event.addPair("appName", agent.appName);
			event.addPair("appID", agent.appID);
			event.addPair("className", className);
			addUserTags(event);
			agent.transport.send(event);
		}
	}

	private static void addUserTags(SplunkLogEvent event) {

		if (!agent.userTags.isEmpty()) {

			Set<String> keys = agent.userTags.keySet();
			for (String key : keys) {
				event.addPair(key, agent.userTags.get(key));
			}
		}

	}

	public static void methodEntered(String className, String methodName,
			String desc) {

		if (agent.traceMethodEntered) {
			SplunkLogEvent event = new SplunkLogEvent("method_entered",
					"splunkagent", true, false);
			event.addPair("appName", agent.appName);
			event.addPair("appID", agent.appID);
			event.addPair("className", className);
			event.addPair("methodName", methodName);
			event.addPair("methodDesc", desc);
			event.addPair("threadID", Thread.currentThread().getId());
			event.addPair("threadName", Thread.currentThread().getName());
			
			try {
				StackTraceElement ste = Thread.currentThread().getStackTrace()[3];
				if(ste != null)
				  event.addPair("lineNumber", ste.getLineNumber());
				  event.addPair("sourceFileName", ste.getFileName());
			} catch (Exception e1) {
			}
			
			addUserTags(event);
			try {
				agent.eventQueue.put(event);
				// agent.eventQueue.offer(event,1000,TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {

			}
		}
	}

	public static void methodExited(String className, String methodName,
			String desc) {

		if (agent.traceMethodExited) {

			SplunkLogEvent event = new SplunkLogEvent("method_exited",
					"splunkagent", true, false);
			event.addPair("appName", agent.appName);
			event.addPair("appID", agent.appID);
			event.addPair("className", className);
			event.addPair("methodName", methodName);
			event.addPair("methodDesc", desc);
			event.addPair("threadID", Thread.currentThread().getId());
			event.addPair("threadName", Thread.currentThread().getName());
			addUserTags(event);
			try {
				agent.eventQueue.put(event);
				// agent.eventQueue.offer(event,1000,TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {

			}
		}
	}

	public static void throwableCaught(String className, String methodName,
			String desc, Throwable t) {

		if (agent.traceErrors) {

			SplunkLogEvent event = new SplunkLogEvent("throwable_caught",
					"splunkagent", true, false);
			event.addPair("appName", agent.appName);
			event.addPair("appID", agent.appID);
			event.addPair("className", className);
			event.addPair("methodName", methodName);
			event.addPair("methodDesc", desc);
			event.addThrowable(t);
			event.addPair("methodName", methodName);
			event.addPair("threadID", Thread.currentThread().getId());
			event.addPair("threadName", Thread.currentThread().getName());
			addUserTags(event);
			try {
				agent.eventQueue.put(event);
				// agent.eventQueue.offer(event,1000,TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {

			}

		}
	}

	public static void hprofRecordEvent(byte recordType, byte subRecordType,
			SplunkLogEvent event) {

		if (traceHprofRecordType(recordType, subRecordType)) {
			event.addPair("appName", agent.appName);
			event.addPair("appID", agent.appID);
			addUserTags(event);
			try {

				agent.eventQueue.put(event);

			} catch (InterruptedException e) {

			}

		}
	}

	public static void jmxEvent(SplunkLogEvent event) {

		event.addPair("appName", agent.appName);
		event.addPair("appID", agent.appID);
		addUserTags(event);
		try {

			agent.eventQueue.put(event);

		} catch (InterruptedException e) {

		}

	}

	private static boolean traceHprofRecordType(byte recordType,
			byte subRecordType) {
		if (agent.hprofRecordFilter == null
				|| agent.hprofRecordFilter.isEmpty())
			return true;
		else {
			for (byte b : agent.hprofRecordFilter) {
				if (b == recordType) {
					List<Byte> subrecords = agent.hprofHeapDumpSubRecordFilter
							.get(recordType);
					if (subrecords == null || subrecords.isEmpty()) {
						return true;
					} else {
						for (byte bb : subrecords) {
							if (bb == subRecordType) {
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}

}
