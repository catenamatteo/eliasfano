/*
 * Copyright 2016-2018 Matteo Catena
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

import org.junit.Test;

import eu.nicecode.eliasfano.Bits;

import static org.junit.Assert.*;
/**
 * 
 * @author Matteo Catena
 *
 */
public class BitsTest {

	@Test
	public void testUnary() {
		
		byte[] in = new byte[32];
		Bits.writeUnary(in, 0, 0);
		Bits.writeUnary(in, 1, 1);
		Bits.writeUnary(in, 3, 2);
		Bits.writeUnary(in, 6, 20);
		assertEquals(0, Bits.readUnary(in, 0));
		assertEquals(1, Bits.readUnary(in, 1));
		assertEquals(2, Bits.readUnary(in, 3));
		assertEquals(20, Bits.readUnary(in, 6));
	}
	
	@Test
	public void testBinary() {
		

		byte[] in = new byte[32];
		Bits.writeBinary(in, 0, 0, 0);
		Bits.writeBinary(in, 0, 1, 1);
		Bits.writeBinary(in, 1, 1, 1);
		Bits.writeBinary(in, 2, 2, 2);
		Bits.writeBinary(in, 4, 2, 3);
		Bits.writeBinary(in, 8, Short.MAX_VALUE, Short.SIZE);
		Bits.writeBinary(in, 24, Integer.MAX_VALUE, Integer.SIZE);
		Bits.writeBinary(in, 56, Short.MAX_VALUE, Integer.SIZE);
		assertEquals(0, Bits.readBinary(in, 0, 0));
		assertEquals(1, Bits.readBinary(in, 0, 1));
		assertEquals(1, Bits.readBinary(in, 1, 1));
		assertEquals(2, Bits.readBinary(in, 2, 2));
		assertEquals(2, Bits.readBinary(in, 4, 3));
		assertEquals(Short.MAX_VALUE, Bits.readBinary(in, 8, Short.SIZE));
		assertEquals(Integer.MAX_VALUE, Bits.readBinary(in, 24, Integer.SIZE));
		assertEquals(Short.MAX_VALUE, Bits.readBinary(in, 56, Integer.SIZE));
	}
	
}
