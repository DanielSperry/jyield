package jyield.instr;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.instrument.IllegalClassFormatException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class Instrument {

	private Properties options = new Properties();
	private List<String> files = new ArrayList<String>();
	private boolean verbose;
	private String outputDir;
	private boolean overwrite;

	public static void main(String[] args) throws IOException,
			IllegalClassFormatException {
		new Instrument().execute(args);

	}

	private void execute(String[] args) throws IOException,
			IllegalClassFormatException {
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (arg.startsWith("-")) {
				int idx = arg.indexOf(':');
				if (idx > 0) {
					options.put(arg.substring(0, idx).toLowerCase(), arg
							.substring(idx + 1));
				} else {
					options.put(arg.toLowerCase(), "true");
				}
			} else {
				files.add(arg);
			}
		}
		boolean help = options.getProperty("--help") != null;
		if (help || args.length == 0) {
			printHelp();
			return;
		}
		String inputDir = options.getProperty("--inputdir", null);
		boolean recursive = "true".equals(options.getProperty("--recursive"));
		verbose = "true".equals(options.getProperty("--verbose"));
		outputDir = options.getProperty("--outputdir", ".");
		overwrite = "true".equals(options.getProperty("--overwrite"));
		if (inputDir != null) {
			findFiles(inputDir, recursive);
		}
		YieldInstrumentation yi = new YieldInstrumentation();
		for (String file : files) {
			if (verbose) {
				System.out.println(new File(file).getAbsolutePath());
			}
			RandomAccessFile raf = new RandomAccessFile(file, "r");
			byte[] classfileBuffer = new byte[(int) raf.length()];
			try {
				raf.readFully(classfileBuffer);
			} finally {
				raf.close();
			}
			TransformResult tr = yi.transformClass(classfileBuffer);
			if (tr != null) {
				saveFile(tr.className, tr.transformedClassFileBuffer);
				for (Map.Entry<String, byte[]> e : tr.createdClasses.entrySet()) {
					saveFile(e.getKey(), e.getValue());
				}
			}
		}
	}

	public void printHelp() {
		System.out.println("jyield intrumentation");
		System.out
				.println("Instruments classes offline, thus avoiding the need for runtime instrumentation.");
		System.out.println("");
		System.out
				.println("java -jar jyield-VERSION-with-deps.jar [--inputdir:path01] [--outputdir:path02] [--verbose] [--overwrite] [file01] [file02] ...");
		System.out
				.println("	--inputdir:input-path  the input directory to be searched");
		System.out
				.println("	--outputdir:out-path   the output directory, default is the current dir");
		System.out
				.println("	--verbose              prints debug info during instrumentation");
		System.out
				.println("	--overwrite            allows class files to be overwriten");
		System.out.println("	--help                 displays this message");
		System.out.println("");
		System.out.println("Examples: ");
		System.out
				.println("   java -jar jyield-VERSION-with-deps.jar --overwrite Sample.class --verbose");
		System.out
				.println("   java -jar jyield-VERSION-with-deps.jar --overwrite --inputdir:bin --outputdir:bin");
	}

	private void saveFile(String cname, byte[] cbuffer) throws IOException {
		String fname = outputDir + "/" + cname + ".class";
		if (verbose)
			System.out.println(fname);
		File file = new File(fname);
		if (file.exists()) {
			if (!overwrite) {
				throw new RuntimeException(
						"Cannot overwrite: ["
								+ fname
								+ "]. You may want to use the --overwrite command line option.");
			} else if (!file.delete()) {
				throw new RuntimeException("Could not delete: " + fname);
			}
		}
		File parentFile = file.getParentFile();
		if (!parentFile.exists()) {
			parentFile.mkdirs();
		}
		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		try {
			raf.write(cbuffer);
		} finally {
			raf.close();
		}

	}

	private void findFiles(String inputDir, boolean recursive) {
		for (File file : new File(inputDir).listFiles()) {
			if (file.getName().endsWith(".class")) {
				files.add(file.getPath());
			} else if (recursive && file.isDirectory()) {
				findFiles(file.getPath(), recursive);
			}
		}
	}
}
