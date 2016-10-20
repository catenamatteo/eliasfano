package eliasfano;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class OffsetAndLengthTest {

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                 { 0, in.length }, 
                 { 0, in.length - 1}, 
                 { 1, in.length - 1},
                 { 2, in.length - 2},
                 { 0, 1 }, 
                 { 1, 1 }  
           });
    }
	
	public static int[] in = {0, 1, 2, 3, 4, 5, 6, Byte.MAX_VALUE, Short.MAX_VALUE, Short.MAX_VALUE + Byte.MAX_VALUE};
	@Parameter(value = 0)
	public int offset;
	@Parameter(value = 1)
	public int len;

	/*
	 * Get using offset and length on the original array
	 */
	@Test
	public void getTest1() {
		
		EliasFanoWriter efw = new EliasFanoWriter();
		byte[] comp = new byte[efw.getSafeCompressedLength(in, offset, len)]; 
		efw.compress(in, offset, len, comp, 0, true);
		EliasFanoReader efr = new EliasFanoReader();
		for (int i = 0; i < len; i++) 
			assertEquals(in[i+offset], efr.get(comp, 0, in[offset+len-1], len , i));

	}
	
	/*
	 * Get using offset on the compressed array
	 */
	@Test
	public void getTest2() {
		
		EliasFanoWriter efw = new EliasFanoWriter();
		byte[] comp = new byte[efw.getSafeCompressedLength(in, 0, in.length)+offset]; 
		efw.compress(in, 0, in.length, comp, offset, true);
		EliasFanoReader efr = new EliasFanoReader();
		for (int i = 0; i < in.length; i++) 
			assertEquals(in[i], efr.get(comp, offset, in[in.length-1], in.length , i));

	}
	
	/*
	 * Decompress using offset and length on the original array
	 */
	@Test
	public void decompressTest1() {
		
		EliasFanoWriter efw = new EliasFanoWriter();
		byte[] comp = new byte[efw.getSafeCompressedLength(in, offset, len)];
		efw.compress(in, offset, len, comp, 0, true);
		EliasFanoReader efr = new EliasFanoReader();
		int[] b = new int[len];
		efr.decompress(comp, 0, in[offset+len-1], len, b, 0);
		assertArrayEquals(Arrays.copyOfRange(in, offset, offset+len), b);

	}
	

	/*
	 * Decompress using offset on the compressed array
	 */
	@Test
	public void decompressTest2() {
		
		EliasFanoWriter efw = new EliasFanoWriter();
		byte[] comp = new byte[efw.getSafeCompressedLength(in, 0, in.length)+offset];
		efw.compress(in, 0, in.length, comp, offset, true);
		EliasFanoReader efr = new EliasFanoReader();
		int[] b = new int[in.length];
		efr.decompress(comp, offset, in[in.length-1], in.length, b, 0);
		assertArrayEquals(Arrays.copyOfRange(in, 0, in.length), b);

	}
	
	/*
	 * Select using offset and length on the original array
	 */
	@Test
	public void selectTest1() {
		
		EliasFanoWriter efw = new EliasFanoWriter();
		byte[] comp = new byte[efw.getSafeCompressedLength(in, offset, len)];
		efw.compress(in, offset, len, comp, 0, true);
		EliasFanoReader efr = new EliasFanoReader();
		for (int i = 0; i < len; i++) {
			int pos1 = -1;
			for (int j = 0; j < len; j++) 
				if (in[j+offset]==in[i+offset]) {
					pos1 = j+offset;
					break;
				}
			assertEquals(pos1, efr.select(comp, 0, in[offset+len-1], len, in[i+offset])+offset);
		}
		assertEquals(-1, efr.select(comp, 0, in[offset+len-1], len, Integer.MAX_VALUE));
	}
	
	/*
	 * Select using offset on the compressed array
	 */
	@Test
	public void selectTest2() {
		
		EliasFanoWriter efw = new EliasFanoWriter();
		byte[] comp = new byte[efw.getSafeCompressedLength(in, 0, in.length)+offset];
		efw.compress(in, 0, in.length, comp, offset, true);
		EliasFanoReader efr = new EliasFanoReader();
		for (int i = 0; i < in.length; i++) {
			int pos1 = -1;
			for (int j = 0; j < in.length; j++) 
				if (in[j]==in[i]) {
					pos1 = j;
					break;
				}
			assertEquals(pos1, efr.select(comp, offset, in[in.length-1], in.length, in[i]));
		}
		assertEquals(-1, efr.select(comp, offset, in[in.length-1], in.length, Integer.MAX_VALUE));
	}
	
	/*
	 * Rank using offset and length on the original array
	 */
	@Test
	public void rankTest1() {
		
		EliasFanoWriter efw = new EliasFanoWriter();
		byte[] comp = new byte[efw.getSafeCompressedLength(in, offset, len)];
		efw.compress(in, offset, len, comp, 0, true);
		EliasFanoReader efr = new EliasFanoReader();
		for (int i = 0; i < len; i++) {
			int cnt = 0;
			for (int j = 0; j < len; j++) 
				if (in[j+offset]==in[i+offset]) {
					cnt++;
				}
			assertEquals(cnt, efr.rank(comp, 0, in[offset+len-1], len, in[i+offset]));
		}
	}
	
	/*
	 * Rank using offset on the compressed array
	 */
	@Test
	public void rankTest2() {
		
		EliasFanoWriter efw = new EliasFanoWriter();
		byte[] comp = new byte[efw.getSafeCompressedLength(in, 0, in.length)+offset];
		efw.compress(in, 0, in.length, comp, offset, true);
		EliasFanoReader efr = new EliasFanoReader();
		for (int i = 0; i < in.length; i++) {
			int cnt = 0;
			for (int j = 0; j < in.length; j++) 
				if (in[j]==in[i]) {
					cnt++;
				}
			assertEquals(cnt, efr.rank(comp, offset, in[in.length-1], in.length, in[i]));
		}
	}
	
}
