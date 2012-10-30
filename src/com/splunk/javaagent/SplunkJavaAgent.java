package com.splunk.javaagent;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ArrayBlockingQueue;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import com.splunk.javaagent.transport.SplunkTransport;

public class SplunkJavaAgent implements ClassFileTransformer {

	private Properties props;
	private SplunkTransport transport;
	private List<FilterListItem> whiteList;
	private List<FilterListItem> blackList;
	private static SplunkJavaAgent agent;
	private boolean traceMethodExited;
	private boolean traceMethodEntered;
	private boolean traceClassLoaded;
	private boolean traceErrors;
	private Map<String, String> userTags;

	private ArrayBlockingQueue<SplunkLogEvent> eventQueue;
	private String appName;
	private String appID;

	private TransporterThread transporterThread;

	public SplunkJavaAgent() {

		whiteList = new ArrayList<FilterListItem>();
		blackList = new ArrayList<FilterListItem>();
		eventQueue = new ArrayBlockingQueue<SplunkLogEvent>(1000);

	}

	public static void premain(String agentArgument,
			Instrumentation instrumentation) {

		agent = new SplunkJavaAgent();
		if (!agent.loadProperties())
			return;
		if (!agent.initTransport())
			return;
		if (!agent.initFilters())
			return;
		if (!agent.initUserTags())
			return;

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {

					agent.transporterThread.stopTransporterThread();
					agent.transport.stop();
				} catch (Exception e) {

				}
			}
		});

		instrumentation.addTransformer(agent);

	}

	private boolean initUserTags() {

		String tags = (String) props.getProperty("agent.userEventTags", "");
		userTags = new HashMap<String, String>();

		StringTokenizer st = new StringTokenizer(tags, ",");
		while (st.hasMoreTokens()) {
			String item = st.nextToken();
			StringTokenizer st2 = new StringTokenizer(item, "=");
			String key = st2.nextToken();
			String value = st2.nextToken();
			userTags.put(key, value);

		}

		return true;
	}

	class TransporterThread extends Thread {

		boolean stopped = false;
		Thread parent;

		TransporterThread(Thread parent) {
			this.parent = parent;
		}

		public void run() {

			while (!stopped && parent.isAlive()) {

				while (!agent.eventQueue.isEmpty()) {
					SplunkLogEvent event = agent.eventQueue.poll();

					if (event != null) {
						agent.transport.send(event);
					}
				}
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {

				}
			}
		}

		public void stopTransporterThread() {

			this.stopped = true;

		}
	}

	private boolean initFilters() {

		try {
			String white = (String) props.getProperty("agent.whitelist", "");
			String black = (String) props.getProperty("agent.blacklist", "");

			addToList(white, whiteList);
			addToList(black, blackList);
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
			transport = (SplunkTransport) Class
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
			transport.init(args);
			transport.start();
			transporterThread = new TransporterThread(Thread.currentThread());
			transporterThread.start();

		} catch (Exception e) {

			return false;
		}
		return true;

	}

	private boolean loadProperties() {
		props = new Properties();
		InputStream in = ClassLoader
				.getSystemResourceAsStream("splunkagent.properties");
		try {
			props.load(in);
			this.appName = props.getProperty("agent.app.name", "");
			this.appID = props.getProperty("agent.app.instance", "");
			this.traceClassLoaded = Boolean.parseBoolean(agent.props
					.getProperty("trace.classLoaded", "true"));
			this.traceMethodEntered = Boolean.parseBoolean(agent.props
					.getProperty("trace.methodEntered", "true"));
			this.traceMethodExited = Boolean.parseBoolean(agent.props
					.getProperty("trace.methodExited", "true"));
			this.traceErrors = Boolean.parseBoolean(agent.props.getProperty(
					"trace.errors", "true"));
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

	@Override
	public byte[] transform(ClassLoader loader, String className,
			Class classBeingRedefined, ProtectionDomain protectionDomain,
			byte[] classFileBuffer) throws IllegalClassFormatException {

		if (this.getClass().getClassLoader().equals(loader)) {

			if (!isBlackListed(className) && isWhiteListed(className))
				return processClass(className, classBeingRedefined,
						classFileBuffer);
			else
				return classFileBuffer;
		} else {
			return classFileBuffer;
		}
	}

	private byte[] processClass(String className, Class classBeingRedefined,
			byte[] classFileBuffer) {

		classLoaded(className);
		ClassReader cr = new ClassReader(classFileBuffer);
		ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
		ClassTracerAdaptor ca = new ClassTracerAdaptor(cw);
		cr.accept(ca, ClassReader.SKIP_FRAMES);
		return cw.toByteArray();

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
			event.addPair("throwableType", t.getClass().getCanonicalName());
			event.addPair("throwableMessage", t.getMessage());
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

}
