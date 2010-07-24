package jyield.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import jyield.Continuable;
import jyield.Continuation;

import org.junit.Test;

/**
 * Test the use of the <code>synchronized</code> keyword inside coroutines.
 * 
 * @author Daniel Sperry - 2010
 */
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

	/**
	 * In this test two locks are acquired.
	 */
	@Test(timeout = 1000)
	public void nestedSynchronized() {
		Continuation c = nestedSynchBlocks();
		assertTrue(c.resume());
		assertTrue(c.resume());
		// the locks are automatically released on Continuation.suspend
		// here test check if they where really released.
		synchronized (a) {
			a.notify();
		}
		synchronized (b) {
			b.notify();
		}
		assertFalse(c.resume());
	}

	@Continuable
	public Continuation nestedSynchBlocks() {
		synchronized (a) {
			// notify may be called only if you own the lock
			a.notify();
			Continuation.suspend();
			a.notify();
			synchronized (b) {
				b.notify();
				Continuation.suspend();
				b.notify();
				a.notify();
			}
		}
		return null;
	}
}
