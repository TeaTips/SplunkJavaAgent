package com.splunk.javaagent;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import org.objectweb.asm.Opcodes;

public class ClassTracerAdaptor extends ClassVisitor {

	private String className;
	private boolean isInterface;

	public ClassTracerAdaptor(ClassVisitor cv) {
		super(Opcodes.ASM4, cv);
	}

	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		cv.visit(version, access, name, signature, superName, interfaces);
		className = name;
		isInterface = (access & Opcodes.ACC_INTERFACE) != 0;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
		MethodVisitor mv = cv.visitMethod(access, name, desc, signature,
				exceptions);
		if (!isInterface && mv != null && !name.equals("<init>")
				&& !SplunkJavaAgent.isBlackListed(className, name)
				&& SplunkJavaAgent.isWhiteListed(className, name)) {
			mv = new MethodTracerAdaptor(className, name, mv);
		}
		return mv;
	}

}
