package jyield;

import java.util.Enumeration;

public class Yield {

	private static final String errorMessage = "This code needs instrumentation to work! Instrument the class files or use -javaagent jvm argument.";

	@SuppressWarnings("unchecked")
	public static Enumeration done = new Enumeration() {

		@Override
		public boolean hasMoreElements() {
			return false;
		}

		@Override
		public Object nextElement() {
			return null;
		}
	};

	public static <T> Enumeration<T> ret(T string) {
		throw new IllegalStateException(errorMessage);
	}

	public static <T> Enumeration<T> join(Enumeration<T> string) {
		throw new IllegalStateException(errorMessage);
	}

	@SuppressWarnings("unchecked")
	public static <T> Enumeration<T> done() {
		return done;
	}

}
