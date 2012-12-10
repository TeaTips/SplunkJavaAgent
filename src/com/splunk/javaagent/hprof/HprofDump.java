package com.splunk.javaagent.hprof;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.util.HashMap;
import java.util.Map;

import com.splunk.javaagent.SplunkJavaAgent;

public class HprofDump {

	HprofHeader header;
	File hprofFile;
	Map nameMap;
	Map classNameMap;
	Map classMap;

	public HprofDump(File hprofFile) {

		this.hprofFile = hprofFile;
		this.nameMap = new HashMap();
		this.classNameMap = new HashMap();
		this.classMap = new HashMap();
	}

	public void process() {
		RandomAccessFile raf = null;
		if (hprofFile.exists()) {

			try {
				raf = new RandomAccessFile(hprofFile, "r");
				MappedByteBuffer buf = raf.getChannel().map(
						java.nio.channels.FileChannel.MapMode.READ_ONLY, 0L,
						hprofFile.length());

				this.header = new HprofHeader(buf);

				while (buf.position() < buf.limit()) {

					byte tag = buf.get();
					int elapsedTimeMicroSeconds = buf.getInt();
					int recordLength = buf.getInt();

					HprofRecord record = HprofRecord.create(this, tag,
							elapsedTimeMicroSeconds, recordLength, buf);
					if (record != null) {
						if (record instanceof HeapDumpRecord) {
							// do nothing
						} else {
							SplunkJavaAgent.hprofRecordEvent(tag, (byte) 0,
									record.getSplunkLogEvent());
						}

					}
				}
			} catch (Throwable t) {

			} finally {
				if (raf != null) {
					try {
						raf.close();
					} catch (Exception e) {
					}
				}
			}
		}
	}

	public HprofHeader getHeader() {
		return header;
	}

}
