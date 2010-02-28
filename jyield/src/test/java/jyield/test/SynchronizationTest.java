package jyield.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import jyield.Continuable;
import jyield.Continuation;

import org.junit.Test;

public class SynchronizationTest {

	Object a = new Object();
	Object b = new Object();

	@Test
	public void simpleSynchronized() {
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

	@Test
	public void nestedSynchronized() {
		Continuation c = nestedSynchBlocks();
		assertTrue(c.resume());
		assertFalse(c.resume());
	}

	@Continuable
	public Continuation nestedSynchBlocks() {
		synchronized (a) {
			// Continuation.suspend();
			synchronized (b) {
				Continuation.suspend();
			}
		}
		return null;
	}
}
