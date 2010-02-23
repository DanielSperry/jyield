import jyield.Continuable;
import jyield.Continuation;

public class ParallelContinuations {

	@Continuable
	public Continuation firstt() {
		System.out.print("started_01!");
		for (int i = 0; i < 10; i++) {
			Continuation.suspend();
			System.out.print((char) ('a' + i));
		}
		return null;
	}

	@Continuable
	public Continuation second() {
		System.out.print("started_02!");
		for (int i = 0; i < 10; i++) {
			Continuation.suspend();
			System.out.print(i);
		}
		return null;
	}

	public static void main(String[] args) {
		ParallelContinuations p = new ParallelContinuations();
		Continuation c1 = p.firstt();
		Continuation c2 = p.second();
		while (!c1.isDone() || !c2.isDone()) {
			c1.resume();
			c2.resume();
			System.out.print(" ");
		}
	}
}

// Output: started_01!started_02! a0 b1 c2 d3 e4 f5 g6 h7 i8 j9 