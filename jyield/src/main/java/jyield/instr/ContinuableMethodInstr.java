/**
 * 
 */
package jyield.instr;

import static org.objectweb.asm.Opcodes.ACC_PROTECTED;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DLOAD;
import static org.objectweb.asm.Opcodes.DSTORE;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.FLOAD;
import static org.objectweb.asm.Opcodes.FSTORE;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.ISTORE;
import static org.objectweb.asm.Opcodes.LLOAD;
import static org.objectweb.asm.Opcodes.LSTORE;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.SIPUSH;
import static org.objectweb.asm.Opcodes.SWAP;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jyield.Continuation;
import jyield.Yield;
import jyield.runtime.YieldContextImpl;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.Value;

final class ContinuableMethodInstr extends MethodAdapter {
	private static final String YIELD_CONTEXT_IMPL_CLASS = YieldContextImpl.class
			.getName().replace('.', '/');
	private static final String CONTINUATION_CLASS = Continuation.class
			.getName().replace('.', '/');
	private static final String RET = "ret";
	private static final String RET_DESC = "(Ljava/lang/Object;I)L"
			+ YIELD_CONTEXT_IMPL_CLASS + ";";
	private static final String SUSPEND = "suspend";
	private static final String SUSPEND_DESC = "(I)L"
			+ YIELD_CONTEXT_IMPL_CLASS + ";";
	private static final String JOIN = "join";
	private static final String JOIN_DESC1 = "(L"
			+ Iterable.class.getName().replace('.', '/') + ";I)L"
			+ YIELD_CONTEXT_IMPL_CLASS + ";";
	private static final String JOIN_DESC2 = "(L"
			+ Iterator.class.getName().replace('.', '/') + ";I)L"
			+ YIELD_CONTEXT_IMPL_CLASS + ";";
	private static final String JOIN_DESC3 = "(L"
			+ Enumeration.class.getName().replace('.', '/') + ";I)L"
			+ YIELD_CONTEXT_IMPL_CLASS + ";";
	private static final String JOIN_DESC4 = "(L" + CONTINUATION_CLASS + ";I)L"
			+ YIELD_CONTEXT_IMPL_CLASS + ";";
	private static final String JOIN_DESC_PREFIX1 = "(L"
			+ Iterable.class.getName().replace('.', '/');
	private static final String JOIN_DESC_PREFIX2 = "(L"
			+ Iterator.class.getName().replace('.', '/');
	private static final String JOIN_DESC_PREFIX3 = "(L"
			+ Enumeration.class.getName().replace('.', '/');
	private static final String JOIN_DESC_PREFIX4 = "(L"
			+ Continuation.class.getName().replace('.', '/');
	// private static final String DONE = "done";
	private static final String YIELD_CLASS = Yield.class.getName().replace(
			'.', '/');

	private String stepMethodDesc;
	private final MethodNode mn;
	private final YieldClassInstr cv;
	private Analyzer analyzer;
	private String contextClassName;
	private List<Label> retLabels = new ArrayList<Label>();
	private List<AbstractInsnNode> toRemove = new ArrayList<AbstractInsnNode>();
	private String stepMethodName;
	private boolean isStatic;

	ContinuableMethodInstr(YieldClassInstr cv, MethodNode mn) {
		super(mn);
		this.isStatic = (mn.access & ACC_STATIC) != 0;
		this.cv = cv;
		this.mn = mn;
		this.analyzer = new Analyzer(new BasicInterpreter());
	}

	@Override
	public void visitEnd() {
		super.visitEnd();
		// System.out.println(mn.name);
		contextClassName = cv.name + "_YC_" + mn.name + "_"
				+ Math.abs(mn.desc.hashCode());
		stepMethodName = mn.name + "_yc_" + Math.abs(mn.desc.hashCode());
		stepMethodDesc = "(L" + YIELD_CONTEXT_IMPL_CLASS + ";"
				+ mn.desc.substring(mn.desc.indexOf(')'));

		try {
			analyzer.analyze(cv.name, mn);
		} catch (AnalyzerException e) {
			// /throw new RuntimeException(e);
		}
		emitReplacedMethod();
		emitChangedMethod();

		createYieldContextClass();
	}

	private void createYieldContextClass() {
		ClassWriter ctxw = new ClassWriter(ClassWriter.COMPUTE_FRAMES
				| ClassWriter.COMPUTE_MAXS);
		ctxw.visit(cv.version, ACC_PUBLIC, contextClassName, null,
				YIELD_CONTEXT_IMPL_CLASS, null);
		ctxw.visitEnd();
		MethodVisitor cmv = ctxw.visitMethod(ACC_PUBLIC, "<init>",
				"(ILjava/lang/Object;)V", null, null);
		cmv.visitVarInsn(ALOAD, 0);
		cmv.visitVarInsn(ILOAD, 1);
		cmv.visitVarInsn(ALOAD, 2);
		cmv.visitMethodInsn(INVOKESPECIAL, YIELD_CONTEXT_IMPL_CLASS, "<init>",
				"(ILjava/lang/Object;)V");
		cmv.visitInsn(RETURN);
		cmv.visitMaxs(3, 3);

		cmv = ctxw.visitMethod(ACC_PROTECTED, "doStep",
				"()Ljava/util/Enumeration;", null, null);
		cmv.visitVarInsn(ALOAD, 0);
		if (!isStatic) {
			cmv.visitFieldInsn(GETFIELD, YIELD_CONTEXT_IMPL_CLASS, "target",
					"Ljava/lang/Object;");
			cmv.visitTypeInsn(CHECKCAST, cv.name);
		}
		cmv.visitVarInsn(ALOAD, 0);
		if (!isStatic) {
			cmv.visitMethodInsn(INVOKEVIRTUAL, cv.name, stepMethodName,
					stepMethodDesc);
		} else {
			cmv.visitMethodInsn(INVOKESTATIC, cv.name, stepMethodName,
					stepMethodDesc);
		}
		cmv.visitInsn(ARETURN);
		cmv.visitMaxs(3, 3);
		cv.createdClasses.put(contextClassName, ctxw.toByteArray());
		// cv.yinstr.loadClass(cv.loader, contextClassName.replace('/', '.'),
		// ctxw
		// .toByteArray());
	}

	@SuppressWarnings("unchecked")
	public void emitChangedMethod() {
		MethodVisitor fmv = cv.underliningVisitor().visitMethod(
				mn.access | ACC_PUBLIC,
				stepMethodName,
				stepMethodDesc,
				null,
				(String[]) mn.exceptions.toArray(new String[mn.exceptions
						.size()]));
		int ctxIdx = mn.maxLocals + 1;
		fmv.visitVarInsn(ALOAD, isStatic ? 0 : 1);
		fmv.visitVarInsn(ASTORE, ctxIdx);
		fmv.visitVarInsn(ALOAD, ctxIdx);
		fmv.visitMethodInsn(INVOKEVIRTUAL, YIELD_CONTEXT_IMPL_CLASS,
				"getNextLine", "()I");

		if (retLabels.size() > 0) {
			Label start = new Label();
			fmv.visitTableSwitchInsn(1, retLabels.size(), start, retLabels
					.toArray(new Label[retLabels.size()]));
			fmv.visitLabel(start);
		}
		emitLoadStoreLocals(true, false, ctxIdx, fmv, null, null, analyzer
				.getFrames()[0]);

		Map<LabelNode, Frame> labelFrames = new HashMap<LabelNode, Frame>();

		InsnList instructions = mn.instructions;
		for (int i = 0; i < instructions.size(); i++) {
			AbstractInsnNode insn = instructions.get(i);
			if (insn instanceof LabelNode) {
				Frame f = analyzer.getFrames()[i];
				labelFrames.put((LabelNode) insn, f);
			}
		}

		for (int i = 0; i < retLabels.size(); i++) {
			LabelNode ln = (LabelNode) retLabels.get(i).info;
			MethodInsnNode mn = (MethodInsnNode) ln.getPrevious();
			instructions.insertBefore(ln, new VarInsnNode(ALOAD, ctxIdx));
			if (!SUSPEND.equals(mn.name)) {
				instructions.insertBefore(ln, new InsnNode(SWAP));
			}
			instructions.insertBefore(ln, new IntInsnNode(SIPUSH, i + 1));
			if (RET.equals(mn.name)) {
				instructions.insertBefore(ln, new MethodInsnNode(INVOKEVIRTUAL,
						YIELD_CONTEXT_IMPL_CLASS, RET, RET_DESC));
			} else if (SUSPEND.equals(mn.name)) {
				instructions.insertBefore(ln, new MethodInsnNode(INVOKEVIRTUAL,
						YIELD_CONTEXT_IMPL_CLASS, SUSPEND, SUSPEND_DESC));
			} else if (JOIN.equals(mn.name)) {
				if (mn.desc.startsWith(JOIN_DESC_PREFIX1))
					instructions.insertBefore(ln, new MethodInsnNode(
							INVOKEVIRTUAL, YIELD_CONTEXT_IMPL_CLASS, JOIN,
							JOIN_DESC1));
				else if (mn.desc.startsWith(JOIN_DESC_PREFIX2))
					instructions.insertBefore(ln, new MethodInsnNode(
							INVOKEVIRTUAL, YIELD_CONTEXT_IMPL_CLASS, JOIN,
							JOIN_DESC2));
				else if (mn.desc.startsWith(JOIN_DESC_PREFIX3))
					instructions.insertBefore(ln, new MethodInsnNode(
							INVOKEVIRTUAL, YIELD_CONTEXT_IMPL_CLASS, JOIN,
							JOIN_DESC3));
				else if (mn.desc.startsWith(JOIN_DESC_PREFIX4))
					instructions.insertBefore(ln, new MethodInsnNode(
							INVOKEVIRTUAL, YIELD_CONTEXT_IMPL_CLASS, JOIN,
							JOIN_DESC4));
			}
			Frame f = labelFrames.get(ln);
			instructions.insert(ln, new VarInsnNode(ALOAD, ctxIdx));
			emitLoadStoreLocals(true, true, ctxIdx, null, instructions, ln, f);
			instructions.insertBefore(ln, new InsnNode(ARETURN));
		}

		for (AbstractInsnNode insn : toRemove) {
			instructions.remove(insn);
		}
		for (AbstractInsnNode inst = instructions.getFirst(); inst != null; inst = inst
				.getNext()) {
			inst.accept(fmv);
		}
		fmv.visitInsn(ACONST_NULL);
		fmv.visitInsn(ARETURN);
		for (int i = 0; i < mn.localVariables.size(); i++) {
			LocalVariableNode n = (LocalVariableNode) mn.localVariables.get(i);
			n.accept(fmv);
		}
		// fmv.visitMaxs(3 + mn.maxStack, 4 + mn.maxLocals);
		fmv.visitMaxs(3 + mn.maxStack, 4 + mn.maxLocals);
		fmv.visitEnd();
	}

	private void emitLoadStoreLocals(boolean emitLoad, boolean emitStore,
			int contextLocalIdx, MethodVisitor fmv, InsnList instr,
			AbstractInsnNode baseInstruction, Frame frame) {
		// System.out.println(":::");
		for (int j = (isStatic ? 0 : 1), ls = frame.getLocals(); j < ls; j++) {
			Value local = frame.getLocal(j);
			// System.out.println(local);
			int bLoadOpc;
			String bStoreFunc, bStoreDesc;
			int aStoreOpc;
			String aLoadFunc, aLoadDesc;
			if (local instanceof BasicValue) {
				if (local == BasicValue.INT_VALUE) {
					bLoadOpc = ILOAD;
					bStoreFunc = "storeInt";
					bStoreDesc = "(II)V";
					aStoreOpc = ISTORE;
					aLoadFunc = "loadInt";
					aLoadDesc = "(I)I";
				} else if (local == BasicValue.REFERENCE_VALUE) {
					bLoadOpc = ALOAD;
					bStoreFunc = "storeObject";
					bStoreDesc = "(ILjava/lang/Object;)V";
					aStoreOpc = ASTORE;
					aLoadFunc = "loadObject";
					aLoadDesc = "(I)Ljava/lang/Object;";
				} else if (local == BasicValue.FLOAT_VALUE) {
					bLoadOpc = FLOAD;
					bStoreFunc = "storeFloat";
					bStoreDesc = "(IF)V";
					aStoreOpc = FSTORE;
					aLoadFunc = "loadFloat";
					aLoadDesc = "(I)F";
				} else if (local == BasicValue.DOUBLE_VALUE) {
					bLoadOpc = DLOAD;
					bStoreFunc = "storeDouble";
					bStoreDesc = "(ID)V";
					aStoreOpc = DSTORE;
					aLoadFunc = "loadDouble";
					aLoadDesc = "(I)D";
				} else if (local == BasicValue.LONG_VALUE) {
					bLoadOpc = LLOAD;
					bStoreFunc = "storeLong";
					bStoreDesc = "(IJ)V";
					aStoreOpc = LSTORE;
					aLoadFunc = "loadLong";
					aLoadDesc = "(I)J";
				} else
					continue;
			} else
				continue;
			if (emitLoad) {
				if (fmv != null) {
					fmv.visitVarInsn(ALOAD, contextLocalIdx);
					fmv.visitIntInsn(SIPUSH, j);
					fmv.visitMethodInsn(INVOKEVIRTUAL,
							YIELD_CONTEXT_IMPL_CLASS, aLoadFunc, aLoadDesc);
					fmv.visitVarInsn(aStoreOpc, j);
				} else {
					// don't mind this nightmarish inverted order
					instr
							.insert(baseInstruction, new VarInsnNode(aStoreOpc,
									j));
					instr.insert(baseInstruction, new MethodInsnNode(
							INVOKEVIRTUAL, YIELD_CONTEXT_IMPL_CLASS, aLoadFunc,
							aLoadDesc));
					instr.insert(baseInstruction, new IntInsnNode(SIPUSH, j));
					instr.insert(baseInstruction, new VarInsnNode(ALOAD,
							contextLocalIdx));
				}
			}
			if (emitStore) {
				if (fmv != null) {
					fmv.visitVarInsn(ALOAD, contextLocalIdx);
					fmv.visitIntInsn(SIPUSH, j);
					fmv.visitVarInsn(bLoadOpc, j);
					fmv.visitMethodInsn(INVOKEVIRTUAL,
							YIELD_CONTEXT_IMPL_CLASS, bStoreFunc, bStoreDesc);
				} else {
					instr.insertBefore(baseInstruction, new VarInsnNode(ALOAD,
							contextLocalIdx));
					instr.insertBefore(baseInstruction, new IntInsnNode(SIPUSH,
							j));
					instr.insertBefore(baseInstruction, new VarInsnNode(
							bLoadOpc, j));
					instr.insertBefore(baseInstruction, new MethodInsnNode(
							INVOKEVIRTUAL, YIELD_CONTEXT_IMPL_CLASS,
							bStoreFunc, bStoreDesc));
				}
			}

		}
	}

	@SuppressWarnings("unchecked")
	public void emitReplacedMethod() {
		MethodVisitor fmv = cv.underliningVisitor().visitMethod(
				mn.access,
				mn.name,
				mn.desc,
				mn.signature,
				(String[]) mn.exceptions.toArray(new String[mn.exceptions
						.size()]));
		fmv.visitTypeInsn(NEW, contextClassName);
		fmv.visitInsn(DUP);
		fmv.visitIntInsn(SIPUSH, mn.maxLocals);
		if (0 == (mn.access & ACC_STATIC)) {
			fmv.visitVarInsn(ALOAD, 0);
		} else {
			fmv.visitInsn(ACONST_NULL);
		}
		fmv.visitMethodInsn(INVOKESPECIAL, contextClassName, "<init>",
				"(ILjava/lang/Object;)V");

		int ctxIdx = mn.maxLocals + 1;
		fmv.visitVarInsn(ASTORE, ctxIdx);
		Frame frame = analyzer.getFrames()[0];
		emitLoadStoreLocals(false, true, ctxIdx, fmv, null, null, frame);

		fmv.visitVarInsn(ALOAD, ctxIdx);
		fmv.visitInsn(ARETURN);
		fmv.visitMaxs(2, mn.localVariables.size() + 2);
		fmv.visitEnd();
	}

	@Override
	public void visitMethodInsn(int opcode, String owner, String name,
			String desc) {
		super.visitMethodInsn(opcode, owner, name, desc);
		if (opcode == INVOKESTATIC) {
			if (owner.equals(YIELD_CLASS)) {
				if (RET.equals(name) || JOIN.equals(name)) {
					toRemove.add(mn.instructions.getLast());
					Label label = new Label();
					retLabels.add(label);
					visitLabel(label);
				}
			} else if (owner.equals(CONTINUATION_CLASS)) {
				if (SUSPEND.equals(name) || JOIN.equals(name)) {
					toRemove.add(mn.instructions.getLast());
					Label label = new Label();
					retLabels.add(label);
					visitLabel(label);
				}
			}
		}
	}
}