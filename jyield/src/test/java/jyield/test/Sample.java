package jyield.test;

import jyield.Continuable;
import jyield.Yield;

public class Sample {

	@Continuable
	public static Iterable<Integer> power(int number, int exponent) {
		int counter = 0;
		int result = 1;
		while (counter++ < exponent) {
			result = result * number;
			System.out.println("==> " + result);
			Yield.ret(result);
		}
		return Yield.done();
	}

	public static void main(String[] args) {
		// Display powers of 2 up to the exponent 4:
		for (int i : power(2, 4)) {
			System.out.println("    " + i);
		}
	}
}
