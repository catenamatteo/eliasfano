package eliasfano;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.Test;

public class EliasFanoTest {

	/* TODO:
	 * 1) don't resort to random data
	 * 3) test non monotone data
	 * 2) test offsets
	 */
	
	@Test
	public void testDecompressInt() {
	
		EliasFano ef = new EliasFano();
		Random r = new Random(42);
		int[] data1 = new int[1000];
		for (int i = 1; i < data1.length; i++) data1[i] = r.nextInt(Short.MAX_VALUE) + data1[i-1];
		int[] data2 = new int[500];
		for (int i = 1; i < data2.length; i++) data2[i] = r.nextInt(Short.MAX_VALUE) + data2[i-1];
		
		int s1 = ef.getCompressedSize(data1[data1.length-1], data1.length);
		int s2 = ef.getCompressedSize(data2[data2.length-1], data2.length);
		
		long[] comp = new long[s1+s2];
		int L1 = ef.getL(data1[data1.length-1], data1.length);
		int L2 = ef.getL(data2[data2.length-1], data2.length);
		int written = ef.compress(data1, 0, data1.length, comp, 0);
		ef.compress(data2, 0, data2.length, comp, written);
		
		int[] decom1 = new int[data1.length];
		int[] decom2 = new int[data2.length];
		int read = ef.decompress(comp, 0, data1.length, L1, decom1, 0);
		ef.decompress(comp, read, data2.length, L2, decom2, 0);
		
		assertArrayEquals(data1, decom1);
		assertArrayEquals(data2, decom2);
		
		for (int i = 0; i < data1.length; i++)
			assertEquals(data1[i], (int) ef.get(comp, 0, data1.length, L1, i));
		for (int i = 0; i < data2.length; i++)
			assertEquals(data2[i], (int) ef.get(comp, written, data2.length, L2, i));
	}
	
	@Test
	public void testDecompressLong() {
	
		EliasFano ef = new EliasFano();
		Random r = new Random(42);
		long[] data1 = new long[1000];
		for (int i = 1; i < data1.length; i++) data1[i] = r.nextInt(Integer.MAX_VALUE) + data1[i-1];
		long[] data2 = new long[500];
		for (int i = 1; i < data2.length; i++) data2[i] = r.nextInt(Integer.MAX_VALUE) + data2[i-1];
		
		int s1 = ef.getCompressedSize(data1[data1.length-1], data1.length);
		int s2 = ef.getCompressedSize(data2[data2.length-1], data2.length);
		
		long[] comp = new long[s1+s2];
		int L1 = ef.getL(data1[data1.length-1], data1.length);
		int L2 = ef.getL(data2[data2.length-1], data2.length);
		int written = ef.compress(data1, 0, data1.length, comp, 0);
		ef.compress(data2, 0, data2.length, comp, written);
		
		long[] decom1 = new long[data1.length];
		long[] decom2 = new long[data2.length];
		int read = ef.decompress(comp, 0, data1.length, L1, decom1, 0);
		ef.decompress(comp, read, data2.length, L2, decom2, 0);
		
		assertArrayEquals(data1, decom1);
		assertArrayEquals(data2, decom2);	
		
		for (int i = 0; i < data1.length; i++)
			assertEquals(data1[i], ef.get(comp, 0, data1.length, L1, i));
		for (int i = 0; i < data2.length; i++)
			assertEquals(data2[i], ef.get(comp, written, data2.length, L2, i));
	}
	
	@Test
	public void testSelect() {
		
		EliasFano ef = new EliasFano();
		Random r = new Random(42);
		long[] data1 = new long[1000];
		for (int i = 1; i < data1.length; i++) data1[i] = r.nextInt(Short.MAX_VALUE) + data1[i-1];

		int s1 = ef.getCompressedSize(data1[data1.length-1], data1.length);
		
		long[] comp = new long[s1];
		int L1 = ef.getL(data1[data1.length-1], data1.length);
		ef.compress(data1, 0, data1.length, comp, 0);
		
		for (int i = 0; i < data1.length; i++) {
			
			long val = data1[i];
			int pos = -1;
			int j = 0;
			for (; j < data1.length; j++) {
				
				if (data1[j] == val) {
					
					pos = j;
					break;
				}
			}
			assertEquals(pos, ef.select(comp, 0, data1.length, L1, val));
		}
		
		for (int i = 0; i < data1.length-1; i++) {
			
			long val = data1[i]+1;
			if (data1[i+1]!=val) {
				
				assertEquals(i+1, ef.select(comp, 0, data1.length, L1, val));
			}
		}

		assertEquals(-1, ef.select(comp, 0, data1.length, L1, Long.MAX_VALUE));
	}

	@Test
	public void testRank() {
		
		EliasFano ef = new EliasFano();
		Random r = new Random(42);
		long[] data1 = new long[1000];
		for (int i = 1; i < data1.length; i++) data1[i] = r.nextInt(Short.MAX_VALUE) + data1[i-1];
		int numEquals = 20;
		for (int i = 0; i < numEquals; i++) {
			int idx = r.nextInt(data1.length - 1) + 1;
			data1[idx] = data1[idx-1];
		}
		int s1 = ef.getCompressedSize(data1[data1.length-1], data1.length);
		
		long[] comp = new long[s1];
		int L1 = ef.getL(data1[data1.length-1], data1.length);
		ef.compress(data1, 0, data1.length, comp, 0);
		
		for (int i = 0; i < data1.length; i++) {
			
			int cnt = 0;
			for (int j = 0; j < data1.length; j++) {
				
				if (data1[i] == data1[j]) cnt++;
			}
			
			if (cnt != 1) System.err.println("Ok");
			
			assertEquals(cnt, ef.rank(comp, 0, data1.length, L1, data1[i]));
		}

		assertEquals(0, ef.rank(comp, 0, data1.length, L1, Long.MAX_VALUE));
		
	}
}
