package jyield.test;

import static org.junit.Assert.assertFalse;
import jyield.Continuable;

import org.junit.Test;

public class IssueTest {

	@Test(timeout = 1000)
	public void yieldNothingTest() {
		assertFalse(yieldNothing().iterator().hasNext());
	}

	@Continuable
	public Iterable<String> yieldNothing() {
		return null;
	}

}
