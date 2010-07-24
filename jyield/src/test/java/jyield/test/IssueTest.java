package jyield.test;

import static org.junit.Assert.assertFalse;
import jyield.Continuable;
import jyield.Yield;

import org.junit.Test;

public class IssueTest {

	/**
	 * Tests if the an empty generator works the right way.
	 */
	@Test(timeout = 1000)
	public void yieldNothingTest() {
		assertFalse(yieldNothing().iterator().hasNext());
	}

	@Continuable
	public Iterable<String> yieldNothing() {
		return null;
	}

	@Continuable
	public static Iterable<String> staticGenerator() {
		return null;
	}

	/**
	 * Tests if static function may be generators.
	 */
	@Test(timeout = 1000)
	public void staticMethodInstrumentationTest() {
		assertFalse(staticGenerator().iterator().hasNext());
	}

	@Continuable
	private Iterable<String> privateGenerator() {
		return null;
	}

	/**
	 * Tests if private method may be generators.
	 * <p>
	 * In order to private methods to work a hidden accessor must be added to
	 * the generator class.
	 */
	@Test(timeout = 1000)
	public void privateMethodInstrumentationTest() {
		assertFalse(privateGenerator().iterator().hasNext());
	}

	@Continuable
	private Iterable<String> typeVariable() {
		String a = "aaa";
		if (!(a.endsWith("aa"))) {
			Yield.ret("bla");
		}
		return null;
	}

	/**
	 * Tests if an object variable is restored correctly.
	 * <p>
	 * The object variable restore must typecheck the restored variable or else
	 * the jvm byte verifier fails.
	 */
	@Test
	public void typeVariableTest() {
		assertFalse(typeVariable().iterator().hasNext());
	}

}
