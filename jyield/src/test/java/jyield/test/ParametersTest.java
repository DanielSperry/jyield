package jyield.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Enumeration;

import jyield.Continuable;
import jyield.Yield;

import org.junit.Test;

public class ParametersTest {
	int proof;

	@Test
	public void testIntParam() {
		proof = 1;
		Enumeration<String> e = produceStringsWithFor(1, 3);
		assertEquals(1, proof);
		assertEquals("c1", e.nextElement());
		assertEquals("c2", e.nextElement());
		assertEquals("c3", e.nextElement());
		assertFalse(e.hasMoreElements());
		assertEquals(3, proof);

	}

	@Continuable
	public Enumeration<String> produceStringsWithFor(int start, int end) {
		for (int i = start; i <= end; i++) {
			Yield.ret("c" + i);
			proof = i;
		}
		return Yield.done();
	}
}
