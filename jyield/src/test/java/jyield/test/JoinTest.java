package jyield.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import jyield.Continuable;
import jyield.Yield;

import org.junit.Test;

public class JoinTest {

	/**
	 * Tests a generator joining with generator, enumerables and iterables. 
	 */
	@Test
	public void testJoin() {
		Iterator<String> e = produceStrings().iterator();

		assertEquals("c1", e.next());
		assertEquals("a", e.next());
		assertEquals("b", e.next());
		assertEquals("c2", e.next());
		assertEquals("x", e.next());
		assertEquals("y", e.next());
		assertEquals("c3", e.next());
		assertEquals("c4", e.next());
		assertEquals("w", e.next());
		assertEquals("z", e.next());
		assertEquals("c5", e.next());
		assertEquals("w2", e.next());
		assertEquals("z2", e.next());
		assertEquals("c6", e.next());
		assertFalse(e.hasNext());

	}

	@Continuable
	public Iterable<String> produceStrings() {
		Yield.ret("c1");
		// joining with another generator
		Yield.join(produceStrings2());
		Yield.ret("c2");
		// joining with a list (Iterable)
		Yield.join(Arrays.asList("x", "y"));
		Yield.ret("c3");
		// joining with an empty Iterable
		Yield.join(Collections.emptyList());
		Yield.ret("c4");
		// joining with a Enumeration
		Yield.join(Collections.enumeration(Arrays.asList("w", "z")));
		Yield.ret("c5");
		// joining with a Iterator
		Yield.join(Arrays.asList("w2", "z2").iterator());
		Yield.ret("c6");
		return null;
	}

	@Continuable
	public Iterable<String> produceStrings2() {
		Yield.ret("a");
		Yield.ret("b");
		return null;
	}
}
