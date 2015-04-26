## ContinuationExample.java ##
Similar to example from [javaflow tutorial](http://commons.apache.org/sandbox/javaflow/tutorial.html)
```
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
```

## Sample.java ##
Similar to the [c# yield documentation example](http://msdn.microsoft.com/en-us/library/9k7k7cf0.aspx).
```
import jyield.Continuable;
import jyield.Yield;

public class Sample {
        
        @Continuable
        public static Iterable<Integer> power(int number, int exponent) {
                int counter = 0;
                int result = 1;
                while (counter++ < exponent) {
                        result = result * number;
                        System.out.print("[" + result+ "]");
                        Yield.ret(result);
                }
                return Yield.done();
        }

        public static void main(String[] args) {
                // Display powers of 2 up to the exponent 8:
                for (int i : power(2, 8)) {
                        System.out.print(" "+ i + " ");
                }
        }
}

// Output: [2] 2 [4] 4 [8] 8 [16] 16 [32] 32 [64] 64 [128] 128 [256] 256
```

## ParallelContinuations.java ##
Two coroutines running "in parallel" using continuations.
```
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
```


## RecursionSample.java ##
Some generator recursion... Thanks, Remko.
```
iimport jyield.Continuable;
import jyield.Yield;

public class RecursionSample {

	public static class Node<T> {
		public Node<T> left;
		public Node<T> right;
		T value;

		public Node(T value) {
			this.value = value;
		}

		public void add(T value) {
			int i = ((Comparable<T>)value).compareTo(this.value);
			if (i < 0) {
				if (left != null)
					left.add(value);
				else
					left = new Node<T>(value);
			} else if (i > 0) {
				if (right != null)
					right.add(value);
				else
					right = new Node<T>(value);
			}
		}
	}

	@Continuable
	<T> Iterable<T> walk(Node<T> node) {
		if(node == null)
			return null;
		if (node != null) {
			Yield.join(walk(node.left));
			Yield.ret(node.value);
			Yield.join(walk(node.right));
		}
		return null;
	}

	public static void main(String[] args) {
		Node<Integer> root = new Node<Integer>(4);
		root.add(2);
		root.add(6);
		root.add(1);
		root.add(3);
		root.add(7);
		root.add(5);
		RecursionSample s = new RecursionSample();
		// Sample generator: Display powers of 2 up to the exponent 8:
		for (int i : s.walk(root)) {
			System.out.print(" "+ i + " ");
		}
	}
}

// Output:  1  2  3  4  5  6  7 
```