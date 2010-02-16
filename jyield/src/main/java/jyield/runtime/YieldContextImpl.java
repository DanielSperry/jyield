package jyield.runtime;

import java.util.Enumeration;
import java.util.Iterator;

public class YieldContextImpl<T> implements YieldContext<T>, Iterator<T> {
	protected Object target;
	private Object[] objectVariables;
	private long[] primitiveVariables;
	private Object nextValue;
	private boolean hasNext;
	private boolean mustStep = true;
	private boolean done;
	private int nextLine;
	@SuppressWarnings("unchecked")
	private Enumeration joined;
	private boolean shouldRemove;

	public YieldContextImpl(int localsCount, Object target) {
		super();
		this.target = target;
		if (localsCount > 0) {
			objectVariables = new Object[localsCount];
			primitiveVariables = new long[localsCount];
		}
	}

	@SuppressWarnings("unchecked")
	public Enumeration ret(Object obj, int nextLine) {
		this.nextLine = nextLine;
		nextValue = obj;
		hasNext = true;
		return this;
	}

	@SuppressWarnings("unchecked")
	public Enumeration join(Enumeration joined) {
		this.joined = joined;
		return joined;
	}

	public int getNextLine() {
		return nextLine;
	}

	public YieldContext<T> done() {
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
		}
		if (mustStep) {
			step();
			mustStep = false;
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
		if (mustStep)
			step();
		if (!done)
			mustStep = true;
		return (T) nextValue;
	}

	protected void step() {
		doStep();
		shouldRemove = false;
	}

	@SuppressWarnings("unchecked")
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

}
