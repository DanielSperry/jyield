package jyield.instr.test;
//package org.dsperry.yield.instr.test;
//
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.lang.instrument.IllegalClassFormatException;
//import java.lang.reflect.InvocationTargetException;
//import java.util.HashMap;
//import java.util.Map;
//
//import org.dsperry.yield.instr.YieldInstrumentation;
//import org.dsperry.yield.test.LocalVarsTest;
//import org.dsperry.yield.test.LoopTests;
//import org.dsperry.yield.test.SimpleTest;
//import org.junit.Test;
//import org.objectweb.asm.ClassReader;
//import org.objectweb.asm.ClassVisitor;
//import org.objectweb.asm.commons.EmptyVisitor;
//import org.objectweb.asm.util.CheckClassAdapter;
//
//public class InstrumentationTest {
//
//	class CL extends ClassLoader {
//		public Map<String, byte[]> classes = new HashMap<String, byte[]>();
//
//		public void doit() {
//			byte[] b = classes.get(LocalVarsTest.class.getName());
//			defineClass(LocalVarsTest.class.getName(), b, 0, b.length);
//		}
//
//		protected Class<?> myFindClass(String name)
//				throws ClassNotFoundException {
//			byte[] bs = classes.get(name);
//			if (bs == null) {
//				bs = classes.get(name.replace('.', '/'));
//			}
//			if (bs != null) {
//				return defineClass(name, bs, 0, bs.length);
//			}
//			return null;
//		}
//
//		@SuppressWarnings("unchecked")
//		@Override
//		protected synchronized Class<?> loadClass(String name, boolean resolve)
//				throws ClassNotFoundException {
//			Class c = findLoadedClass(name);
//			if (c == null) {
//				c = myFindClass(name);
//			}
//			if (c == null) {
//				c = super.loadClass(name, resolve);
//			}
//			return c;
//		}
//
//	}
//
//	@Test
//	public void simpleTest() throws Throwable {
//		instrumentAndRun(SimpleTest.class.getName(), "testApp");
//	}
//
//	@Test
//	public void varsTest() throws Throwable {
//		instrumentAndRun(LocalVarsTest.class.getName(), "testApp");
//	}
//
//	@Test
//	public void testFor() throws Throwable {
//		instrumentAndRun(LoopTests.class.getName(), "testFor");
//	}
//
//	@Test
//	public void testWhile() throws Throwable {
//		instrumentAndRun(LoopTests.class.getName(), "testWhile");
//	}
//
//	private void instrumentAndRun(String cname, String method)
//			throws IOException, IllegalClassFormatException,
//			ClassNotFoundException, InstantiationException,
//			IllegalAccessException, NoSuchMethodException, Throwable {
//		InputStream in = getClass().getClassLoader().getResourceAsStream(
//				cname.replace('.', '/') + ".class");
//		ByteArrayOutputStream bo = new ByteArrayOutputStream();
//		byte[] b = new byte[64 * 1024];
//		for (int len; 0 < (len = in.read(b));) {
//			bo.write(b, 0, len);
//		}
//		YieldInstrumentation yi = new YieldInstrumentation();
//		byte[] bs = yi.transform(null, null, null, null, bo.toByteArray());
//		ClassReader cr = new ClassReader(bs);
//		ClassVisitor cv = new CheckClassAdapter(new EmptyVisitor());
//		cr.accept(cv, 0);
//
//		CL cl = new CL();
//		cl.classes.putAll(yi.getNewClasses());
//		cl.classes.put(cname, bs);
//		Class<?> c = cl.loadClass(cname);
//		Object test = c.newInstance();
//		try {
//			c.getMethod(method).invoke(test);
//		} catch (InvocationTargetException ex) {
//			throw ex.getCause();
//		}
//	}
//
//}
