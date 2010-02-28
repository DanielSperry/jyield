package jyield.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import jyield.Continuable;
import jyield.Continuation;

import org.junit.Test;

public class ContinuationReturnsTest {

	@Test
	public void testRet() {
		Continuation c = doStuff();
		assertTrue(c.resume());
		assertFalse(c.resume());
	}

	@Test
	public void testRet2() {
		Continuation c = doOuterStuff();
		assertTrue(c.resume());
		assertTrue(c.resume());
		assertFalse(c.resume());
	}

	@Continuable
	public Continuation doStuff() {
		assertNotNull(Continuation.suspend());
		return null;
	}

	@Continuable
	public Continuation doOuterStuff() {
		assertNotNull(Continuation.suspend());
		assertNotNull(Continuation.join(doStuff()));
		return null;
	}
}
