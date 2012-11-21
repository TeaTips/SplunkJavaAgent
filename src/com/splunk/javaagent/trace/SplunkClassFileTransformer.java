package com.splunk.javaagent.trace;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import com.splunk.javaagent.SplunkJavaAgent;

public class SplunkClassFileTransformer implements ClassFileTransformer {

	public SplunkClassFileTransformer() {
	}

	@Override
	public byte[] transform(ClassLoader loader, String className,
			Class classBeingRedefined, ProtectionDomain protectionDomain,
			byte[] classFileBuffer) throws IllegalClassFormatException {

		if (this.getClass().getClassLoader().equals(loader)) {

			if (!SplunkJavaAgent.isBlackListed(className)
					&& SplunkJavaAgent.isWhiteListed(className))
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

		SplunkJavaAgent.classLoaded(className);
		ClassReader cr = new ClassReader(classFileBuffer);
		ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
		ClassTracerAdaptor ca = new ClassTracerAdaptor(cw);
		cr.accept(ca, ClassReader.SKIP_FRAMES);
		return cw.toByteArray();

	}

}
