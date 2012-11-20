package com.splunk.javaagent.jmx;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServerConnection;
import javax.management.NotificationFilter;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.TabularDataSupport;

import com.splunk.javaagent.SplunkLogEvent;
import com.splunk.javaagent.jmx.config.Attribute;
import com.splunk.javaagent.jmx.config.JMXServer;
import com.splunk.javaagent.jmx.config.MBean;
import com.splunk.javaagent.jmx.config.Notification;
import com.splunk.javaagent.jmx.config.Operation;
import com.splunk.javaagent.jmx.formatter.Formatter;
import com.splunk.javaagent.jmx.transport.Transport;

/**
 * Thread to lookup MBeans and Attributes
 * 
 * 
 * @author Damien Dallimore damien@dtdsoftware.com
 * 
 */
public class ProcessServerThread extends Thread {

	// private Logger logger;

	private MBeanServerConnection serverConnection;

	private JMXServer serverConfig;

	// formatter
	private Formatter formatter;

	// transport
	private Transport transport;

	private boolean registerNotificationListeners;

	/**
	 * Thread to run each JMX Server connection in
	 * 
	 * @param serverConfig
	 *            config POJO for this JMX Server
	 * @param formatter
	 *            formatter impl
	 * @param transport
	 *            transport impl
	 */
	public ProcessServerThread(JMXServer serverConfig, Formatter formatter,
			Transport transport, boolean registerNotificationListeners,
			MBeanServerConnection serverConnection) {

		// this.logger = LoggerFactory.getLogger(this.getName());
		this.serverConfig = serverConfig;
		this.formatter = formatter;
		this.transport = transport;
		this.registerNotificationListeners = registerNotificationListeners;
		this.serverConnection = serverConnection;

	}

	@Override
	public void run() {
		try {

			// get list of MBeans to Query
			List<MBean> mbeans = serverConfig.getMbeans();
			if (mbeans != null) {
				for (MBean bean : mbeans) {

					// the list of queried MBeans found on the server
					// if no values are specified for domain and properties
					// attributes , the value will default to the * wildcard
					Set<ObjectInstance> foundBeans = serverConnection
							.queryMBeans(
									new ObjectName(
											(bean.getDomain().length() == 0 ? "*"
													: bean.getDomain())
													+ ":"
													+ (bean.getPropertiesList()
															.length() == 0 ? "*"
															: bean.getPropertiesList())),
									null);

					for (ObjectInstance oi : foundBeans) {

						ObjectName on = oi.getObjectName();
						// the mbean specific part of the SPLUNK output String
						String mBeanName = on.getCanonicalName();

						try {
							Notification notification = bean.getNotification();
							if (registerNotificationListeners
									&& notification != null) {

								NotificationFilter filter = null;
								String filterClass = notification
										.getFilterImplementationClass();
								if (filterClass != null
										&& filterClass.length() > 0) {
									filter = (NotificationFilter) Class
											.forName(filterClass).newInstance();
								}
								SplunkNotificationListener listener = new SplunkNotificationListener(
										mBeanName);
								serverConnection.addNotificationListener(on,
										listener, filter, null);

							}
						} catch (Exception e1) {
						}

						Map<String, String> mBeanAttributes = new HashMap<String, String>();

						// execute operations
						if (bean.getOperations() != null) {

							for (Operation operation : bean.getOperations()) {
								try {
									Object result = serverConnection.invoke(on,
											operation.getName(),
											operation.getParametersArray(),
											operation.getSignatureArray());
									String outputname = operation
											.getOutputname();
									if (outputname != null
											&& !outputname.isEmpty())
										mBeanAttributes.put(
												operation.getOutputname(),
												resolveObjectToString(result));
								} catch (Exception e) {

									// logger.error("Error : " +
									// e.getMessage());
								}
							}
						}
						// extract all attributes
						if (bean.isDumpAllAttributes()) {
							MBeanAttributeInfo[] attributes = serverConnection
									.getMBeanInfo(on).getAttributes();
							for (MBeanAttributeInfo attribute : attributes) {
								try {
									Object attributeValue = serverConnection
											.getAttribute(on,
													attribute.getName());
									extractAttributeValue(attributeValue,
											mBeanAttributes,
											attribute.getName());
								} catch (Exception e) {

									// logger.error("Error : " +
									// e.getMessage());
								}

							}

						}
						// extract attributes
						else if (bean.getAttributes() != null) {

							// look up the attribute for the MBean
							for (Attribute singular : bean.getAttributes()) {
								List<String> tokens = singular.getTokens();
								Object attributeValue = null;

								// if the attribute pattern is multi level, loop
								// through the levels until the value is found
								for (String token : tokens) {

									// get root attribute object the first time
									if (attributeValue == null) {
										try {

											attributeValue = serverConnection
													.getAttribute(on, token);

										} catch (Exception e) {

											// logger.error("Error : "
											// + e.getMessage());
										}
									} else if (attributeValue instanceof CompositeDataSupport) {
										try {

											attributeValue = ((CompositeDataSupport) attributeValue)
													.get(token);
										} catch (Exception e) {

											// logger.error("Error : "
											// + e.getMessage());
										}
									} else if (attributeValue instanceof TabularDataSupport) {
										try {

											Object[] key = { token };

											attributeValue = ((TabularDataSupport) attributeValue)
													.get(key);

										} catch (Exception e) {

											// logger.error("Error : "
											// + e.getMessage());
										}
									} else {

									}
								}

								mBeanAttributes.put(singular.getOutputname(),
										resolveObjectToString(attributeValue));

							}

						}

						SplunkLogEvent event = formatter.format(mBeanName,
								mBeanAttributes, System.currentTimeMillis());

						transport.transport(event);
					}

				}

			}

		} catch (Exception e) {

			// logger.error(serverConfig + ",systemErrorMessage=\""
			// + e.getMessage() + "\"");
		} finally {

			if (transport != null) {
				transport.close();
			}

		}

	}

	/**
	 * Extract MBean attributes and if necessary, deeply inspect and resolve
	 * composite and tabular data.
	 * 
	 * @param attributeValue
	 *            the attribute object
	 * @param mBeanAttributes
	 *            the map used to hold attribute values before being handed off
	 *            to the formatter
	 * @param attributeName
	 *            the attribute name
	 */
	private void extractAttributeValue(Object attributeValue,
			Map<String, String> mBeanAttributes, String attributeName) {

		if (attributeValue instanceof String[]) {
			try {
				mBeanAttributes.put(attributeName,
						resolveObjectToString(attributeValue));
			} catch (Exception e) {

				// logger.error("Error : " + e.getMessage());
			}
		} else if (attributeValue instanceof Object[]) {
			try {
				int index = 0;
				for (Object obj : (Object[]) attributeValue) {
					index++;
					extractAttributeValue(obj, mBeanAttributes, attributeName
							+ "_" + index);
				}
			} catch (Exception e) {

				// logger.error("Error : " + e.getMessage());
			}
		} else if (attributeValue instanceof Collection) {
			try {
				int index = 0;
				for (Object obj : (Collection) attributeValue) {
					index++;
					extractAttributeValue(obj, mBeanAttributes, attributeName
							+ "_" + index);
				}
			} catch (Exception e) {

				// logger.error("Error : " + e.getMessage());
			}
		} else if (attributeValue instanceof CompositeDataSupport) {

			try {
				CompositeDataSupport cds = ((CompositeDataSupport) attributeValue);
				CompositeType ct = cds.getCompositeType();

				Set<String> keys = ct.keySet();

				for (String key : keys) {
					extractAttributeValue(cds.get(key), mBeanAttributes,
							attributeName + "_" + key);
				}

			} catch (Exception e) {

				// logger.error("Error : " + e.getMessage());
			}
		} else if (attributeValue instanceof TabularDataSupport) {
			try {
				TabularDataSupport tds = ((TabularDataSupport) attributeValue);
				Set<Object> keys = tds.keySet();
				for (Object key : keys) {

					Object keyName = ((List) key).get(0);
					Object[] keyArray = { keyName };
					extractAttributeValue(tds.get(keyArray), mBeanAttributes,
							attributeName + "_" + keyName);
				}

			} catch (Exception e) {
				// logger.error("Error : " + e.getMessage());
			}
		} else {

			try {
				mBeanAttributes.put(attributeName,
						resolveObjectToString(attributeValue));
			} catch (Exception e) {

				// logger.error("Error : " + e.getMessage());
			}
		}

	}

	/**
	 * Resolve an Object to a String representation. Arrays, Lists, Sets and
	 * Maps will be recursively deep resolved
	 * 
	 * @param obj
	 * @return
	 */
	private String resolveObjectToString(Object obj) {

		StringBuffer sb = new StringBuffer();
		if (obj != null) {

			// convert an array to a List view
			if (obj instanceof Object[]) {
				sb.append(Arrays.toString((Object[]) obj));
			} else if (obj instanceof int[]) {
				sb.append(Arrays.toString((int[]) obj));
			} else if (obj instanceof long[]) {
				sb.append(Arrays.toString((long[]) obj));
			} else if (obj instanceof float[]) {
				sb.append(Arrays.toString((float[]) obj));
			} else if (obj instanceof double[]) {
				sb.append(Arrays.toString((double[]) obj));
			} else if (obj instanceof char[]) {
				sb.append(Arrays.toString((char[]) obj));
			} else if (obj instanceof boolean[]) {
				sb.append(Arrays.toString((boolean[]) obj));
			} else if (obj instanceof byte[]) {
				sb.append(Arrays.toString((byte[]) obj));
			} else if (obj instanceof short[]) {
				sb.append(Arrays.toString((short[]) obj));
			}

			else if (obj instanceof Map) {
				sb.append("[");
				Map map = (Map) obj;
				Set keys = map.keySet();
				int totalEntrys = keys.size();
				int index = 0;
				for (Object key : keys) {
					index++;
					Object value = map.get(key);
					sb.append(resolveObjectToString(key));
					sb.append("=");
					sb.append(resolveObjectToString(value));
					if (index < totalEntrys)
						sb.append(",");
				}
				sb.append("]");
			} else if (obj instanceof List) {
				sb.append("[");
				List list = (List) obj;
				int totalEntrys = list.size();
				int index = 0;
				for (Object item : list) {
					index++;
					sb.append(resolveObjectToString(item));
					if (index < totalEntrys)
						sb.append(",");
				}
				sb.append("]");
			} else if (obj instanceof Set) {
				sb.append("[");
				Set set = (Set) obj;
				int totalEntrys = set.size();
				int index = 0;
				for (Object item : set) {
					index++;
					sb.append(resolveObjectToString(item));
					if (index < totalEntrys)
						sb.append(",");
				}
				sb.append("]");
			} else {

				sb.append(obj.toString());
			}
		}
		return sb.toString();

	}

}
