/**
 * 
 */
package jyield.instr;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.MethodNode;

public class YieldClassInstr extends ClassAdapter {
	int version;
	String name;
	final Map<String, byte[]> createdClasses = new HashMap<String, byte[]>();

	YieldClassInstr(ClassVisitor cv) {
		super(cv);
	}

	public ClassVisitor underliningVisitor() {
		return cv;
	}

	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		this.version = version;
		this.name = name;
		super.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
		if (!ClassTaster.isValidDesc(desc)) {
			return super.visitMethod(access, name, desc, signature, exceptions);
		}

		return new ContinuableMethodInstr(this, new MethodNode(access, name,
				desc, signature, exceptions));
	}

}