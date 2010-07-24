package jyield.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Iterator;

import jyield.Continuable;
import jyield.Yield;

import org.junit.Test;

public class IteratorRemoveTest {

	/**
	 * Tests the Iterator.remove signaling is working properly.
	 * <p>
	 * The generators must be able to know when the Iterator.remove was called.
	 * They may use YieldContext.getShouldRemove() to check this.
	 */
	@Test
	public void testRet() {
		Iterator<String> r = produceStrings();
		assertEquals("c1", r.next());
		r.remove();
		assertEquals("x1", r.next());
		assertEquals("c2", r.next());
		assertFalse(r.hasNext());
	}

	@Continuable
	public Iterator<String> produceStrings() {
		if (Yield.ret("c1").getShouldRemove()) {
			Yield.ret("x1");
		}
		if (Yield.ret("c2").getShouldRemove()) {
			Yield.ret("x2");
		}
		return null;
	}
}
