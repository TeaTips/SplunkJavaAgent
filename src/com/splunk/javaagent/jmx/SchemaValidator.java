package com.splunk.javaagent.jmx;

import java.net.URL;

import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Vaildate an XML file against config.xsd
 * 
 * Damien Dallimore damien@dtdsoftware.com
 * 
 */
public class SchemaValidator {

	/**
	 * Validate XML source
	 * 
	 * @param xml
	 * @throws Exception
	 *             if validation fails
	 */
	public void validateSchema(InputSource xml) throws Exception {

		SAXParser parser = new SAXParser();
		try {

			parser.setFeature("http://xml.org/sax/features/validation", true);
			parser.setFeature(
					"http://apache.org/xml/features/validation/schema", true);
			parser.setFeature(
					"http://apache.org/xml/features/validation/schema-full-checking",
					true);

			// config.xsd is on the classpath in jmxpoller.jar
			URL schemaUrl = SchemaValidator.class
					.getResource("/com/splunk/javaagent/jmx/config.xsd");

			parser.setProperty(
					"http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation",
					schemaUrl.toString());
			Validator handler = new Validator();
			parser.setErrorHandler(handler);

			parser.parse(xml);

			if (handler.validationError == true)
				throw new Exception("XML has a validation error:"
						+ handler.validationError + ""
						+ handler.saxParseException.getMessage());
			else
				return;
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}

	}

	/**
	 * Validation handler
	 * 
	 */
	private class Validator extends DefaultHandler {
		public boolean validationError = false;

		public SAXParseException saxParseException = null;

		public void error(SAXParseException exception) throws SAXException {
			validationError = true;
			saxParseException = exception;
		}

		public void fatalError(SAXParseException exception) throws SAXException {
			validationError = true;
			saxParseException = exception;
		}

		public void warning(SAXParseException exception) throws SAXException {
		}
	}

}
