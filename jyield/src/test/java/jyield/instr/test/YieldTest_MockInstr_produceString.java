package jyield.instr.test;

import java.util.Enumeration;

import jyield.runtime.YieldContextImpl;

public class YieldTest_MockInstr_produceString extends YieldContextImpl<String> {

	public YieldTest_MockInstr_produceString(int localsCount, Object target) {
		super(localsCount, target);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Enumeration doStep() {
		return ((YieldTest_MockInstr_Test) target)._produceString_step(this);
	}

}
