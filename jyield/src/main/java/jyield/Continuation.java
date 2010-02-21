package jyield;

public abstract class Continuation {
	public static boolean suspend() {
		throw new IllegalAccessError("This code must be instrumented to work!");
	}

	public abstract boolean isDone();

	/**
	 * 
	 * @return true if is not done
	 */
	public abstract boolean resume();

}