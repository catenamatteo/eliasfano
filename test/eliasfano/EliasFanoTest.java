package eliasfano;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class EliasFanoTest {

	/* TODO:
	 * 1) test offsets
	 */
	
	private int[] loadInts(String filename) throws IOException {
		
		BufferedReader br = new BufferedReader(new FileReader(filename));
		List<Integer> l = new ArrayList<Integer>();
		String line = null;
		while ((line = br.readLine()) != null) l.add(Integer.parseInt(line));
		int[] data = new int[l.size()];
		for (int i = 0; i < l.size(); i++) data[i] = l.get(i);
		br.close();
		return data;
	}

	private long[] loadLongs(String filename) throws IOException {
		
		BufferedReader br = new BufferedReader(new FileReader(filename));
		List<Long> l = new ArrayList<Long>();
		String line = null;
		while ((line = br.readLine()) != null) l.add(Long.parseLong(line));
		long[] data = new long[l.size()];
		for (int i = 0; i < l.size(); i++) data[i] = l.get(i);
		br.close();
		return data;
	}
	
	@Test
	public void testDecompressInt() throws IOException {
	
		EliasFano ef = new EliasFano();
		int[] data1 = loadInts("resources/1000ints.txt");
		int[] data2 = loadInts("resources/500ints.txt");
		
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
	public void testDecompressLong() throws IOException {
	
		EliasFano ef = new EliasFano();
		long[] data1 = loadLongs("resources/1000longs.txt");
		long[] data2 = loadLongs("resources/500longs.txt");
		
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
	public void testOffset() throws IOException {
	
		EliasFano ef = new EliasFano();
		long[] data1 = loadLongs("resources/1000longs.txt");
		
		int s1 = ef.getCompressedSize(data1[data1.length-2], data1.length-2);
		
		long[] comp = new long[s1+1];
		int L1 = ef.getL(data1[data1.length-2], data1.length-2);
		ef.compress(data1, 1, data1.length-2, comp, 1);
		
		long[] decom1 = new long[data1.length];
		ef.decompress(comp, 1, data1.length-2, L1, decom1, 1);
		
		//test decompress
		for (int i = 1; i < data1.length - 1; i++) 
			assertEquals(data1[i], decom1[i]);
		
		//test get
		for (int i = 1; i < data1.length - 1; i++)
			assertEquals(data1[i], ef.get(comp, 1, data1.length-2, L1, i-1));
		
	}
	
	@Test
	public void testNonMonotone() throws IOException {
		
		EliasFano ef = new EliasFano();
		
		int[] data1 = loadInts("resources/1000ints_non_monotone.txt");
		long[] data2 = new long[data1.length];
		data2[0] = data1[0];
		for (int i = 1; i < data1.length; i++) data2[i]=data1[i]+data2[i-1];
		
		int s2 = ef.getCompressedSize(data2[data2.length-1], data2.length);
		
		long[] comp = new long[s2];
		int L2 = ef.getL(data2[data2.length-1], data2.length);
		ef.compress(data2, 0, data2.length, comp, 0);
		
		assertEquals(data1[0], ef.get(comp, 0, data2.length, L2, 0));
		for (int i = 1; i < data1.length; i++)
			assertEquals(data1[i], ef.get(comp, 0, data2.length, L2, i)-ef.get(comp, 0, data2.length, L2, i-1));
	}
	
	@Test
	public void testSelect() throws IOException {
		
		EliasFano ef = new EliasFano();
		long[] data1 = loadLongs("resources/1000longs.txt");

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
	public void testRank() throws IOException {
		
		EliasFano ef = new EliasFano();
		long[] data1 = loadLongs("resources/1000longs_with_duplicates.txt");
		
		int s1 = ef.getCompressedSize(data1[data1.length-1], data1.length);
		
		long[] comp = new long[s1];
		int L1 = ef.getL(data1[data1.length-1], data1.length);
		ef.compress(data1, 0, data1.length, comp, 0);
		
		for (int i = 0; i < data1.length; i++) {
			
			int cnt = 0;
			for (int j = 0; j < data1.length; j++) {
				
				if (data1[i] == data1[j]) cnt++;
			}
			
			assertEquals(cnt, ef.rank(comp, 0, data1.length, L1, data1[i]));
		}

		assertEquals(0, ef.rank(comp, 0, data1.length, L1, Long.MAX_VALUE));
		
	}
}
