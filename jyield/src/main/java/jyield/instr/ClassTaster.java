/**
 * 
 */
package jyield.instr;

import java.util.Enumeration;
import java.util.Iterator;

import jyield.Continuable;
import jyield.runtime.YieldContext;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.EmptyVisitor;

final class ClassTaster extends EmptyVisitor {
	static final String LJYIELD_CONTINUABLE = "L"
			+ Continuable.class.getName().replace('.', '/') + ";";

	static final String SUFFIX1 = ")L"
			+ Enumeration.class.getName().replace('.', '/') + ";";
	static final String SUFFIX2 = ")L"
			+ Iterable.class.getName().replace('.', '/') + ";";
	static final String SUFFIX3 = ")L"
			+ Iterator.class.getName().replace('.', '/') + ";";
	static final String SUFFIX4 = ")L"
			+ YieldContext.class.getName().replace('.', '/') + ";";

	boolean rightReturn;
	boolean rightAnnotation;

	public boolean isShouldInstrument() {
		return rightReturn && rightAnnotation;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
		if (isValidDesc(desc)) {
			rightReturn = true;
		}
		return super.visitMethod(access, name, desc, signature, exceptions);
	}

	public static boolean isValidDesc(String desc) {
		return desc.endsWith(SUFFIX1) || desc.endsWith(SUFFIX2)
				|| desc.endsWith(SUFFIX3) || desc.endsWith(SUFFIX4);
	}

	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		if (LJYIELD_CONTINUABLE.equals(desc)) {
			rightAnnotation = true;
		}
		return super.visitAnnotation(desc, visible);
	}
}