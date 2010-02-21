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
		assertFalse(e.hasNext());

	}

	@Continuable
	public Iterable<String> produceStrings() {
		Yield.ret("c1");
		Yield.join(produceStrings2());
		Yield.ret("c2");
		Yield.join(Arrays.asList("x", "y"));
		Yield.ret("c3");
		Yield.join(Collections.emptyList());
		Yield.ret("c4");
		return null;
	}

	@Continuable
	public Iterable<String> produceStrings2() {
		Yield.ret("a");
		Yield.ret("b");
		return null;
	}
}
