import jyield.Continuable;
import jyield.Continuation;
import jyield.Yield;

public class Sample2 {
        
	@Continuable
	public Iterable<Integer> power(int number, int exponent) {
		int counter = 0;
		int result = 1;
		while (counter++ < exponent) {
			result = result * number;
			Yield.ret(result);
		}
		return null;
	}

	@Continuable
	public Continuation process() {
		System.out.println("c1");
		Continuation.suspend();
		System.out.println("c2");
		return null;
	}

	public void main(String[] args) {
		Sample2 s = new Sample2();
		// Sample generator: Display powers of 2 up to the exponent 8:
		for (int i : s.power(2, 8)) {
			System.out.print(" "+ i + " ");
		}
		// Sample continuation
		Continuation c = process();
		c.resume();
		System.out.println("m1");
		c.resume();
	}
}

