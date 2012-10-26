package com.splunk.javaagent;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class MethodTracerAdaptor extends MethodVisitor {

	private String cName;
	private String mName;

	public MethodTracerAdaptor(String owner, String name, MethodVisitor mv) {

		super(Opcodes.ASM4, mv);

		this.mName = name;
		this.mv = mv;
		this.cName = owner;

	}

	@Override
	public void visitCode() {
		try {
			mv.visitCode();
			mv.visitLdcInsn(cName);
			mv.visitLdcInsn(mName);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC,
					"com/splunk/javaagent/SplunkJavaAgent", "methodEntered",
					"(Ljava/lang/String;Ljava/lang/String;)V");
		} catch (Exception e) {
		}
	}

	@Override
	public void visitInsn(int opcode) {

		try {

			if (opcode == Opcodes.ATHROW) {
				mv.visitLdcInsn(cName);
				mv.visitLdcInsn(mName);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC,
						"com/splunk/javaagent/SplunkJavaAgent",
						"throwableCaught",
						"(Ljava/lang/String;Ljava/lang/String;)V");
			}

			if (opcode == Opcodes.IRETURN || opcode == Opcodes.FRETURN
					|| opcode == Opcodes.RETURN || opcode == Opcodes.ARETURN
					|| opcode == Opcodes.LRETURN || opcode == Opcodes.DRETURN) {

				mv.visitLdcInsn(cName);
				mv.visitLdcInsn(mName);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC,
						"com/splunk/javaagent/SplunkJavaAgent", "methodExited",
						"(Ljava/lang/String;Ljava/lang/String;)V");
			}

			mv.visitInsn(opcode);

		} catch (Exception e) {
		}
	}

	@Override
	public void visitMaxs(int maxStack, int maxLocals) {
		mv.visitMaxs(maxStack + 4, maxLocals);
	}
}
