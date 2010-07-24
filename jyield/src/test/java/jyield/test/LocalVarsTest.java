package jyield.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Enumeration;
import java.util.Random;

import jyield.Continuable;
import jyield.Yield;

import org.junit.Test;

public class LocalVarsTest {
	int p = 0;

	/**
	 * Checks it the local variables are preserved as the generator yields
	 * values.
	 */
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
		// tests all kinds of variables.
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
		d = Double.MAX_VALUE; // using max value to check if the double->longbit
								// is preserving the double.
		c = 'z';
		b = 108;
		bo = true;
		// after the yield those variables must be restored to their original
		// values.
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
		return null;
	}

	// check values for the random test
	int gi;
	float gf;
	String gstr;
	long gj;
	short gs;
	double gd;
	char gc;
	byte gb;
	boolean gbo;
	Random r = new Random();

	/**
	 * Test several iterations with random local variable data.
	 */
	@Test
	public void testHarder() {
		int iterations = 1000;
		Enumeration<String> e = produceString2(iterations);
		for (int i = 0; i < iterations; i++) {
			assertTrue(e.hasMoreElements());
			assertNotNull(e.nextElement());
		}
		assertFalse(e.hasMoreElements());
	}

	@Continuable
	public Enumeration<String> produceString2(int iterations) {
		// tests all kinds of variables.
		int i;
		float f;
		String str;
		long j;
		short s;
		double d;
		char c;
		byte b;
		boolean bo;

		for (int x = 0; x < iterations; x++) {
			// sets the local variables and save a copy in the instance fields.
			gi = i = r.nextInt();
			gf = f = r.nextFloat() * r.nextInt();
			gstr = str = "s" + r.nextInt();
			gj = j = r.nextLong();
			gs = s = (short) r.nextInt();
			gd = d = r.nextDouble() * r.nextLong();
			gc = c = (char) r.nextInt();
			gb = b = (byte) r.nextInt();
			gbo = bo = r.nextBoolean();
			Yield.ret(str);
			// checks if the local variables where restored and are equal to the
			// save instance fields values
			assertEquals(gi, i, 0f);
			assertEquals(gf, f, 0f);
			assertEquals(gstr, str);
			assertEquals(gj, j);
			assertEquals(gs, s);
			assertEquals(gd, d, 0d);
			assertEquals(gc, c);
			assertEquals(gb, b);
			assertEquals(gbo, bo);
		}
		return null;
	}
}
