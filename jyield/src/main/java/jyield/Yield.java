package jyield;

import java.util.Enumeration;
import java.util.Iterator;

import jyield.runtime.YieldContext;


/**
 * Helper class to emulate the working of the yield keyword from c#. 
 * <p>
 * The static methods Continuation.ret(), Continuation.join() and Yield.done() should be used only inside methods marked with @Continuable. 
 */
public class Yield {

	private static final String errorMessage = "This code needs instrumentation to work! Instrument the class files or use -javaagent jvm argument.";

	/**
	 * Equivalent to c# <code>yield return value</code> construct.
	 * Works only if called from methods marked with @Continuable annotation.  
	 * @return
	 */
	public static <T> YieldContext<T> ret(T value) {
		throw new IllegalStateException(errorMessage);
	}

	/**
	 * Helper method that will join the result of the current generator with the items from a Enumeration. 
	 * To allow for compile time checking of the generic parameter, use the same parameter from the enclosing @Continuable method.
	 * <p>  
	 * Works only if called from methods marked with @Continuable annotation.  
	 */
	public static <T> YieldContext<T> join(Enumeration<T> other) {
		throw new IllegalStateException(errorMessage);
	}

	/**
	 * Helper method that will join the result of the current generator with the items from a Enumeration. 
	 * To allow for compile time checking of the generic parameter, use the same parameter from the enclosing @Continuable method.
	 * <p>  
	 * Works only if called from methods marked with @Continuable annotation.  
	 */
	public static <T> YieldContext<T> join(Iterable<T> other) {
		throw new IllegalStateException(errorMessage);
	}

	/**
	 * Helper method that will join the result of the current generator with the items from a Enumeration. 
	 * To allow for compile time checking of the generic parameter, use the same parameter from the enclosing @Continuable method.
	 * <p>  
	 * Works only if called from methods marked with @Continuable annotation.  
	 */
	public static <T> YieldContext<T> join(Iterator<T> other) {
		throw new IllegalStateException(errorMessage);
	}

	/**
	 * Helper method to avoid <code>return null</code> from the generator.
	 * @deprecated
	 */
	@Deprecated
	public static <T> YieldContext<T> done() {
		return null;
	}

}
