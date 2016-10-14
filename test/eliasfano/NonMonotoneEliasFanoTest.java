package eliasfano;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import static org.junit.Assert.assertEquals;


@RunWith(Parameterized.class)
public class NonMonotoneEliasFanoTest {

	@Parameters
	public static Collection<int[]> data() {
		return Arrays.asList(new int[][] { { 4, 3, 2, 1, 0, 1, 2, 3, 4 } });
	}

	@Parameter
	public int[] param;

	@Test(expected = IllegalArgumentException.class)
	public void sanityCheck() {

		EliasFanoWriter efw = new EliasFanoWriter();
		efw.compress(param, 0, param.length);
	}
	
	@Test
	public void basicTest() {
		
		int[] copy = Arrays.copyOf(param, param.length);
		copy[0] = param[0];
		for (int i = 1; i < copy.length; i++) copy[i] = param[i]+copy[i-1];
		EliasFanoWriter efw = new EliasFanoWriter();
		byte[] comp = efw.compress(copy, 0, copy.length);
		EliasFanoReader efr = new EliasFanoReader(comp, copy[copy.length-1], copy.length);
		assertEquals(param[0], efr.get(0));
		for (int i = 1; i < param.length; i++) assertEquals(param[i], efr.get(i)-efr.get(i-1));
	}

}
