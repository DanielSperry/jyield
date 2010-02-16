package jyield.runtime;

import java.util.Enumeration;

public interface YieldContext<T> extends Enumeration<T>, Iterable<T> {
	public abstract boolean getShouldRemove();
}