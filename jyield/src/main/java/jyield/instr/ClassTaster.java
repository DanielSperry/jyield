/**
 * 
 */
package jyield.instr;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.EmptyVisitor;

final class ClassTaster extends EmptyVisitor {
	boolean rightReturn;
	boolean rightAnnotation;

	public boolean isShouldInstrument() {
		return rightReturn && rightAnnotation;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
		if (!desc.endsWith(YieldInstrumentation.SUFFIX)) {
			rightReturn = true;
		}
		return super.visitMethod(access, name, desc, signature, exceptions);
	}

	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		if ("Ljyield/Continuable;".equals(desc)) {
			rightAnnotation = true;
		}
		return super.visitAnnotation(desc, visible);
	}
}