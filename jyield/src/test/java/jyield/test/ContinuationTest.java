package jyield.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;

import jyield.Continuable;
import jyield.Continuation;

import org.junit.Test;

public class ContinuationTest {
	int proof;
	private StringWriter sw = new StringWriter();

//	@Test(timeout = 1000)
//	public void testContinuation() {
//		Continuation c = runStuff();
//		println("returned a continuation");
//		while (!c.isDone()) {
//			c.resume();
//			println("i am outside");
//		}
//		assertEquals(2134234, sw.hashCode());
//	}

	public void println(String s) {
		System.out.println(s);
		sw.write(s + "\r\n");
	}

	@Continuable
	public Continuation runStuff() {
		System.out.println("started!");
		for (int i = 0; i < 10; i++)
			Continuation.join(echo(i));
		return null;
	}

	@Continuable
	public Continuation echo(int x) {
		println("" + x);
		Continuation.suspend();
		return null;
	}

	@Continuable
	public Continuation simple() {
		println("c1");
		Continuation.suspend();
		println("c2");
		return null;
	}

	@Test
	public void testSimple() {
		Continuation c = simple();
		assertEquals("", sw.toString());
		assertFalse(c.isDone());
		assertTrue(c.resume());
		assertEquals("c1\r\n", sw.toString());
		assertFalse(c.isDone());
		assertFalse(c.resume());
		assertEquals("c1\r\nc2\r\n", sw.toString());
		assertTrue(c.isDone());

	}

}
