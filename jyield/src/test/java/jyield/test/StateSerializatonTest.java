package jyield.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import jyield.Continuable;
import jyield.Continuation;

import org.junit.Test;

public class StateSerializatonTest {
	public static class ContinuableStuff implements Serializable {
		private static final long serialVersionUID = 1L;

		public int p = -1;

		@Continuable
		public Continuation run() {
			int a = 10;
			p = a++;
			Continuation.suspend();
			p = a++;
			Continuation.suspend();
			p = a++;
			return null;
		}
	}

	@Test
	public void checkNormalBehaviour() throws IOException {
		ContinuableStuff c1 = new ContinuableStuff();
		c1.p = -100;
		Continuation e1 = c1.run();

		assertEquals(-100, c1.p);
		assertTrue(e1.resume());
		assertEquals(10, c1.p);
		assertTrue(e1.resume());
		assertEquals(11, c1.p);
		assertFalse(e1.resume());
		assertEquals(12, c1.p);
	}

	@Test
	public void testStateSerialization() throws IOException,
			ClassNotFoundException {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		ObjectOutputStream o = new ObjectOutputStream(b);

		ContinuableStuff c1 = new ContinuableStuff();
		c1.p = -100;
		Continuation e1 = c1.run();

		assertEquals(-100, c1.p);
		assertTrue(e1.resume());
		assertEquals(10, c1.p);
		assertTrue(e1.resume());
		o.writeObject(c1);
		o.writeObject(e1);

		ObjectInputStream i = new ObjectInputStream(new ByteArrayInputStream(
				b.toByteArray()));
		ContinuableStuff c2 = (ContinuableStuff) i.readObject();
		Continuation e2 = (Continuation) i.readObject();
		assertEquals(11, c2.p);
		assertFalse(e2.resume());
		assertEquals(12, c2.p);
	}
}
