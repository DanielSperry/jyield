package jyield.runtime;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Iterator;

import jyield.Continuation;

/**
 * Subclasses of YieldContextImpl are returned by the transformed @Continuable
 * methods.
 * <p>
 * The user should not use this class directly but rather access the methods
 * from Continuation or YieldContext.
 */
public class YieldContextImpl<T> extends Continuation implements
		YieldContext<T>, Iterator<T>, ContinuationContext, Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * If the generator/coroutine is not static, target is the object instance
	 * that receives the calls.
	 */
	protected Object target;

	/**
	 * Holds the object variables from the generator/continuation stack. The
	 * variables are only saved in this array when Yield.ret or
	 * Continuation.suspend are called.
	 */
	private Object[] objectVariables;
	/**
	 * Holds the primitive variables from the generator/continuation stack. The
	 * variables are only saved in this array when Yield.ret or
	 * Continuation.suspend are called.
	 * <p>
	 * No other arrays are created to avoiding allocation too many primitive
	 * type arrays. Floats and Doubles are converted for storage using
	 * floatToRawIntBits and Double.doubleToRawLongBits.
	 */
	private long[] primitiveVariables;

	/**
	 * For generators, nextValue holds the last yielded value, if any.
	 */
	private Object nextValue;

	/**
	 * Has a next value to yield? If hasNext==false and mustStep==false then
	 * generator is done.
	 */
	private boolean hasNext;

	/**
	 * Indicates if the generator must step before the Iterator returns a value
	 * or before evaluating hasNext.
	 */
	private boolean mustStep = true;

	/**
	 * True if the generator/continuation reached its end.
	 */
	private boolean done;

	/**
	 * Holds the pseudo instruction pointer that allows jyield to find out from
	 * where it should resume the generator/continuation.
	 */
	private int nextLine;
	private Enumeration<T> joined;

	/**
	 * True when the user called Iterator.remove() since the last yield.
	 */
	private boolean shouldRemove;

	/**
	 * Used when Continuation.join or Yield.join was called.
	 */
	private Iterator<T> joinedIterator;

	public YieldContextImpl(int localsCount, Object target) {
		super();
		this.target = target;
		if (localsCount > 0) {
			objectVariables = new Object[localsCount];
			primitiveVariables = new long[localsCount];
		}
	}

	public YieldContextImpl<T> ret(Object obj, int nextLine) {
		this.nextLine = nextLine;
		nextValue = obj;
		hasNext = true;
		return this;
	}

	public YieldContextImpl<T> suspend(int nextLine) {
		this.nextLine = nextLine;
		nextValue = null;
		hasNext = true;
		return this;
	}

	public YieldContextImpl<T> join(Enumeration<T> joined, int nextLine) {
		this.joined = joined;
		this.nextLine = nextLine;
		return this;
	}

	public YieldContextImpl<T> join(Iterable<T> joined, int nextLine) {
		this.nextLine = nextLine;
		this.joinedIterator = joined.iterator();
		return this;
	}

	public YieldContextImpl<T> join(Iterator<T> joined, int nextLine) {
		this.joinedIterator = joined;
		this.nextLine = nextLine;
		return this;
	}

	@SuppressWarnings("unchecked")
	public YieldContextImpl<T> join(Continuation joined, int nextLine) {
		this.joinedIterator = (Iterator<T>) joined;
		this.nextLine = nextLine;
		return this;
	}

	public int getNextLine() {
		return nextLine;
	}

	public YieldContextImpl<T> done() {
		hasNext = false;
		mustStep = false;
		done = true;
		return this;
	}

	public boolean hasMoreElements() {
		if (joined != null) {
			if (joined.hasMoreElements())
				return true;
			joined = null;
			mustStep = true;
		}
		if (joinedIterator != null) {
			if (joinedIterator.hasNext())
				return true;
			joinedIterator = null;
			mustStep = true;
		}
		if (mustStep) {
			while (true) {
				privateStep();
				mustStep = false;
				if (joined != null) {
					if (joined.hasMoreElements())
						return true;
					joined = null;
					continue;
				}
				if (joinedIterator != null) {
					if (joinedIterator.hasNext())
						return true;
					joinedIterator = null;
					continue;
				}
				break;
			}
		}

		return hasNext;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T nextElement() {
		if (joined != null) {
			if (joined.hasMoreElements())
				return (T) joined.nextElement();
			joined = null;
		}
		if (joinedIterator != null) {
			if (joinedIterator.hasNext())
				return (T) joinedIterator.next();
			joinedIterator = null;
		}
		if (mustStep) {
			while (true) {
				privateStep();
				if (joined != null) {
					if (joined.hasMoreElements()) {
						mustStep = true;
						return (T) joined.nextElement();
					}
					joined = null;
					continue;
				}
				if (joinedIterator != null) {
					if (joinedIterator.hasNext()) {
						mustStep = true;
						return (T) joinedIterator.next();
					}
					joinedIterator = null;
					continue;
				}
				break;
			}
		}
		if (!done)
			mustStep = true;

		return (T) nextValue;
	}

	public boolean step() {
		if (hasMoreElements()) {
			nextElement();
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean isDone() {
		return done;
	}

	private void privateStep() {
		hasNext = false;
		doStep();
		shouldRemove = false;
		if (!hasNext && joinedIterator == null && joined == null) {
			mustStep = false;
			done = true;
		}
	}

	@SuppressWarnings("rawtypes")
	protected Enumeration doStep() {
		// must be overidden!
		return this;
	}

	public void storeInt(int index, int v) {
		primitiveVariables[index] = v;
	}

	public void storeLong(int index, long v) {
		primitiveVariables[index] = v;
	}

	public void storeFloat(int index, float v) {
		primitiveVariables[index] = Float.floatToRawIntBits(v);
	}

	public void storeDouble(int index, double v) {
		primitiveVariables[index] = Double.doubleToRawLongBits(v);
	}

	public void storeObject(int index, Object v) {
		objectVariables[index] = v;
	}

	public int loadInt(int index) {
		return (int) primitiveVariables[index];
	}

	public long loadLong(int index) {
		return primitiveVariables[index];
	}

	public float loadFloat(int index) {
		return Float.intBitsToFloat((int) primitiveVariables[index]);
	}

	public double loadDouble(int index) {
		return Double.longBitsToDouble(primitiveVariables[index]);
	}

	public Object loadObject(int index) {
		return objectVariables[index];
	}

	@Override
	public Iterator<T> iterator() {
		return this;
	}

	@Override
	public boolean hasNext() {
		return hasMoreElements();
	}

	@Override
	public T next() {
		return nextElement();
	}

	@Override
	public void remove() {
		shouldRemove = true;
	}

	public boolean getShouldRemove() {
		return shouldRemove;
	}

	@Override
	public boolean resume() {
		return step();
	}

}
