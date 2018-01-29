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

/**
 * A support class for unaligned binary writing/reading and unary writing/reading
 * 
 * @author Matteo Catena
 *
 */
public class Bits {
	
	public void writeBinary(byte[] in, long bitOffset, int val, int numbits) {
		
		while (numbits > 0) {

			int longOffset = (int) (bitOffset / Byte.SIZE);
			int bitPosition = (int) (bitOffset % Byte.SIZE);
			int availableSpace = Byte.SIZE - bitPosition;
			int bitsToWrite = Math.min(numbits, availableSpace);
			int shift = Math.abs(numbits - availableSpace);
			
			if (availableSpace < numbits) {

				in[longOffset] |= val >>> shift;

			} else {

				in[longOffset] |= val << shift;

			}
			
			val &= 0xFFFFFFFF >>> (Integer.SIZE - shift);
			bitOffset += bitsToWrite;
			numbits -= bitsToWrite;
		}
	}

	public int readBinary(byte[] in, long bitOffset, int numbits) {

		int val = 0;

		while (numbits > 0) {

			int longOffset = (int) (bitOffset / Byte.SIZE);
			int bitPosition = (int) (bitOffset % Byte.SIZE);
			int availableSpace = Byte.SIZE - bitPosition;
			int bitsToRead = Math.min(numbits, availableSpace);
			
			val <<= bitsToRead;
			val |= ((0xFF >>> bitPosition) & in[longOffset]) >>> Math.max(0, availableSpace - numbits);
			
			bitOffset += bitsToRead;
			numbits -= bitsToRead;
		}

		return (int) val;
	}

	public void writeUnary(byte[] in, long bitOffset, int val) {
		
		while (val > 0) {

			int longOffset = (int) (bitOffset / Byte.SIZE);
			int bitPosition = (int) (bitOffset % Byte.SIZE);
			int availableSpace = Byte.SIZE - bitPosition;
			
			int numZeros = (int) Math.min(val, availableSpace);
			int zeros = 0xFF >>> numZeros;
			
			in[longOffset] &= (zeros >>> bitPosition) | (zeros << (Byte.SIZE - bitPosition));
			bitOffset += numZeros;
			val -= numZeros;
		}

		int longOffset = (int) (bitOffset / Byte.SIZE);
		int bitPosition = (int) (bitOffset % Byte.SIZE);
		in[longOffset] |= 0x80 >>> bitPosition;
	}

	public int readUnary(byte[] in, long bitOffset) {

		int val = 0;

		while (true) {

			int longOffset = (int) (bitOffset / Byte.SIZE);
			int bitPosition = (int) (bitOffset % Byte.SIZE);

			int x = in[longOffset] & (0xFF >>> bitPosition);
			
			if (x == 0) {

				val += Byte.SIZE - bitPosition;
				bitOffset += Byte.SIZE - bitPosition;
				
				bitPosition = 0;
				
				while (in[longOffset + 1] == 0) {
					
					val += Byte.SIZE; 
					bitOffset += Byte.SIZE;
					longOffset += 1;
				}
				
				x = in[longOffset + 1] & 0xFF;
			}

			return val + (Byte.SIZE - (Integer.SIZE - Integer.numberOfLeadingZeros(x))) - bitPosition;	
		}
	}
}