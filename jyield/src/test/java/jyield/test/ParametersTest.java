package jyield.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Enumeration;

import jyield.Continuable;
import jyield.Yield;

import org.junit.Test;

public class ParametersTest {

	@Test
	public void testIntParam() {
		Enumeration<String> e = produceStringsWithFor(1, 3);

		assertEquals("c1", e.nextElement());
		assertEquals("c2", e.nextElement());
		assertEquals("c3", e.nextElement());
		assertFalse(e.hasMoreElements());

	}

	@Continuable
	public Enumeration<String> produceStringsWithFor(int start, int end) {
		for (int i = start; i <= end; i++) {
			Yield.ret("c" + i);
		}
		return Yield.done();
	}

}
