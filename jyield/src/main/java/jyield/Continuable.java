package jyield;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation marks a method for runtime instrumentation.
 * <p>
 * Methods marked with this annotation should return Iterable<T>, Enumerable<T>, Iterator<T> or Continuation.
 * <pre>
 *import jyield.*;
 *import java.util.*;
 *
 *public class Sample2 {
 *        
 *	&#64;Continuable
 *	public Iterable&lt;Integer&gt; power(int number, int exponent) {
 *		int counter = 0;
 *		int result = 1;
 *		while (counter++ < exponent) {
 *			result = result * number;
 *			Yield.ret(result);
 *		}
 *		return null;
 *	}
 *
 *	&#64;Continuable
 *	public Continuation process() {
 *		System.out.println("c1");
 *		Continuation.suspend();
 *		System.out.println("c2");
 *		return null;
 *	}
 *
 *	public void main(String[] args) {
 *		Sample2 s = new Sample2();
 *		// Sample generator: Display powers of 2 up to the exponent 8:
 *		for (int i : s.power(2, 8)) {
 *			System.out.print(" "+ i + " ");
 *		}
 *		// Sample continuation
 *		Continuation c = process();
 *		c.resume();
 *		System.out.println("m1");
 *		c.resume();
 *	}
 *}
 *</pre> 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Continuable {

}
