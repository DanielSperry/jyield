/**
 * 
 */
package jyield.instr;

import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PROTECTED;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;
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
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.ISTORE;
import static org.objectweb.asm.Opcodes.LLOAD;
import static org.objectweb.asm.Opcodes.LSTORE;
import static org.objectweb.asm.Opcodes.MONITORENTER;
import static org.objectweb.asm.Opcodes.MONITOREXIT;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.SIPUSH;
import static org.objectweb.asm.Opcodes.SWAP;
import static org.objectweb.asm.Opcodes.V1_6;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jyield.Continuation;
import jyield.Yield;
import jyield.runtime.YieldContextImpl;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.Value;

final class ContinuableMethodInstr extends MethodAdapter {
	private static final String[] THROWABLE_EXCEPTION_LIST = new String[] { Throwable.class
			.getName().replace('.', '/') };
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
	private List<Label> retLabels = new ArrayList<Label>();
	private Set<AbstractInsnNode> toRemove = new HashSet<AbstractInsnNode>();
	private String stepMethodName;
	private boolean isStatic;
	private String contextClassName;
	private String innerClassName;
	private String accessMethodDesc;

	ContinuableMethodInstr(YieldClassInstr cv, MethodNode mn) {
		super(mn);
		this.isStatic = (mn.access & ACC_STATIC) != 0;
		this.cv = cv;
		this.mn = mn;
		this.analyzer = new Analyzer(new BasicInterpreter() {
			@Override
			public Value newValue(Type type) {
				if (type != null && type.getSort() == Type.OBJECT) {
					return new BasicValue(type);
				}
				return super.newValue(type);
			}

			@Override
			public Value merge(Value v, Value w) {
				if (v != w && v != null && w != null && !v.equals(w)) {
					Type t = ((BasicValue) v).getType();
					Type u = ((BasicValue) w).getType();
					if (t != null && u != null && t.getSort() == Type.OBJECT
							&& u.getSort() == Type.OBJECT) {
						return BasicValue.REFERENCE_VALUE;
					}
				}
				return super.merge(v, w);
			}
		}) {
			@Override
			protected Frame newFrame(Frame src) {
				return new DataFlowFrame(src);
			}

			@Override
			protected Frame newFrame(int locals, int stack) {
				return new DataFlowFrame(locals, stack);
			}
		};
	}

	@Override
	public void visitEnd() {
		super.visitEnd();
		// System.out.println(mn.name);
		stepMethodName = mn.name + "_yc_" + Math.abs(mn.desc.hashCode());
		innerClassName = mn.name + "_yc_" + Math.abs(mn.desc.hashCode());
		contextClassName = cv.name + "$" + innerClassName;

		stepMethodDesc = "(L" + YIELD_CONTEXT_IMPL_CLASS + ";"
				+ mn.desc.substring(mn.desc.indexOf(')'));

		if (isStatic)
			accessMethodDesc = "(L" + YIELD_CONTEXT_IMPL_CLASS + ";"
					+ mn.desc.substring(mn.desc.indexOf(')'));
		else
			accessMethodDesc = "(L" + cv.name + ";L" + YIELD_CONTEXT_IMPL_CLASS
					+ ";" + mn.desc.substring(mn.desc.indexOf(')'));
		try {
			analyzer.analyze(cv.name, mn);
		} catch (AnalyzerException e) {
			e.printStackTrace(System.err);
			// /throw new RuntimeException(e);
		}
		emitReplacedMethod();
		emitChangedMethod();

		createYieldContextClass();
	}

	private void createYieldContextClass() {
		ClassWriter ctxw = new ClassWriter(ClassWriter.COMPUTE_FRAMES
				| ClassWriter.COMPUTE_MAXS);

		ctxw.visit(V1_6, ACC_SUPER, contextClassName, null,
				YIELD_CONTEXT_IMPL_CLASS, null);

		// ctxw.visitSource("YieldTest_MockInstr_Test.java", null);

		ctxw.visitInnerClass(contextClassName, cv.name, innerClassName,
				ACC_PRIVATE + ACC_STATIC);

		ctxw.visit(cv.version, ACC_PUBLIC, contextClassName, null,
				YIELD_CONTEXT_IMPL_CLASS, null);
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
		cmv.visitMethodInsn(INVOKESTATIC, cv.name, "access$" + stepMethodName,
				accessMethodDesc);
		cmv.visitInsn(ARETURN);
		cmv.visitMaxs(3, 3);
		ctxw.visitEnd();
		cv.createdClasses.put(contextClassName, ctxw.toByteArray());
		// cv.yinstr.loadClass(cv.loader, contextClassName.replace('/', '.'),
		// ctxw
		// .toByteArray());
	}

	public void emitChangedMethod() {

		// accessor
		{
			MethodVisitor mv = cv.underliningVisitor().visitMethod(
					ACC_STATIC + ACC_SYNTHETIC, "access$" + stepMethodName,
					accessMethodDesc, null, THROWABLE_EXCEPTION_LIST);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitLineNumber(26, l0);
			mv.visitVarInsn(ALOAD, 0);
			if (!isStatic) {
				mv.visitVarInsn(ALOAD, 1);
				mv.visitMethodInsn(INVOKESPECIAL, cv.name, stepMethodName,
						stepMethodDesc);
			} else {
				mv.visitMethodInsn(INVOKESTATIC, cv.name, stepMethodName,
						stepMethodDesc);
			}

			mv.visitInsn(ARETURN);
			mv.visitMaxs(2, 2);
			mv.visitEnd();
		}

		Map<LabelNode, Frame> labelFrames = new HashMap<LabelNode, Frame>();

		InsnList instructions = mn.instructions;

		for (int i = 0; i < instructions.size(); i++) {
			AbstractInsnNode insn = instructions.get(i);
			if (insn instanceof LabelNode) {
				Frame f = analyzer.getFrames()[i];
				labelFrames.put((LabelNode) insn, f);
			}
		}
		int ctxIdx = mn.maxLocals + 1;
		// step
		MethodVisitor fmv = cv.underliningVisitor().visitMethod(
				(mn.access & ~(ACC_PUBLIC | ACC_PROTECTED)) | ACC_PRIVATE,
				stepMethodName, stepMethodDesc, null, THROWABLE_EXCEPTION_LIST);

		fmv.visitVarInsn(ALOAD, isStatic ? 0 : 1);
		fmv.visitVarInsn(ASTORE, ctxIdx);
		fmv.visitVarInsn(ALOAD, ctxIdx);
		fmv.visitMethodInsn(INVOKEVIRTUAL, YIELD_CONTEXT_IMPL_CLASS,
				"getNextLine", "()I");
		Label[] enterLabels = null;
		Label[] exitLabels = null;
		Map<LabelNode, Integer> sRetLabels = new HashMap<LabelNode, Integer>();
		if (retLabels.size() > 0) {
			Label start = new Label();
			enterLabels = new Label[retLabels.size()];
			exitLabels = new Label[retLabels.size()];
			for (int i = 0; i < retLabels.size(); i++) {
				enterLabels[i] = new Label();
				exitLabels[i] = new Label();
				sRetLabels.put((LabelNode) retLabels.get(i).info, i);
			}
			fmv.visitTableSwitchInsn(1, enterLabels.length, start, enterLabels);
			fmv.visitLabel(start);
			emitLoadStoreLocals(true, false, ctxIdx, fmv,
					analyzer.getFrames()[0]);
			Label defaultStart = new Label();
			fmv.visitJumpInsn(GOTO, defaultStart);
			for (int i = 0; i < enterLabels.length; i++) {
				// enter
				fmv.visitLabel(enterLabels[i]);
				Label ln = retLabels.get(i);
				Frame frame = labelFrames.get(ln.info);
				emitLoadStoreLocals(true, false, ctxIdx, fmv, frame);
				fmv.visitVarInsn(ALOAD, ctxIdx);
				fmv.visitJumpInsn(GOTO, ln);
				// exit
				fmv.visitLabel(exitLabels[i]);
				emitLoadStoreLocals(false, true, ctxIdx, fmv, frame);
				fmv.visitVarInsn(ALOAD, ctxIdx);
				fmv.visitInsn(ARETURN);

			}
			fmv.visitLabel(defaultStart);

		} else {
			emitLoadStoreLocals(true, false, ctxIdx, fmv,
					analyzer.getFrames()[0]);
		}

		for (AbstractInsnNode inst = instructions.getFirst(); inst != null; inst = inst
				.getNext()) {
			Integer idx = sRetLabels.get(inst);
			if (idx == null) {
				if (!toRemove.contains(inst)) {
					inst.accept(fmv);
				}
			} else {
				LabelNode ln = (LabelNode) inst;
				MethodInsnNode mn = (MethodInsnNode) ln.getPrevious();
				fmv.visitVarInsn(ALOAD, ctxIdx);
				if (SUSPEND.equals(mn.name)) {
					fmv.visitIntInsn(SIPUSH, idx + 1);
					fmv.visitMethodInsn(INVOKEVIRTUAL,
							YIELD_CONTEXT_IMPL_CLASS, SUSPEND, SUSPEND_DESC);
				} else {
					fmv.visitInsn(SWAP);
					fmv.visitIntInsn(SIPUSH, idx + 1);
					if (RET.equals(mn.name)) {
						fmv.visitMethodInsn(INVOKEVIRTUAL,
								YIELD_CONTEXT_IMPL_CLASS, RET, RET_DESC);
					} else if (JOIN.equals(mn.name)) {
						if (mn.desc.startsWith(JOIN_DESC_PREFIX1))
							fmv.visitMethodInsn(INVOKEVIRTUAL,
									YIELD_CONTEXT_IMPL_CLASS, JOIN, JOIN_DESC1);
						else if (mn.desc.startsWith(JOIN_DESC_PREFIX2))
							fmv.visitMethodInsn(INVOKEVIRTUAL,
									YIELD_CONTEXT_IMPL_CLASS, JOIN, JOIN_DESC2);
						else if (mn.desc.startsWith(JOIN_DESC_PREFIX3))
							fmv.visitMethodInsn(INVOKEVIRTUAL,
									YIELD_CONTEXT_IMPL_CLASS, JOIN, JOIN_DESC3);
						else if (mn.desc.startsWith(JOIN_DESC_PREFIX4))
							fmv.visitMethodInsn(INVOKEVIRTUAL,
									YIELD_CONTEXT_IMPL_CLASS, JOIN, JOIN_DESC4);
					}
				}
				fmv.visitJumpInsn(GOTO, exitLabels[idx]);
				inst.accept(fmv);
			}
		}

		for (int i = 0; i < mn.localVariables.size(); i++) {
			LocalVariableNode n = (LocalVariableNode) mn.localVariables.get(i);
			n.accept(fmv);
		}
		for (int i = 0; i < mn.tryCatchBlocks.size(); i++) {
			TryCatchBlockNode tn = (TryCatchBlockNode) mn.tryCatchBlocks.get(i);
			tn.accept(fmv);
		}
		fmv.visitMaxs(3 + mn.maxStack, 4 + mn.maxLocals);
		fmv.visitEnd();
	}

	private void emitLoadStoreLocals(boolean emitLoad, boolean emitStore,
			int contextLocalIdx, MethodVisitor fmv, Frame frame) {
		// System.out.println(":::");
		if (frame == null) {
			return;
		}
		for (int j = (isStatic ? 0 : 1), ls = frame.getLocals(); j < ls; j++) {
			BasicValue local = (BasicValue) frame.getLocal(j);
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
				} else if (local == BasicValue.REFERENCE_VALUE
						|| (local != null && local.getType() != null && local
								.getType().getSort() == Type.OBJECT)) {
					bLoadOpc = ALOAD;
					bStoreFunc = "storeObject";
					bStoreDesc = "(ILjava/lang/Object;)V";
					aStoreOpc = ASTORE;
					aLoadFunc = "loadObject";
					aLoadDesc = "(I)Ljava/lang/Object;";
				} else
					continue;
			} else
				continue;
			if (emitLoad) {
				fmv.visitVarInsn(ALOAD, contextLocalIdx);
				fmv.visitIntInsn(SIPUSH, j);
				fmv.visitMethodInsn(INVOKEVIRTUAL, YIELD_CONTEXT_IMPL_CLASS,
						aLoadFunc, aLoadDesc);
				if (bLoadOpc == ALOAD && local != BasicValue.REFERENCE_VALUE) {
					fmv.visitTypeInsn(CHECKCAST, ((BasicValue) local).getType()
							.getInternalName());
				}
				fmv.visitVarInsn(aStoreOpc, j);
			}
			if (emitStore) {
				fmv.visitVarInsn(ALOAD, contextLocalIdx);
				fmv.visitIntInsn(SIPUSH, j);
				fmv.visitVarInsn(bLoadOpc, j);
				fmv.visitMethodInsn(INVOKEVIRTUAL, YIELD_CONTEXT_IMPL_CLASS,
						bStoreFunc, bStoreDesc);
			}

		}

		// Deal with synchronized blocks
		if (frame instanceof DataFlowFrame) {
			DataFlowFrame df = (DataFlowFrame) frame;
			if (df.monitors != null) {
				if (emitStore) {
					for (int k = df.monitors.size(); --k >= 0;) {
						int local = df.monitors.get(k);
						fmv.visitVarInsn(ALOAD, local);
						fmv.visitInsn(MONITOREXIT);
					}
				}
				if (emitLoad) {
					for (int k = 0; k < df.monitors.size(); k++) {
						int local = df.monitors.get(k);
						fmv.visitVarInsn(ALOAD, local);
						fmv.visitInsn(MONITORENTER);
					}
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
		emitLoadStoreLocals(false, true, ctxIdx, fmv, frame);

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