package com.splunk.javaagent;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

public class MethodTracerAdaptor extends AdviceAdapter {

	private String cName;
	private String mName;
	private String desc;

	public MethodTracerAdaptor(String owner, String name, MethodVisitor mv,
			String desc, int access) {

		super(Opcodes.ASM4, mv, access, name, desc);
		this.mName = name;
		this.mv = mv;
		this.cName = owner;
		this.desc = desc;

	}

	@Override
	public void visitCode() {
		try {
			super.visitCode();
			super.visitLdcInsn(cName);
			super.visitLdcInsn(mName);
			super.visitLdcInsn(desc);
			super.visitMethodInsn(Opcodes.INVOKESTATIC,
					"com/splunk/javaagent/SplunkJavaAgent", "methodEntered",
					"(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
		} catch (Exception e) {
		}
	}

	@Override
	public void visitInsn(int opcode) {

		try {

			if (opcode == Opcodes.ATHROW) {
				// get the Throwable object off the stack
				super.visitInsn(Opcodes.DUP);

				int exceptionVar = newLocal(Type.getType(Throwable.class));
				super.visitVarInsn(Opcodes.ASTORE, exceptionVar);

				super.visitLdcInsn(cName);
				super.visitLdcInsn(mName);
				super.visitLdcInsn(desc);
				super.visitVarInsn(ALOAD, exceptionVar);

				super.visitMethodInsn(
						Opcodes.INVOKESTATIC,
						"com/splunk/javaagent/SplunkJavaAgent",
						"throwableCaught",
						"(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)V");
			}

			if (opcode == Opcodes.IRETURN || opcode == Opcodes.FRETURN
					|| opcode == Opcodes.RETURN || opcode == Opcodes.ARETURN
					|| opcode == Opcodes.LRETURN || opcode == Opcodes.DRETURN) {

				super.visitLdcInsn(cName);
				super.visitLdcInsn(mName);
				super.visitLdcInsn(desc);
				super.visitMethodInsn(Opcodes.INVOKESTATIC,
						"com/splunk/javaagent/SplunkJavaAgent", "methodExited",
						"(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
			}

			super.visitInsn(opcode);

		} catch (Exception e) {
		}
	}

	@Override
	public void visitMaxs(int maxStack, int maxLocals) {

		// will be overridden by COMPUTE_MAXS
		super.visitMaxs(0, 0);

	}
}
