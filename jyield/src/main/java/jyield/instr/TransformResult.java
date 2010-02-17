package jyield.instr;

import java.util.Map;

public class TransformResult {
	public String className;
	public byte[] transformedClassFileBuffer;
	public Map<String, byte[]> createdClasses;
}
