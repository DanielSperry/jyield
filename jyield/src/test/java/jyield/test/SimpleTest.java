package jyield.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Enumeration;

import jyield.Continuable;
import jyield.Yield;

import org.junit.Test;

public class SimpleTest {
	int p = 0;

	@Test
	public void testApp() {
		p = 0;
		Enumeration<String> e = produceString();

		assertEquals("a", e.nextElement());
		assertEquals(1, p);
		assertEquals("b", e.nextElement());
		assertEquals(2, p);
		assertFalse(e.hasMoreElements());
		assertEquals(3, p);
		assertFalse(e.hasMoreElements());
		assertEquals(3, p);
	}

	@Continuable
	public Enumeration<String> produceString() {
		p = 1;
		Yield.ret("a");
		p = 2;
		Yield.ret("b");
		p++;
		return Yield.done();
	}
}
