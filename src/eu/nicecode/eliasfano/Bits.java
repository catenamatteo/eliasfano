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
 * A support class for unaligned binary writing/reading and unary
 * writing/reading
 * 
 * @author Matteo Catena
 *
 */
public final class Bits {

	private Bits() {

	}

	static final int[] VAL_TO_WRITE;
	private static final int[] ZEROS;
	private static final int[] ONE;

	static {

		VAL_TO_WRITE = new int[Integer.SIZE + 1];
		for (int i = 0; i <= Integer.SIZE; i++)
			VAL_TO_WRITE[i] = 0xFFFFFFFF >>> (Integer.SIZE - i);

		ZEROS = new int[] { 0b11111111, 0b01111111, 0b00111111, 0b00011111, 0b00001111, 0b00000111, 0b00000011,
				0b00000001, 0b00000000 };

		ONE = new int[] { 0b10000000, 0b01000000, 0b00100000, 0b00010000, 0b00001000, 0b00000100, 0b00000010,
				0b00000001 };
	}

	public static void writeBinary(final byte[] in, long bitOffset, int val, int numbits) {

		while (numbits > 0) {

			final int longOffset = (int) (bitOffset / Byte.SIZE);
			final int bitPosition = (int) (bitOffset % Byte.SIZE);
			final int availableSpace = Byte.SIZE - bitPosition;
			final int bitsToWrite = Math.min(numbits, availableSpace);
			final int shift = numbits - availableSpace;

			if (availableSpace < numbits) {

				in[longOffset] |= val >>> shift;
				val &= VAL_TO_WRITE[shift];

			} else {

				in[longOffset] |= val << -shift;
				val &= VAL_TO_WRITE[-shift];
			}

			bitOffset += bitsToWrite;
			numbits -= bitsToWrite;
		}
	}

	public static int readBinary(final byte[] in, long bitOffset, int numbits) {

		int val = 0;

		while (numbits > 0) {

			final int longOffset = (int) (bitOffset / Byte.SIZE);
			final int bitPosition = (int) (bitOffset % Byte.SIZE);
			final int availableSpace = Byte.SIZE - bitPosition;
			final int bitsToRead = Math.min(numbits, availableSpace);

			val <<= bitsToRead;
			int read = ZEROS[bitPosition] & in[longOffset];
			final int shift = availableSpace - numbits;
			if (shift > 0) {
				read >>>= shift;
			}
			val |= read;

			bitOffset += bitsToRead;
			numbits -= bitsToRead;
		}

		return (int) val;
	}

	public static void writeUnary(final byte[] in, long bitOffset, int val) {

		while (val > 0) {

			final int longOffset = (int) (bitOffset / Byte.SIZE);
			final int bitPosition = (int) (bitOffset % Byte.SIZE);
			final int availableSpace = Byte.SIZE - bitPosition;

			final int numZeros = (int) Math.min(val, availableSpace);
			final int zeros = ZEROS[numZeros];

			in[longOffset] &= (zeros >>> bitPosition) | (zeros << (Byte.SIZE - bitPosition));
			bitOffset += numZeros;
			val -= numZeros;
		}

		final int longOffset = (int) (bitOffset / Byte.SIZE);
		final int bitPosition = (int) (bitOffset % Byte.SIZE);
		in[longOffset] |= ONE[bitPosition];
	}

	public static int readUnary(final byte[] in, long bitOffset) {

		int val = 0;

		int longOffset = (int) (bitOffset / Byte.SIZE);
		final int bitPosition = (int) (bitOffset % Byte.SIZE);

		int x = in[longOffset] & ZEROS[bitPosition];

		if (x != 0) {

			val -= bitPosition;

		} else {

			val += Byte.SIZE - bitPosition;

			while (in[longOffset + 1] == 0) {

				val += Byte.SIZE;
				longOffset++;
			}

			x = in[longOffset + 1] & 0xFF;
		}
		
		return val + (Byte.SIZE - (Integer.SIZE - Integer.numberOfLeadingZeros(x)));
	}
}