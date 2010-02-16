package jyield.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Enumeration;

import jyield.Continuable;
import jyield.Yield;

import org.junit.Test;

public class LoopTest {

	@Test
	public void testFor() {
		Enumeration<String> e = produceStringsWithFor();

		for (int i = 0; i < 4; i++) {
			assertEquals("c" + i, e.nextElement());
		}
	}

	@Test
	public void testWhile() {
		Enumeration<String> e = produceStringsWithWhile();

		assertEquals("c1", e.nextElement());
		assertEquals("c2", e.nextElement());
		assertEquals("c3", e.nextElement());
		assertEquals("c4", e.nextElement());
		assertFalse(e.hasMoreElements());

	}

	@Continuable
	public Enumeration<String> produceStringsWithFor() {
		for (int i = 0; i < 4; i++) {
			Yield.ret("c" + i);
		}
		return Yield.done();
	}

	@Continuable
	public Enumeration<String> produceStringsWithWhile() {
		int i = 0;
		while (i++ < 4) {
			Yield.ret("c" + i);
		}
		return Yield.done();
	}
}
