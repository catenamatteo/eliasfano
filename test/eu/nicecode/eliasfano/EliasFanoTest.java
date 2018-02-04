/*
 * Copyright 2016 Matteo Catena
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.nicecode.eliasfano;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import eu.nicecode.eliasfano.EliasFano;

/**
 * 
 * @author Matteo Catena
 *
 */
public class EliasFanoTest {
	
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
	
	@Test
	public void testDecompressInt() throws IOException {
	
		int[] data1 = loadInts("resources/1000ints.txt");
		int[] data2 = loadInts("resources/500ints.txt");
		
		int s1 = EliasFano.getCompressedSize(data1[data1.length-1], data1.length);
		int s2 = EliasFano.getCompressedSize(data2[data2.length-1], data2.length);
		
		byte[] comp = new byte[s1+s2];
		int L1 = EliasFano.getL(data1[data1.length-1], data1.length);
		int L2 = EliasFano.getL(data2[data2.length-1], data2.length);
		byte[] written1 = EliasFano.compress(data1, 0, data1.length);
		byte[] written2 = EliasFano.compress(data2, 0, data2.length);
		System.arraycopy(written1, 0, comp, 0, s1);
		System.arraycopy(written2, 0, comp, s1, s2);
		
		int[] decom1 = new int[data1.length];
		int[] decom2 = new int[data2.length];
		int read = EliasFano.decompress(comp, 0, data1.length, L1, decom1, 0);
		EliasFano.decompress(comp, read + 1, data2.length, L2, decom2, 0);
		
		assertArrayEquals(data1, decom1);
		assertArrayEquals(data2, decom2);
		
		for (int i = 0; i < data1.length; i++)
			assertEquals(data1[i], (int) EliasFano.get(comp, 0, data1.length, L1, i));
		for (int i = 0; i < data2.length; i++)
			assertEquals(data2[i], (int) EliasFano.get(comp, s1, data2.length, L2, i));
	}
	
	@Test
	public void testOffset() throws IOException {
	
		int[] data1 = loadInts("resources/1000ints.txt");
		
		int s1 = EliasFano.getCompressedSize(data1[data1.length-2], data1.length-2);
		
		byte[] comp = new byte[s1+1];
		int L1 = EliasFano.getL(data1[data1.length-2], data1.length-2);
		byte[] comp2 = EliasFano.compress(data1, 1, data1.length-2);
		System.arraycopy(comp2, 0, comp, 1, comp2.length);
		
		int[] decom1 = new int[data1.length];
		EliasFano.decompress(comp, 1, data1.length-2, L1, decom1, 1);
		
		//test decompress
		for (int i = 1; i < data1.length - 1; i++) 
			assertEquals(data1[i], decom1[i]);
		
		//test get
		for (int i = 1; i < data1.length - 1; i++)
			assertEquals(data1[i], EliasFano.get(comp, 1, data1.length-2, L1, i-1));
		
	}
	
	@Test
	public void testNonMonotone() throws IOException {
		
		
		int[] data1 = loadInts("resources/1000ints_non_monotone.txt");
		int[] data2 = new int[data1.length];
		data2[0] = data1[0];
		for (int i = 1; i < data1.length; i++) data2[i]=data1[i]+data2[i-1];
		
		int L2 = EliasFano.getL(data2[data2.length-1], data2.length);
		byte[] comp = EliasFano.compress(data2, 0, data2.length);
		
		assertEquals(data1[0], EliasFano.get(comp, 0, data2.length, L2, 0));
		for (int i = 1; i < data1.length; i++)
			assertEquals(data1[i], EliasFano.get(comp, 0, data2.length, L2, i)-EliasFano.get(comp, 0, data2.length, L2, i-1));
	}
	
	@Test
	public void testSelect() throws IOException {
		
		int[] data1 = loadInts("resources/1000ints.txt");
		
		int L1 = EliasFano.getL(data1[data1.length-1], data1.length);
		byte[] comp = EliasFano.compress(data1, 0, data1.length);
		
		for (int i = 0; i < data1.length; i++) {
			
			int val = data1[i];
			int pos = -1;
			int j = 0;
			for (; j < data1.length; j++) {
				
				if (data1[j] == val) {
					
					pos = j;
					break;
				}
			}
			assertEquals(pos, EliasFano.select(comp, 0, data1.length, L1, val));
		}
		
		for (int i = 0; i < data1.length-1; i++) {
			
			int val = data1[i]+1;
			if (data1[i+1]!=val) {
				
				assertEquals(i+1, EliasFano.select(comp, 0, data1.length, L1, val));
			}
		}

		assertEquals(-1, EliasFano.select(comp, 0, data1.length, L1, Integer.MAX_VALUE));
	}

	@Test
	public void testRank() throws IOException {
		
		int[] data1 = loadInts("resources/1000ints_with_duplicates.txt");
				
		int L1 = EliasFano.getL(data1[data1.length-1], data1.length);
		byte[] comp = EliasFano.compress(data1, 0, data1.length);
		
		for (int i = 0; i < data1.length; i++) {
			
			int cnt = 0;
			for (int j = 0; j < data1.length; j++) {
				
				if (data1[i] == data1[j]) cnt++;
			}
			
			assertEquals(cnt, EliasFano.rank(comp, 0, data1.length, L1, data1[i]));
		}

		assertEquals(0, EliasFano.rank(comp, 0, data1.length, L1, Integer.MAX_VALUE));
		
	}
}
