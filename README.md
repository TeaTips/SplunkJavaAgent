## Splunk Java Agent v0.1

## Overview


This JVM(Java Virtual Machine)agent can be used to obtain metrics pertaining to :

1. class loading
2. method execution 
3. method timings (cumulative, min, avg, max, std deviation)
4. method call tracing(count of calls, group by app/app node(for clustered systems)/thread/clqss/package)
5. application/thread stalls
6. errors/exceptions/throwables
7. JVM heap analysis, object allocation count/size, leak detection

It is able to obtain these metrics dynamically at runtime by "weaving" the necessary bytecode into loaded classes using the Java instrumentation API and the ASM framework.
There is no source code changes required by the end user and no class files on disk are altered.
By default , the metrics will be streamed directly into Splunk over TCP, however the transport mechanism is configurable and extensible.

In the Splunk UI you can then create Splunk searches over the agent data and visualizations, reports,alerts etc.. over the results of the searches.
The events are already being fed into Splunk in best practice semantic format, key=value pairs , no additional field extractions are required.
As Splunk is being using to index all the data and perform searches(real time if you wish) massive amounts of tracing data from as many JVMs as you need to monitor can be indexed and correlated and you can leverage all of the scalability and HA features of the Splunk platform.


## Supported Java Runtime


1. JRE 5+
2. JVMs : Hotspot , JRockit, OpenJDK, IBM J9

## Install


1. Uncompress splunkagent.tar.gz
2. The agent is just a single jar file, splunkagent.jar, you should see this in the uncompressed directory.

## Setup


Pass the follow argument to your JVM at startup:

-javaagent:splunkagent.jar

or 

-javaagent:lib/splunkagent.jar

etc..

The location of the jar file should be relative to the directory where you are executing "java" from.

All dependencies and resources are bundled into the jar file.

## Configuration


You can configure the agent with the properties file "splunkagent.properties" that resides inside the jar file.
The various options are detailed below.
Open the jar , edit the file, close the jar.

Note : unless you want incredibly verbose tracing , you will want to specify just the packages/classes/methods you are interested in profiling in the "agent.whitelist" property


## Properties Options


splunk.transport.impl : fully qualified class name, an implementation of the "com.splunk.javaagent.transport.SplunkTransport" interface
splunk.transport.tcp.host : Splunk host name for the SplunkTCPTransport
splunk.transport.tcp.port : Splunk TCP port for the SplunkTCPTransport
splunk.transport.tcp.maxQueueSize : defaults to 500K , format [<integer>|<integer>[KB|MB|GB]]
splunk.transport.tcp.dropEventsOnQueueFull : true | false , if true then the queue will get emptied when it fills up to accommodate new data.
agent.whitelist : comma delimited string of patterns, see below
agent.blacklist comma delimited string of patterns, see below
agent.app.name : name of the application ie: Tomcat
agent.app.instance : instance identifier of the application ie: might be a node id in a cluster
agent.userEventTags : comma delimited list of user defined key=value pairs to add to events sent to Splunk
trace.methodEntered : true | false
trace.methodExited : true | false
trace.classLoaded : true | false
trace.errors : true | false


## Whitelist/Blacklist Patterns


Partial package name : com/splunk/
Full package name : com/splunk/javaagent/test/
Fully qualified class : com/splunk/javaagent/test/MyClass
Fully qualified class and method : com/splunk/javaagent/test/MyClass:someMethod

## Contact

This project was initiated by Damien Dallimore
<table>

<tr>
<td><em>Email</em></td>
<td>ddallimore@splunk.com</td>
</tr>

<tr>
<td><em>Twitter</em>
<td>@damiendallimore</td>
</tr>


</table>

