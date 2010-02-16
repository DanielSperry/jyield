package jyield.test;

import static org.junit.Assert.*;

import org.junit.Test;

import jyield.Continuable;
import jyield.Yield;

public class CSampleTest {
	@Continuable
	public static Iterable<Integer> power(int number, int exponent) {
		int counter = 0;
		int result = 1;
		while (counter++ < exponent) {
			result = result * number;
			Yield.ret(result);
		}
		return Yield.done();
	}

	@Test
	public void doTest() {
		// Display powers of 2 up to the exponent 8:

		int val = 2;
		for (int i : power(2, 8)) {
			System.out.printf("%d \r\n", i);
			assertEquals(val, i);
			val *= 2;
		}
	}
}
