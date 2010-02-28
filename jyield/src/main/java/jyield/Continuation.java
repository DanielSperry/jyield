package jyield;

import jyield.runtime.ContinuationContext;

public abstract class Continuation {
	private static final String errorMessage = "This code must be instrumented to work!";

	public static ContinuationContext suspend() {
		throw new IllegalAccessError(errorMessage);
	}

	public abstract boolean isDone();

	public static ContinuationContext join(Continuation other) {
		throw new IllegalAccessError(errorMessage);
	}

	/**
	 * 
	 * @return true if is not done
	 */
	public abstract boolean resume();

}