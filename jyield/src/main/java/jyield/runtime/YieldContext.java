package jyield.runtime;

import java.util.Enumeration;

/**
 * Class returned by some methods of the Yield helper class.
 * 
 * @author Daniel Sperry - 2010
 */
public interface YieldContext<T> extends Enumeration<T>, Iterable<T> {
	/**
	 * Allows the generators to check if the user called
	 * <code>Iterator.remove()</code> since the last <code>Yield.ret()</code>
	 * 
	 * @return true if the consumer from this Iterator requested this element to
	 *         be removed.
	 */
	public abstract boolean getShouldRemove();
}