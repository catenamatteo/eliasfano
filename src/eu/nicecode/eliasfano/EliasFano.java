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

/**
 * A simpl(istic) implementation of the EliasFano compression technique. This
 * class compresses array of MONOTONICALLY INCREASING integers into (smaller)
 * arrays of bytes. It permits to uncompress an arbitrary element for the
 * compressed data, without decompressing the whole array. Similarly, it permits
 * to find the index of the first element in the compressed data, greater or
 * equal to a given value, without decompressing the whole array.
 * 
 * @author Matteo Catena
 *
 */
public final class EliasFano {

	private EliasFano() {
		
	}
	
	static long roundUp(long val, final long den) {

		val = val == 0 ? den : val;
		return (val % den == 0) ? val : val + (den - (val % den));

	}

	/**
	 * Returns the number of lower bits required to encode each element in an
	 * array of size {@code length} with maximum value {@code u}.
	 * 
	 * @param u
	 *            the max value in the array
	 * @param length
	 *            the length of the array
	 * @return the number of lower bits
	 */
	public static int getL(final int u, final int length) {

		long x = roundUp(u, length) / length;
		return Integer.SIZE - Integer.numberOfLeadingZeros((int) (x - 1));
	}

	/**
	 * Compresses {@code length} elements of {@code in}, from {@code inOffset},
	 * into the {@code out} array, from {@code outOffset}
	 * 
	 * @param in
	 *            the array to compress (MONOTONICALLY INCREASING)
	 * @param inOffset
	 *            starting offset
	 * @param length
	 *            number of elements to compress
	 * @return the compress values
	 */
	public static byte[] compress(final int[] in, final int inOffset, final int length) {

		final int u = in[inOffset + length - 1];
		final int l = getL(u, length);

		final byte[] out = new byte[getCompressedSize(u, length)];
		long lowBitsOffset = 0;
		long highBitsOffset = roundUp(l * length, Byte.SIZE);

		int prev = 0;
		for (int i = 0; i < length; i++) {

			int low = Bits.VAL_TO_WRITE[l] & in[i + inOffset];
			Bits.writeBinary(out, lowBitsOffset, low, l);
			lowBitsOffset += l;
			int high = in[i + inOffset] >>> l;
			Bits.writeUnary(out, highBitsOffset, high - prev);
			highBitsOffset += (high - prev) + 1;
			prev = high;
		}

		return out;

	}

	/**
	 * Decompress {@code length} elements from {@code in}, starting at
	 * {@code inOffset}, into {@code out}, starting from {@outOffset}. 
	 * Each element is encoded using {@code l} lower bits.
	 * 
	 * @param in
	 *            the compressed array
	 * @param inOffset
	 *            starting offset
	 * @param length
	 *            number of elements to decompress
	 * @param l
	 *            number of lower bits for each element
	 * @param out
	 *            the uncompressed array
	 * @param outOffset
	 *            starting offset
	 * @return the number of read bytes
	 */
	public static int decompress(final byte[] in, final int inOffset, final int length, final int l,
			final int[] out, final int outOffset) {

		long lowBitsOffset = inOffset * Byte.SIZE;
		long highBitsOffset = roundUp(lowBitsOffset + (l * length), Byte.SIZE);

		int delta = 0;
		for (int i = 0; i < length; i++) {

			final int low = Bits.readBinary(in, lowBitsOffset, l);
			final int high = Bits.readUnary(in, highBitsOffset);
			delta += high;
			out[outOffset + i] = (delta << l) | low;
			lowBitsOffset += l;
			highBitsOffset += high + 1;
		}

		return (int) (roundUp(highBitsOffset, Byte.SIZE) / Byte.SIZE);
	}

	/**
	 * Decompresses the idx-th element from the compressed array {@code in},
	 * starting from {@code inOffset}. The uncompressed array has size
	 * {@code length} and its elements are encoded using {@code l} lower bits
	 * each.
	 * 
	 * @param in
	 *            the compressed array
	 * @param inOffset
	 *            starting offset
	 * @param length
	 *            the size of the uncompressed array
	 * @param l
	 *            number of lower bits
	 * @param idx
	 *            the index of the element to decompress
	 * @return the value of the idx-th element
	 */
	public static int get(final byte[] in, final int inOffset, final int length, final int l, final int idx) {

		final long lowBitsOffset = inOffset * Byte.SIZE;
		final long highBitsOffset = roundUp(lowBitsOffset + (l * length), Byte.SIZE);

		final int low = Bits.readBinary(in, lowBitsOffset + (l * idx), l);

		final int startOffset = (int) (highBitsOffset / Byte.SIZE);
		int offset = startOffset;
		int prev1Bits = 0;
		int _1Bits = 0;
		while (_1Bits < idx + 1) {

			prev1Bits = _1Bits;
			_1Bits += Integer.bitCount(in[offset++] & 0xFF);
		}
		offset--; // rollback
		int delta = ((offset - startOffset) * Byte.SIZE) - prev1Bits; // delta
		int readFrom = offset * Byte.SIZE;
		for (int i = 0; i < (idx + 1) - prev1Bits; i++) {

			int high = Bits.readUnary(in, readFrom);
			delta += high;
			readFrom += high + 1;

		}

		return (delta << l) | low;
	}

	/**
	 * Returns the index of the first element equal or greater than {@code val}
	 * from the compressed array {@code in}, starting from {@code inOffset}. The
	 * uncompressed array has size {@code length} and its elements are encoded
	 * using {@code l} lower bits each.
	 * 
	 * @param in
	 *            the compressed array
	 * @param inOffset
	 *            starting offset
	 * @param length
	 *            size of the uncompressed array
	 * @param l
	 *            number of lower bits
	 * @param val
	 *            value to select
	 * @return the index of the first element equal or greater than {@code val}
	 */
	public static int select(final byte[] in, final int inOffset, final int length, final int l, final int val) {

		final long lowBitsOffset = inOffset * Byte.SIZE;
		final long highBitsOffset = roundUp(lowBitsOffset + (l * length), Byte.SIZE);

		final int h = val >>> l;

		final int startOffset = (int) (highBitsOffset / Byte.SIZE);
		int offset = startOffset;
		int prev1Bits = 0;
		int _0Bits = 0;
		int _1Bits = 0;
		while (_0Bits < h && _1Bits < length) {

			prev1Bits = _1Bits;
			int bitCount = Integer.bitCount(in[offset++] & 0xFF);
			_1Bits += bitCount;
			_0Bits += Byte.SIZE - bitCount;
		}
		
		offset = Math.max(offset - 1, startOffset); //conditional rollback

		int low = Bits.readBinary(in, lowBitsOffset + (l * prev1Bits), l);
		int delta = ((offset - startOffset) * Byte.SIZE) - prev1Bits; // delta
		int readFrom = offset * Byte.SIZE;
		int high = Bits.readUnary(in, readFrom);
		delta += high;
		readFrom += high + 1;

		if (((delta << l) | low) >= val) {
			
			return prev1Bits;
		
		} else {
		
			for (int i = prev1Bits + 1; i < length; i++) {
		
				low = Bits.readBinary(in, lowBitsOffset + (l * i), l);
				high = Bits.readUnary(in, readFrom);
				delta += high;
				readFrom += high + 1;
		
				if (((delta << l) | low) >= val) return i;
		
			}
		}
		
		return -1 ;


	}


	/**
	 * Returns the number of time {@code val} occurs in the compressed array
	 * {@code in}, starting from {@code inOffset}. The uncompressed array has
	 * size {@code length} and its elements are encoded using {@code l} lower
	 * bits each.
	 * 
	 * @param in
	 *            the compressed array
	 * @param inOffset
	 *            starting offset
	 * @param length
	 *            size of the uncompressed array
	 * @param l
	 *            number of lower bits
	 * @param val
	 *            value to rank
	 * @return number of occurences of {@code val}
	 */
	public static int rank(final byte[] in, final int inOffset, final int length, final int l, final int val) {

		/* TODO:
		 * this implementation is particularly inefficient and it should be rewritten
		 */
		
		final int idx = select(in, inOffset, length, l, val);

		if (idx == -1) {

			return 0;

		} else {

			int cnt = 1;

			for (int i = idx + 1; i < length; i++) {

				if (get(in, inOffset, length, l, i) == val) {

					cnt++;

				} else {

					break;
				}

			}

			return cnt;
		}
	}

	/**
	 * Returns the number of bytes required to compress an array of size
	 * {@code length} and maximum value {@code u}.
	 * 
	 * @param u
	 *            the maximum value in the array to compress
	 * @param length
	 *            the size of the array to compress
	 * @return the number of required bytes
	 */
	public static int getCompressedSize(final int u, final int length) {

		final int l = getL(u, length);
		final long numLowBits = roundUp(l * length, Byte.SIZE);
		final long numHighBits = roundUp(2 * length, Byte.SIZE);
		return (int) ((numLowBits + numHighBits) / Byte.SIZE);
	}

}
