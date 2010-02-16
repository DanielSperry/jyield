package jyield.instr.test;

import java.util.Enumeration;

import jyield.runtime.YieldContext;


public class YieldTest_MockInstr_produceString extends YieldContext<String> {

	public YieldTest_MockInstr_produceString(int localsCount, Object target) {
		super(localsCount, target);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Enumeration doStep() {
		return ((YieldTest_MockInstr_Test) target)._produceString_step(this);
	}

}
