package eliasfano;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class NonMonotoneEliasFanoTest {

	@Parameters
	public static Collection<int[]> data() {
		return Arrays.asList(new int[][] { { 4, 3, 2, 1, 0, 1, 2, 3, 4 },
				{ Short.MAX_VALUE * 2, Short.MAX_VALUE, Byte.MAX_VALUE * 2, Byte.MAX_VALUE, 1, 0 } });
	}

	@Parameter
	public int[] param;

	@Test(expected = IllegalArgumentException.class)
	public void sanityCheck() {

		EliasFanoWriter efw = new EliasFanoWriter();
		byte[] comp = new byte[efw.getSafeCompressedLength(param, 0, param.length)];
		efw.compress(param, 0, param.length, comp, 0, true);
	}

	@Test
	public void getTest() {

		int[] copy = Arrays.copyOf(param, param.length);
		copy[0] = param[0];
		for (int i = 1; i < copy.length; i++)
			copy[i] = param[i] + copy[i - 1];
		EliasFanoWriter efw = new EliasFanoWriter();
		byte[] comp = new byte[efw.getSafeCompressedLength(copy, 0, copy.length)];
		efw.compress(copy, 0, copy.length, comp, 0, true);
		EliasFanoReader efr = new EliasFanoReader();
		int _0 = efr.get(comp, 0, copy[copy.length - 1], copy.length, 0);
		assertEquals(param[0], _0);
		for (int i = 1; i < param.length; i++) {
			int a = efr.get(comp, 0, copy[copy.length - 1], copy.length, i - 1);
			int b = efr.get(comp, 0, copy[copy.length - 1], copy.length, i);
			assertEquals(param[i], b - a);
		}
	}

	@Test
	public void decompressTest() {

		int[] copy = Arrays.copyOf(param, param.length);
		copy[0] = param[0];
		for (int i = 1; i < copy.length; i++)
			copy[i] = param[i] + copy[i - 1];
		EliasFanoWriter efw = new EliasFanoWriter();
		byte[] comp = new byte[efw.getSafeCompressedLength(copy, 0, copy.length)];
		efw.compress(copy, 0, copy.length, comp, 0, true);
		EliasFanoReader efr = new EliasFanoReader();
		int[] b = new int[copy.length];
		efr.decompress(comp, 0, copy[copy.length - 1], copy.length, b, 0);
		assertArrayEquals(copy, b);
	}

}
