import jyield.Continuable;
import jyield.Continuation;

public class ContinuationExample {

	@Continuable
	public Continuation runIt() {
		System.out.print("started!");
		for (int i = 0; i < 10; i++)
			Continuation.join(echo(i));
		return null;
	}

	@Continuable
	public Continuation echo(int x) {
		System.out.print(x);
		Continuation.suspend();
		return null;
	}

	public static void main(String[] args) {
		Continuation c = new ContinuationExample().runIt();
		while (!c.isDone()) {
			System.out.print("-");
			c.resume();
		}
	}
}

// Output: -started!0-1-2-3-4-5-6-7-8-9-