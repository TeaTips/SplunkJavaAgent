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

	private String appName;
	private String appID;

	public SplunkJavaAgent() {

		whiteList = new ArrayList<FilterListItem>();
		blackList = new ArrayList<FilterListItem>();
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

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					agent.transport.stop();
				} catch (Exception e) {

				}
			}
		});

		instrumentation.addTransformer(agent);

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
			this.appName = props.getProperty("app.name", "");
			this.appID = props.getProperty("app.instance", "");
			this.traceClassLoaded = Boolean.parseBoolean(agent.props
					.getProperty("trace.classLoaded", "true"));
			this.traceMethodEntered = Boolean.parseBoolean(agent.props
					.getProperty("trace.methodEntered", "true"));
			this.traceMethodExited = Boolean.parseBoolean(agent.props
					.getProperty("trace.methodExited", "true"));
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
		if (!isBlackListed(className) && isWhiteListed(className))
			return processClass(className, classBeingRedefined, classFileBuffer);
		else
			return classFileBuffer;
	}

	private byte[] processClass(String className, Class classBeingRedefined,
			byte[] classFileBuffer) {

		classLoaded(className);
		ClassReader cr = new ClassReader(classFileBuffer);
		ClassWriter cw = new ClassWriter(cr, 0);
		ClassTracerAdaptor ca = new ClassTracerAdaptor(cw);
		cr.accept(ca, 8);
		return cw.toByteArray();

	}

	public static void classLoaded(String className) {

		if (agent.traceClassLoaded) {
			SplunkLogEvent event = new SplunkLogEvent("class_loaded", "splunkagent", true,
					false);
			event.addPair("appName", agent.appName);
			event.addPair("appID", agent.appID);
			event.addPair("className", className);
			agent.transport.send(event);
		}
	}

	public static void methodEntered(String className, String methodName) {

		if (agent.traceMethodEntered) {
			SplunkLogEvent event = new SplunkLogEvent("method_entered", "splunkagent",
					true, false);
			event.addPair("appName", agent.appName);
			event.addPair("appID", agent.appID);
			event.addPair("nanoTime", System.nanoTime());
			event.addPair("className", className);
			event.addPair("methodName", methodName);
			event.addPair("threadID", Thread.currentThread().getId());
			event.addPair("threadName", Thread.currentThread().getName());
			agent.transport.send(event);
		}
	}

	public static void methodExited(String className, String methodName) {

		if (agent.traceMethodExited) {

			SplunkLogEvent event = new SplunkLogEvent("method_exited", "splunkagent",
					true, false);
			event.addPair("appName", agent.appName);
			event.addPair("appID", agent.appID);
			event.addPair("nanoTime", System.nanoTime());
			event.addPair("className", className);
			event.addPair("methodName", methodName);
			event.addPair("threadID", Thread.currentThread().getId());
			event.addPair("threadName", Thread.currentThread().getName());
			agent.transport.send(event);
		}
	}

}
