/**
 * 
 */
package jyield.instr;

import java.util.Enumeration;
import java.util.Iterator;

import jyield.Continuable;
import jyield.Continuation;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.EmptyVisitor;

/**
 * Tastes classes to find out if they need the jyield instrumentation.
 * <p>
 * Used during online and offline bytecode weaving.
 * 
 * @author Daniel Sperry - 2010
 */
final class ClassTaster extends EmptyVisitor {
	static final String LJYIELD_CONTINUABLE = "L"
			+ Continuable.class.getName().replace('.', '/') + ";";

	// method sulfixes that jyield searches for
	static final String SUFFIX1 = ")L"
			+ Enumeration.class.getName().replace('.', '/') + ";";
	static final String SUFFIX2 = ")L"
			+ Iterable.class.getName().replace('.', '/') + ";";
	static final String SUFFIX3 = ")L"
			+ Iterator.class.getName().replace('.', '/') + ";";
	static final String SUFFIX4 = ")L"
			+ Continuation.class.getName().replace('.', '/') + ";";

	/**
	 * Has found the right return type on a method (using the sulfixes)?
	 */
	boolean rightReturn;
	/**
	 * Has found the right return type on a method?
	 */
	boolean rightAnnotation;

	/**
	 * Visited class name.
	 */
	String name;

	/**
	 * @return true if the visited class should be instrumented.
	 */
	public boolean isShouldInstrument() {
		return rightReturn && rightAnnotation;
	}

	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		this.name = name;
		super.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
		if (isValidDesc(desc)) {
			rightReturn = true;
		}
		return super.visitMethod(access, name, desc, signature, exceptions);
	}

	/**
	 * Checks a method descriptio for the valid sulfixes.
	 * 
	 * @param desc
	 *            jvm method description.
	 * @return true if this method description has one of the valid sulfixes.
	 */
	public static boolean isValidDesc(String desc) {
		return desc.endsWith(SUFFIX1) || desc.endsWith(SUFFIX2)
				|| desc.endsWith(SUFFIX3) || desc.endsWith(SUFFIX4);
	}

	/**
	 * Locates methods annotated with @Continuable annotation.
	 */
	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		if (LJYIELD_CONTINUABLE.equals(desc)) {
			rightAnnotation = true;
		}
		return super.visitAnnotation(desc, visible);
	}
}