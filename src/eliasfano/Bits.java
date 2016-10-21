package eliasfano;

public class Bits {

	private byte[] MSB = { 8, 7, 6, 6, 5, 5, 5, 5, 4, 4, 4, 4, 4, 4, 4, 4, 3, 3, 3, 3, 3, 3, 3, 3,
			3, 3, 3, 3, 3, 3, 3, 3, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
			2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
			1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

	private byte[] BIT_COUNT = { 0, 1, 1, 2, 1, 2, 2, 3, 1, 2, 2, 3, 2, 3, 3, 4, 1, 2, 2, 3, 2, 3, 3, 4, 2, 3,
			3, 4, 3, 4, 4, 5, 1, 2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5, 2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5,
			5, 6, 1, 2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5, 2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 2, 3,
			3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7, 1, 2, 2, 3, 2, 3,
			3, 4, 2, 3, 3, 4, 3, 4, 4, 5, 2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 2, 3, 3, 4, 3, 4, 4, 5, 3, 4,
			4, 5, 4, 5, 5, 6, 3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7, 2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5,
			5, 6, 3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7, 3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7, 4, 5,
			5, 6, 5, 6, 6, 7, 5, 6, 6, 7, 6, 7, 7, 8 };

	public void writeBinary(byte[] in, int bitOffset, int val, int numbits) {

		val &= 0xFFFFFFFF >>> (32 - numbits);
		
		while (numbits > 0) {

			int byteOffset = bitOffset / 8;
			int bitPos = bitOffset % 8;
			int availableSpace = 8 - bitPos;

			if (availableSpace < numbits) {

				in[byteOffset] |= (byte) (val >>> (numbits - availableSpace));
				val &= 0xFFFFFFFF >>> (32 - (numbits - availableSpace));
				bitOffset += availableSpace;
				numbits -= availableSpace;

			} else {

				in[byteOffset] |= (byte) (val << (availableSpace - numbits));
				val = 0;
				bitOffset += numbits;
				numbits = 0;
			}
		}
	}

	public int readBinary(byte[] in, int bitOffset, int numbits) {

		int val = 0;

		while (numbits > 0) {

			int byteOffset = bitOffset / 8;
			int bitPos = bitOffset % 8;
			int availableSpace = 8 - bitPos;

			if (availableSpace < numbits) {

				val <<= availableSpace;
				val |= ((0xFF >>> bitPos) & in[byteOffset]);
				bitOffset += availableSpace;
				numbits -= availableSpace;

			} else {

				val <<= numbits;
				val |= ((0xFF >>> bitPos) & in[byteOffset]) >>> (availableSpace - numbits);
				bitOffset += numbits;
				numbits = 0;
			}

		}

		return val;
	}

	public void writeUnary(byte[] in, int bitOffset, int val) {

		while (val > 0) {

			int byteOffset = bitOffset / Byte.SIZE;
			int bitPosition = bitOffset % Byte.SIZE;
			int availableSpace = Byte.SIZE - bitPosition;
			
			int numZeros = Math.min(val, availableSpace);
			int zeros = 0xFF >>> numZeros;
			
			in[byteOffset] &= (zeros >>> bitPosition) | (zeros << (Byte.SIZE - bitPosition));
			bitOffset += numZeros;
			val -= numZeros;
		}

		int byteOffset = bitOffset / Byte.SIZE;
		int bitPosition = bitOffset % Byte.SIZE;
		in[byteOffset] |= 0b10000000 >>> bitPosition;
	}

	public int readUnary(byte[] in, int bitOffset) {

		boolean done = false;
		int val = 0;

		while (!done) {

			int byteOffset = bitOffset / Byte.SIZE;
			int bitPos = bitOffset % Byte.SIZE;
			int inc = 0;

			int x = in[byteOffset] & (0xFF >>> bitPos);
			
			if (x == 0) {

				inc = Byte.SIZE - bitPos;

			} else {

				inc = MSB[x] - bitPos;
				done = true;

			}

			val += inc;
			bitOffset += inc;
		}
		
		return val;
	}

	public int bitCount(int val) {

		return BIT_COUNT[val];
	}
}
