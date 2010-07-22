package jyield;

import jyield.runtime.ContinuationContext;

/**
 * Continuation represents the state of a @Continuable method. 
 * <p>
 * Methods marked with this @Continuable may return Continuation.
 * <p>
 * The static methods Continuation.suspend(), Continuation.join() should be used only inside @Continuable methods. 
 */
public abstract class Continuation {
	private static final String errorMessage = "This code must be instrumented to work!";

	/**
	 * Suspends the execution of the current @Continuable method.
	 * <p>
	 * The static methods Continuation.suspend(), Continuation.join() should be used only inside @Continuable methods. 
	 * @return true if this continuation is not done
	 */
	public static ContinuationContext suspend() {
		throw new IllegalAccessError(errorMessage);
	}

	/**
	 * @return true if this continuation has finished.
	 */
	public abstract boolean isDone();

	/**
	 * Suspends this continuation until the end of execution other continuation.
	 * <p>
	 * Until the other continuation is done, this continuation's methods isDone() and resume() will call the isDone and resume from the other continuation.
	 * <p>
	 * The static methods Continuation.suspend(), Continuation.join() should be used only inside @Continuable methods. 
	 * @param other the other continuation that will be executed until the end before this one continues again.  
	 */
	public static ContinuationContext join(Continuation other) {
		throw new IllegalAccessError(errorMessage);
	}

	/**
	 * Resumes the execution of this continuation.
	 * The @Continuable method will execute until it's next call to Continuation.suspend() 
	 * @return true if this continuation is not done
	 */
	public abstract boolean resume();

}