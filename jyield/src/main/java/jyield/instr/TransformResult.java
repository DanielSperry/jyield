package jyield.instr;

import java.util.Map;

/**
 * Wrapper class for the result of online class transformer.
 * 
 * @author Daniel Sperry - 2010
 */
public class TransformResult {
	public String className;
	public byte[] transformedClassFileBuffer;

	/**
	 * New classes created during the bytecode transformation. For jyield those
	 * classes are the YieldContextImpl extensions custom made for each
	 * coroutine.
	 * <p>
	 * During the online instrumentation these classes must be passed to the
	 * ClassLoader.
	 */
	public Map<String, byte[]> createdClasses;
}
