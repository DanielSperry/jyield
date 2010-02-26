package jyield.instr;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.Interpreter;
import org.objectweb.asm.tree.analysis.Value;

public class DataFlowFrame extends Frame {
	ArrayList<Integer> monitors;

	public DataFlowFrame(Frame src) {
		super(src);
		if (src instanceof DataFlowFrame) {
			if (((DataFlowFrame) src).monitors != null)
				monitors = new ArrayList<Integer>(
						((DataFlowFrame) src).monitors);
		}
	}

	public DataFlowFrame(int locals, int stack) {
		super(locals, stack);
	}

	@Override
	public Frame init(Frame src) {
		super.init(src);
		if (src instanceof DataFlowFrame) {
			if (((DataFlowFrame) src).monitors != null)
				monitors = new ArrayList<Integer>(
						((DataFlowFrame) src).monitors);
		}
		return this;
	}

	@Override
	public void execute(AbstractInsnNode insn, Interpreter interpreter)
			throws AnalyzerException {
		switch (insn.getOpcode()) {
		case Opcodes.MONITORENTER:
		case Opcodes.MONITOREXIT:
			if (monitors == null) {
				monitors = new ArrayList<Integer>();
			}
			Value v = pop();
			int local;
			for (local = getLocals(); --local >= 0;) {
				if (v == super.getLocal(local))
					break;
			}
			switch (insn.getOpcode()) {
			case Opcodes.MONITORENTER:
				if (local >= 0)
					monitors.add(local);
				break;
			case Opcodes.MONITOREXIT:
				int lastIndexOf = monitors.lastIndexOf(local);
				if (lastIndexOf >= 0)
					monitors.remove(lastIndexOf);
				break;
			}
			interpreter.unaryOperation(insn, v);
			return;
		}
		super.execute(insn, interpreter);
	}

	public List<Integer> getMonitors() {
		return monitors;
	}

}
