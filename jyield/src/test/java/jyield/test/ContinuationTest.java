//package jyield.test;
//
//import static org.junit.Assert.assertEquals;
//
//import java.io.StringWriter;
//
//import jyield.Continuable;
//import jyield.Continuation;
//
//import org.junit.Test;
//
//public class ContinuationTest {
//	int proof;
//	private StringWriter sw = new StringWriter();
//
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
//
//	public void println(String s) {
//		System.out.println(s);
//		sw.write(s + "\r\n");
//	}
//
//	@Continuable(autoJoin = true)
//	public Continuation runStuff() {
//		System.out.println("started!");
//		for (int i = 0; i < 10; i++)
//			echo(i);
//		return null;
//	}
//
//	@Continuable
//	private Continuation echo(int x) {
//		println("" + x);
//		Continuation.suspend();
//		return null;
//	}
//
//}
