package jyield.instr;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.EmptyVisitor;
import org.objectweb.asm.util.CheckClassAdapter;

/**
 * ClassFileTransformer used by the online instrumentation process.
 * 
 * @author Daniel Sperry 2010
 */
public class YieldInstrumentation implements ClassFileTransformer {

	ConcurrentHashMap<String, byte[]> newClasses = new ConcurrentHashMap<String, byte[]>();
	ConcurrentHashMap<String, Class<?>> definedClasses = new ConcurrentHashMap<String, Class<?>>();

	private static Instrumentation instrumentation;

	private Method declaredMethod;

	@Override
	public byte[] transform(ClassLoader loader, String className,
			Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
			byte[] classfileBuffer) throws IllegalClassFormatException {
		TransformResult tr = transformClass(classfileBuffer);
		if (tr == null) {
			return null;
		}
		for (Map.Entry<String, byte[]> e : tr.createdClasses.entrySet()) {
			loadClass(loader, e.getKey().replace('/', '.'), e.getValue());
		}
		return tr.transformedClassFileBuffer;
	}

	public TransformResult transformClass(byte[] classfileBuffer) {
		try {
			ClassReader cr = new ClassReader(classfileBuffer);

			ClassTaster taster = new ClassTaster();
			cr.accept(taster, 0);

			if (!taster.isShouldInstrument()) {
				return null;
			}
			TransformResult tr = new TransformResult();
			tr.className = taster.name;

			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS
					| ClassWriter.COMPUTE_FRAMES);
			YieldClassInstr cv = new YieldClassInstr(cw);
			cr.accept(cv, 0);
			tr.transformedClassFileBuffer = cw.toByteArray();
			checkClass(tr.transformedClassFileBuffer);
//			ASMifierClassVisitor asmifierClassVisitor = new ASMifierClassVisitor(
//					new PrintWriter(System.out));
//			new ClassReader(tr.transformedClassFileBuffer).accept(
//					asmifierClassVisitor, 0);
			tr.createdClasses = cv.createdClasses;
			return tr;
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
			throw new RuntimeException(ex);
		}
	}

	private void checkClass(byte[] byteArray) {
		try {
			ClassReader cr2 = new ClassReader(byteArray);
			ClassVisitor cv2 = new CheckClassAdapter(new EmptyVisitor());
			cr2.accept(cv2, 0);
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}
	}

	private void loadClass(ClassLoader loader, String className,
			byte[] byteArray) {
		checkClass(byteArray);
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

	/**
	 * This method is called by the jvm as result of using the -javaagent parameter.
	 */
	public static void premain(String agentArguments,
			Instrumentation instrumentation) {
		// System.out.println("jyield instrumentation!");
		YieldInstrumentation.instrumentation = instrumentation;
		instrumentation.addTransformer(new YieldInstrumentation());
	}

}
