package CliqInformer;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SampleMethodTest {

	@Test
	public void test() {
		TestProject t =  new TestProject();
		assertEquals(t.SampleMethod(),"SampleMethod");
	}

}
