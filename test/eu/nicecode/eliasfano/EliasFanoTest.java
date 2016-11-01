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
	
		EliasFano ef = new EliasFano();
		int[] data1 = loadInts("resources/1000ints.txt");
		int[] data2 = loadInts("resources/500ints.txt");
		
		int s1 = ef.getCompressedSize(data1[data1.length-1], data1.length);
		int s2 = ef.getCompressedSize(data2[data2.length-1], data2.length);
		
		byte[] comp = new byte[s1+s2];
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
	public void testOffset() throws IOException {
	
		EliasFano ef = new EliasFano();
		int[] data1 = loadInts("resources/1000ints.txt");
		
		int s1 = ef.getCompressedSize(data1[data1.length-2], data1.length-2);
		
		byte[] comp = new byte[s1+1];
		int L1 = ef.getL(data1[data1.length-2], data1.length-2);
		ef.compress(data1, 1, data1.length-2, comp, 1);
		
		int[] decom1 = new int[data1.length];
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
		int[] data2 = new int[data1.length];
		data2[0] = data1[0];
		for (int i = 1; i < data1.length; i++) data2[i]=data1[i]+data2[i-1];
		
		int s2 = ef.getCompressedSize(data2[data2.length-1], data2.length);
		
		byte[] comp = new byte[s2];
		int L2 = ef.getL(data2[data2.length-1], data2.length);
		ef.compress(data2, 0, data2.length, comp, 0);
		
		assertEquals(data1[0], ef.get(comp, 0, data2.length, L2, 0));
		for (int i = 1; i < data1.length; i++)
			assertEquals(data1[i], ef.get(comp, 0, data2.length, L2, i)-ef.get(comp, 0, data2.length, L2, i-1));
	}
	
	@Test
	public void testSelect() throws IOException {
		
		EliasFano ef = new EliasFano();
		int[] data1 = loadInts("resources/1000ints.txt");

		int s1 = ef.getCompressedSize(data1[data1.length-1], data1.length);
		
		byte[] comp = new byte[s1];
		int L1 = ef.getL(data1[data1.length-1], data1.length);
		ef.compress(data1, 0, data1.length, comp, 0);
		
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
			assertEquals(pos, ef.select(comp, 0, data1.length, L1, val));
		}
		
		for (int i = 0; i < data1.length-1; i++) {
			
			int val = data1[i]+1;
			if (data1[i+1]!=val) {
				
				assertEquals(i+1, ef.select(comp, 0, data1.length, L1, val));
			}
		}

		assertEquals(-1, ef.select(comp, 0, data1.length, L1, Integer.MAX_VALUE));
	}

	@Test
	public void testRank() throws IOException {
		
		EliasFano ef = new EliasFano();
		int[] data1 = loadInts("resources/1000ints_with_duplicates.txt");
		
		int s1 = ef.getCompressedSize(data1[data1.length-1], data1.length);
		
		byte[] comp = new byte[s1];
		int L1 = ef.getL(data1[data1.length-1], data1.length);
		ef.compress(data1, 0, data1.length, comp, 0);
		
		for (int i = 0; i < data1.length; i++) {
			
			int cnt = 0;
			for (int j = 0; j < data1.length; j++) {
				
				if (data1[i] == data1[j]) cnt++;
			}
			
			assertEquals(cnt, ef.rank(comp, 0, data1.length, L1, data1[i]));
		}

		assertEquals(0, ef.rank(comp, 0, data1.length, L1, Integer.MAX_VALUE));
		
	}
}
