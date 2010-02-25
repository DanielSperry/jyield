package jyield.test;

import static org.junit.Assert.*;
import jyield.Continuable;
import jyield.Continuation;

import org.junit.Test;

public class ExceptionTest {

	private int where;

	@Test(timeout = 1000)
	public void exceptionsTest() {
		int x = 0;
		Continuation c = exceptional();
		assertEquals(x = 0, where);
		c.resume();
		assertEquals(x = 1, where);
		c.resume();
		assertEquals(x = x | 2 | 4, where);
		c.resume();
		assertEquals(x = x | 8 | 16, where);
		c.resume();
		assertEquals(x = x | 32 | 64, where);
		c.resume();
		assertEquals(x = x | 128 | 256, where);
		c.resume();
		assertEquals(x = x | 512, where);
		assertTrue(c.isDone());
	}

	@Continuable
	public Continuation exceptional() {
		where |= 1;
		Continuation.suspend();
		where |= 2;
		try {
			where |= 4;
			Continuation.suspend();
			where |= 8;
			throw new Exception();
		} catch (Exception ex) {
			where |= 16;
			Continuation.suspend();
			where |= 32;
		} finally {
			where |= 64;
			Continuation.suspend();
			where |= 128;
		}
		where |= 256;
		Continuation.suspend();
		where |= 512;
		return null;
	}

	@Test(timeout = 1000)
	public void exceptionsTest0() {
		Continuation c = exceptional0();
		c.resume();
	}

	@Continuable
	public Continuation exceptional0() {
		try {
			throw new Exception();
		} catch (Exception ex) {
		} finally {
		}
		return null;
	}

	@Test(expected = RuntimeException.class)
	public void exceptionsTest2() {
		Continuation c = exceptional2();
		try {
			assertTrue(c.resume());
		} catch (RuntimeException ex) {
			assertEquals("x", ex.getMessage());
		}
		assertFalse(c.resume());
	}

	@Continuable
	public Continuation exceptional2() {
		try {
			throw new RuntimeException("x");
		} finally {
			Continuation.suspend();
		}
		// return null;
	}

}
