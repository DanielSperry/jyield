package jyield.instr;

import java.util.Map;

/**
 * Wrapper class for the result of online class transformer.
 * 
 * @author Daniel Sperry 2010
 */
public class TransformResult {
	public String className;
	public byte[] transformedClassFileBuffer;
	public Map<String, byte[]> createdClasses;
}
