package jyield;

public abstract class Continuation {
	private static final String errorMessage = "This code must be instrumented to work!";

	public static boolean suspend() {
		throw new IllegalAccessError(errorMessage);
	}

	public abstract boolean isDone();

	public static boolean join(Continuation other) {
		throw new IllegalAccessError(errorMessage);
	}

	/**
	 * 
	 * @return true if is not done
	 */
	public abstract boolean resume();

}