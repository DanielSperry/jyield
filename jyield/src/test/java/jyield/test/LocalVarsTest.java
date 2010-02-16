package jyield.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Enumeration;

import jyield.Continuable;
import jyield.Yield;

import org.junit.Test;

public class LocalVarsTest {
	int p = 0;

	@Test
	public void testApp() {
		p = 0;
		Enumeration<String> e = produceString();

		assertEquals("a", e.nextElement());
		assertEquals(1, p);
		assertEquals("b", e.nextElement());
		assertEquals(2, p);
		assertEquals("c", e.nextElement());
		assertEquals(3, p);
		assertFalse(e.hasMoreElements());
		assertEquals(4, p);
	}

	@Continuable
	public Enumeration<String> produceString() {
		int i;
		float f;
		String str;
		long j;
		short s;
		double d;
		char c;
		byte b;
		boolean bo;
		p = 1;
		f = 1.1f;
		str = "boo";
		j = Long.MAX_VALUE;
		s = 20215;
		d = Double.MAX_VALUE;
		c = 'z';
		b = 108;
		bo = true;
		Yield.ret("a");
		assertEquals(1.1f, f, 0f);
		assertEquals("boo", str);
		assertEquals(Long.MAX_VALUE, j);
		assertEquals(20215, s);
		assertEquals(Double.MAX_VALUE, d, 0d);
		assertEquals('z', c);
		assertEquals(108, b);
		assertEquals(true, bo);
		i = p = 2;
		f = 2.1f;
		str = "boo2";
		j = 1;
		bo = false;
		Yield.ret("b");
		assertEquals(2, i);
		assertEquals(2.1f, f, 0f);
		assertEquals("boo2", str);
		assertEquals(1, j);
		assertEquals(false, bo);
		i = p = 3;
		f = 3.1f;
		j = Long.MIN_VALUE;
		str = "boo3";
		Yield.ret("c");
		assertEquals(3, i);
		assertEquals(3.1f, f, 0f);
		assertEquals("boo3", str);
		assertEquals(Long.MIN_VALUE, j);
		p = 4;
		return Yield.done();
	}
}
