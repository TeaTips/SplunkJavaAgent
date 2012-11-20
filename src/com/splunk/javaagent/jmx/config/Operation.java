package com.splunk.javaagent.jmx.config;

import java.util.List;

/**
 * POJO for an MBean operation
 * 
 * @author Damien Dallimore damien@dtdsoftware.com
 * 
 */
public class Operation {

	// operation name
	public String name;

	// name for the result that should be written out to SPLUNK
	public String outputname = "";

	// operations parameters
	public List<Parameter> parameters;

	// private static Logger logger = LoggerFactory.getLogger(Operation.class);

	public Operation() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOutputname() {
		return outputname;
	}

	public void setOutputname(String outputname) {
		this.outputname = outputname;
	}

	public List<Parameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<Parameter> parameters) {
		this.parameters = parameters;
	}

	public Object[] getParametersArray() {
		if (parameters == null || parameters.isEmpty())
			return null;
		Object[] obj = new Object[parameters.size()];

		for (int i = 0; i < parameters.size(); i++) {
			obj[i] = getObjectInstance(parameters.get(i));
		}

		return obj;

	}

	public String[] getSignatureArray() {
		if (parameters == null || parameters.isEmpty())
			return null;
		String[] signature = new String[parameters.size()];

		for (int i = 0; i < parameters.size(); i++) {
			signature[i] = getObjectType(parameters.get(i));
		}
		return signature;
	}

	private Object getObjectInstance(Parameter parameter) {

		try {
			String type = parameter.getType();
			String value = parameter.getValue();

			if (type.equalsIgnoreCase(Parameter.INT))
				return new Integer(value);
			else if (type.equalsIgnoreCase(Parameter.FLOAT))
				return new Float(value);
			else if (type.equalsIgnoreCase(Parameter.DOUBLE))
				return new Double(value);
			else if (type.equalsIgnoreCase(Parameter.LONG))
				return new Long(value);
			else if (type.equalsIgnoreCase(Parameter.SHORT))
				return new Short(value);
			else if (type.equalsIgnoreCase(Parameter.BYTE))
				return new Byte(value);
			else if (type.equalsIgnoreCase(Parameter.BOOLEAN))
				return new Boolean(value);
			else if (type.equalsIgnoreCase(Parameter.CHAR))
				return new Character(value.charAt(0));
			else if (type.equalsIgnoreCase(Parameter.STRING))
				return value;
			else
				return new Object();
		} catch (Exception e) {
			// logger.error("Error creating parameter object for operation : "
			// + e.getMessage());
			return null;
		}

	}

	private String getObjectType(Parameter parameter) {

		try {
			String type = parameter.getType();
			if (type.equalsIgnoreCase(Parameter.INT))
				return "int";
			else if (type.equalsIgnoreCase(Parameter.FLOAT))
				return "float";
			else if (type.equalsIgnoreCase(Parameter.DOUBLE))
				return "double";
			else if (type.equalsIgnoreCase(Parameter.LONG))
				return "long";
			else if (type.equalsIgnoreCase(Parameter.SHORT))
				return "short";
			else if (type.equalsIgnoreCase(Parameter.BYTE))
				return "byte";
			else if (type.equalsIgnoreCase(Parameter.BOOLEAN))
				return "boolean";
			else if (type.equalsIgnoreCase(Parameter.CHAR))
				return "char";
			else if (type.equalsIgnoreCase(Parameter.STRING))
				return "java.lang.String";
			else
				return null;
		} catch (Exception e) {
			// logger.error("Error creating signature object for operation : "
			// + e.getMessage());
			return null;
		}
	}

}
