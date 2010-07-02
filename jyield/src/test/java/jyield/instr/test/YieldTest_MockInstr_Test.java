package jyield.instr.test;

import static org.junit.Assert.assertEquals;

import java.util.Enumeration;

import jyield.Yield;
import jyield.runtime.YieldContextImpl;

import org.junit.Test;

public class YieldTest_MockInstr_Test {
	@Test
	public void testApp() {
		Enumeration<String> e = produceString();
		assertEquals("a", e.nextElement());
		assertEquals("b", e.nextElement());
		assertEquals("c", e.nextElement());
	}

	public Enumeration<String> produceString() {
		return new YieldTest_MockInstr_produceString(0, this);
	}

	@SuppressWarnings("rawtypes")
	private Enumeration _produceString_step(YieldContextImpl ye) {
		switch (ye.getNextLine()) {
		case 0:
			return ye.ret("a", 1);
		case 1:
			return ye.ret("b", 2);
		case 2:
			return ye.ret("c", 3);
		case 3:
			return Yield.done();
		}
		return Yield.done();
	}

	private static class YieldTest_MockInstr_produceString extends
			YieldContextImpl<String> {

		private static final long serialVersionUID = 1L;

		public YieldTest_MockInstr_produceString(int localsCount, Object target) {
			super(localsCount, target);
		}

		@SuppressWarnings({ "rawtypes" })
		@Override
		protected Enumeration doStep() {
			return ((YieldTest_MockInstr_Test) target)
					._produceString_step(this);
		}

	}

}
