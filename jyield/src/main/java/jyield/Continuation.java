package jyield;

public interface Continuation {
	public boolean step();

	public boolean isDone();
}