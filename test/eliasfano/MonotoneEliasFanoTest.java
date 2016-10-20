package eliasfano;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import me.lemire.integercompression.synth.ClusteredDataGenerator;

@RunWith(Parameterized.class)
public class MonotoneEliasFanoTest {

	@Parameters
    public static Collection<int[]> data() {
        return Arrays.asList(new int[][] {
        	{0},
        	{1},
        	{Byte.MAX_VALUE},
        	{Short.MAX_VALUE},
        	{Integer.MAX_VALUE},
        	{0,1,2,3,4},
        	{0,Byte.MAX_VALUE,Short.MAX_VALUE,Integer.MAX_VALUE},
        	{0,1,Byte.MAX_VALUE-1,Byte.MAX_VALUE,Byte.MAX_VALUE+1,Short.MAX_VALUE-1,Short.MAX_VALUE,Short.MAX_VALUE+1,Integer.MAX_VALUE-1,Integer.MAX_VALUE},
        	{0, 0, 0, Byte.MAX_VALUE, Byte.MAX_VALUE, Byte.MAX_VALUE}
           });
    }
    
    @Parameter
    public int[] param;
	
	@Test
	public void getTest() {
		
		EliasFanoWriter efw = new EliasFanoWriter();
		byte[] comp = new byte[efw.getSafeCompressedLength(param, 0, param.length)];
		efw.compress(param, 0, param.length, comp, 0, true);
		EliasFanoReader efr = new EliasFanoReader();
		for (int i = 0; i < param.length; i++) assertEquals(param[i], efr.get(comp, 0, param[param.length - 1], param.length, i));
	}
	
	@Test
	public void decompressTest() {
		
		EliasFanoWriter efw = new EliasFanoWriter();
		byte[] comp = new byte[efw.getSafeCompressedLength(param, 0, param.length)];
		efw.compress(param, 0, param.length, comp, 0, true);
		EliasFanoReader efr = new EliasFanoReader();
		int[] decom = new int[param.length];
		efr.decompress(comp, 0, param[param.length - 1], param.length, decom, 0);
		assertArrayEquals(param, decom);
	}
	
	/*
	 * Test if select returns the same position of a linear scan
	 */
	@Test
	public void selectTest1() {
		
		EliasFanoWriter efw = new EliasFanoWriter();
		byte[] comp = new byte[efw.getSafeCompressedLength(param, 0, param.length)];
		efw.compress(param, 0, param.length, comp, 0, true);
		EliasFanoReader efr = new EliasFanoReader();
		for (int i = 0; i < param.length; i++) {
			int pos1 = -1;
			for (int j = 0; j < param.length; j++) 
				if (param[j]==param[i]) {
					pos1 = j;
					break;
				}
			assertEquals(pos1, efr.select(comp, 0, param[param.length-1], param.length, param[i]));
		}
	}
	
	/*
	 * Test if select returns the same position of a linear scan, for missing
	 * values 
	 */
	@Test
	public void selectTest2() {
		
		EliasFanoWriter efw = new EliasFanoWriter();
		byte[] comp = new byte[efw.getSafeCompressedLength(param, 0, param.length)];
		efw.compress(param, 0, param.length, comp, 0, true);
		EliasFanoReader efr = new EliasFanoReader();
		int val = Integer.MAX_VALUE;
		int pos = -1;
		for (int i = 0; i < param.length; i++) {
			if (param[i] >= val) {
				pos = i;
				break;
			}
		}
		assertEquals(pos, efr.select(comp, 0, param[param.length-1], param.length, val));
		
	}
	
	/*
	 * Test if rank returns the same count of a linear scan
	 */
	@Test
	public void rankTest() {
		
		EliasFanoWriter efw = new EliasFanoWriter();
		byte[] comp = new byte[efw.getSafeCompressedLength(param, 0, param.length)];
		efw.compress(param, 0, param.length, comp, 0, true);
		EliasFanoReader efr = new EliasFanoReader();
		for (int i = 0; i < param.length; i++) {
			int cnt = 0;
			for (int j = 0; j < param.length; j++) 
				if (param[j]==param[i]) {
					cnt++;
				}
			assertEquals(cnt, efr.rank(comp, 0, param[param.length-1], param.length, param[i]));
		}
	}
	
	/*
	 * Test if rank returns the same count of a linear scan, for
	 * possibly missing values
	 */
	@Test
	public void rankTest2() {
		
		EliasFanoWriter efw = new EliasFanoWriter();
		byte[] comp = new byte[efw.getSafeCompressedLength(param, 0, param.length)];
		efw.compress(param, 0, param.length, comp, 0, true);
		EliasFanoReader efr = new EliasFanoReader();
		int val = Integer.MAX_VALUE;
		int cnt = 0;
		for (int i = 0; i < param.length; i++) 
			if (param[i]==val) {
				cnt++;
			}
		assertEquals(cnt, efr.rank(comp, 0, param[param.length-1], param.length, val));
	}

	@Test
	public void randomTest() {
		
		ClusteredDataGenerator cdg = new ClusteredDataGenerator();
		int[] data = cdg.generateClustered(262143, 524287);
		EliasFanoWriter efw = new EliasFanoWriter();
		byte[] comp = new byte[efw.getSafeCompressedLength(data, 0, data.length)];
		efw.compress(data, 0, data.length, comp, 0, true);
		EliasFanoReader efr = new EliasFanoReader();
		int[] decom = new int[data.length];
		efr.decompress(comp, 0, data[data.length - 1], data.length, decom, 0);
		assertArrayEquals(data, decom);
		data = cdg.generateClustered(262143, 524287);
		Arrays.fill(comp, (byte)0); //TODO: solve this...otherwise you can't reuse array
		efw.compress(data, 0, data.length, comp, 0, true);
		efr.decompress(comp, 0, data[data.length - 1], data.length, decom, 0);
		assertArrayEquals(data, decom);
		
	}
	
}
