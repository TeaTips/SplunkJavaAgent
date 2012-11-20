package com.splunk.javaagent.jmx.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * POJO for a JMX Server
 * 
 * @author Damien Dallimore damien@dtdsoftware.com
 * 
 */
public class JMXServer {

	// private static Logger logger = LoggerFactory.getLogger(JMXServer.class);

	// PID of locally running JVM
	public int processID;

	// PID File of locally running JVM
	public String pidFile;

	// PID Command to obtain PID of locally running JVM
	public String pidCommand;

	// Where a command returns multiple PIDs,store the additional PIDs in a List
	public List<Integer> additionalPIDsFromCommand;

	// JMX hostname, dns alias, ip address
	public String host = "";

	// meta data description of the JVM being connected to
	public String jvmDescription;

	// remote JMX port
	public int jmxport;

	// JMX username
	public String jmxuser = "";

	// JMX password
	public String jmxpass = "";

	// JMX connector protocol
	public String protocol = "rmi"; // default

	// Remote stub lookup path
	public String lookupPath = "";

	// Remote stub source
	public String stubSource = "jndi"; // default

	// Encoded stub
	public String encodedStub = ""; // for "stub" and "ior" stub sources

	// Raw URL input, optional if the pre set fields dont suffice
	public String jmxServiceURL = "";

	// list of MBeans/MBeans Patterns to Query
	public List<MBean> mbeans;

	public JMXServer() {
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getJvmDescription() {
		return jvmDescription;
	}

	public void setJvmDescription(String jvmDescription) {

		this.jvmDescription = jvmDescription;
	}

	public int getJmxport() {
		return jmxport;
	}

	public void setJmxport(int jmxport) {
		this.jmxport = jmxport;
	}

	public String getJmxuser() {
		return jmxuser;
	}

	public void setJmxuser(String jmxuser) {
		this.jmxuser = jmxuser;
	}

	public String getJmxpass() {
		return jmxpass;
	}

	public void setJmxpass(String jmxpass) {
		this.jmxpass = jmxpass;
	}

	public String getEncodedStub() {
		return encodedStub;
	}

	public void setEncodedStub(String encodedStub) {
		this.encodedStub = encodedStub;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getLookupPath() {
		return lookupPath;
	}

	public void setLookupPath(String lookupPath) {
		this.lookupPath = lookupPath;
	}

	public String getStubSource() {
		return stubSource;
	}

	public void setStubSource(String stubSource) {
		this.stubSource = stubSource;
	}

	public String getJmxServiceURL() {
		return jmxServiceURL;
	}

	public void setJmxServiceURL(String jmxServiceURL) {
		this.jmxServiceURL = jmxServiceURL;
	}

	public List<MBean> getMbeans() {
		return mbeans;
	}

	public void setMbeans(List<MBean> mbeans) {

		if (this.mbeans != null) {
			this.mbeans.addAll(mbeans);
		} else
			this.mbeans = mbeans;
	}

	public int getProcessID() {
		return processID;
	}

	public void setProcessID(int processID) {
		this.processID = processID;
	}

	public String getPidFile() {
		return pidFile;
	}

	/**
	 * Attempts to obtain the pid from a pidFile
	 * 
	 * @param pidFile
	 */
	public void setPidFile(String pidFile) {
		this.pidFile = pidFile;
		BufferedReader br = null;
		try {
			File f = new File(pidFile);
			br = new BufferedReader(new FileReader(f));
			String line = br.readLine();
			setProcessID(Integer.parseInt(line.trim()));
		} catch (Exception e) {
			// logger.error("Error obtaining pid from file " + pidFile);
		} finally {
			if (br != null)
				try {
					br.close();
				} catch (Exception e) {
					// logger.error("Error closing file stream");
				}
		}

	}

	public String getPidCommand() {
		return pidCommand;
	}

	/**
	 * Attempts to set the PID from the output of a command
	 * 
	 * @param pidCommand
	 */
	public void setPidCommand(String pidCommand) {
		this.pidCommand = pidCommand;
		BufferedReader input = null;
		try {

			Process process = Runtime.getRuntime().exec(pidCommand);
			input = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			String line;
			if ((line = input.readLine()) != null) {
				setProcessID(Integer.parseInt(line.trim()));
			} else {
				throw new Exception("No command output");
			}
			// check for any additional PIDs in the command output
			while ((line = input.readLine()) != null) {
				if (additionalPIDsFromCommand == null)
					additionalPIDsFromCommand = new ArrayList<Integer>();
				additionalPIDsFromCommand.add(Integer.parseInt(line.trim()));
			}

		} catch (Exception e) {
			// logger.error("Error obtaining pid from command " + pidCommand);
		} finally {
			if (input != null)
				try {
					input.close();
				} catch (Exception e) {
					// logger.error("Error closing process output stream");
				}
		}
	}

	/**
	 * Where a command returns multiple PIDs,store the additional PIDs in a List
	 * 
	 * @return
	 */
	public List<Integer> getAdditionalPIDsFromCommand() {
		return additionalPIDsFromCommand;
	}

	/**
	 * Where a command returns multiple PIDs, we can clone other JMXServer
	 * objects from this one
	 * 
	 * @param pid
	 * @return
	 */
	public JMXServer cloneForAdditionalPID(int pid) {

		JMXServer clone = new JMXServer();
		clone.setProcessID(pid);
		clone.setHost(this.getHost());
		clone.setJvmDescription(this.getJvmDescription());
		clone.setMbeans(this.getMbeans());
		return clone;
	}

	@Override
	public String toString() {
		return "host=" + host + ", jmxServiceURL=" + jmxServiceURL
				+ ", jmxport=" + jmxport + ", jvmDescription=" + jvmDescription
				+ ", processID=" + processID;
	}

}
