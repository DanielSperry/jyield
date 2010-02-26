package jyield.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import jyield.Continuable;
import jyield.Continuation;

import org.junit.Test;

public class SynchronizationTest {

	Object a = new Object();

	@Test
	public void testApp() {
		Continuation c = synchBlock();
		assertTrue(c.resume());
		assertFalse(c.resume());
	}

	@Continuable
	public Continuation synchBlock() {
		synchronized (a) {
			Continuation.suspend();
		}
		return null;
	}
}
