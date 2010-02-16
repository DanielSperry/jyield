package jyield.instr;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.EmptyVisitor;
import org.objectweb.asm.util.CheckClassAdapter;

public class YieldInstrumentation implements ClassFileTransformer {
	static final String SUFFIX = ")L"
			+ Enumeration.class.getName().replace('.', '/') + ";";

	ConcurrentHashMap<String, byte[]> newClasses = new ConcurrentHashMap<String, byte[]>();
	ConcurrentHashMap<String, Class<?>> definedClasses = new ConcurrentHashMap<String, Class<?>>();

	private static Instrumentation instrumentation;

	private Method declaredMethod;

	@Override
	public byte[] transform(ClassLoader loader, String className,
			Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
			byte[] classfileBuffer) throws IllegalClassFormatException {
		ClassReader cr = new ClassReader(classfileBuffer);

		ClassTaster tester = new ClassTaster();
		cr.accept(tester, 0);

		if (!tester.isShouldInstrument()) {
			return null;
		}

		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS
				| ClassWriter.COMPUTE_FRAMES);
		ClassVisitor cv = new YieldClassInstr(this, loader,
				classBeingRedefined, cw);
		cr.accept(cv, 0);
		byte[] byteArray = cw.toByteArray();
		try {
			ClassReader cr2 = new ClassReader(byteArray);
			ClassVisitor cv2 = new CheckClassAdapter(new EmptyVisitor());
			cr2.accept(cv2, 0);
		} catch (Exception ex) {
			System.err.println(ex);
		}

		return byteArray;
	}

	public void loadClass(ClassLoader loader, String className, byte[] byteArray) {
		if (instrumentation != null) {
			try {
				if (!definedClasses.containsKey(className)) {
					if (declaredMethod == null) {
						Method dm = ClassLoader.class.getDeclaredMethod(
								"defineClass", String.class, byte[].class,
								int.class, int.class);
						// ehehe
						dm.setAccessible(true);
						declaredMethod = dm;
					}
					Class<?> clazz = (Class<?>) declaredMethod.invoke(loader,
							className, byteArray, 0, byteArray.length);
					definedClasses.put(className, clazz);
				} else {
					instrumentation.redefineClasses(new ClassDefinition(
							definedClasses.get(className), byteArray));
				}
			} catch (Exception e) {
				throw new RuntimeException(
						"Error instrumenting continuation while loading generated class: "
								+ className, e);
			}
		} else {
			newClasses.put(className, byteArray);
		}
	}

	public ConcurrentHashMap<String, byte[]> getNewClasses() {
		return newClasses;
	}

	public static void premain(String agentArguments,
			Instrumentation instrumentation) {
		// System.out.println("jyield instrumentation!");
		YieldInstrumentation.instrumentation = instrumentation;
		instrumentation.addTransformer(new YieldInstrumentation());
	}

}
