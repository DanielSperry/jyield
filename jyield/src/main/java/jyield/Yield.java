package jyield;

import java.util.Enumeration;

import jyield.runtime.YieldContext;

public class Yield {

	private static final String errorMessage = "This code needs instrumentation to work! Instrument the class files or use -javaagent jvm argument.";

	public static <T> Enumeration<T> ret(T string) {
		throw new IllegalStateException(errorMessage);
	}

	public static <T> Enumeration<T> join(Enumeration<T> string) {
		throw new IllegalStateException(errorMessage);
	}

	public static <T> YieldContext<T> done() {
		return null;
	}


}
