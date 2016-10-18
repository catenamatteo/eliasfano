package eliasfano;

public class Bits {

	private static byte[][] N_0s_FROM = { { (byte) 0b11111111 },
			{ (byte) 0b01111111, (byte) 0b10111111, (byte) 0b11011111, (byte) 0b11101111, (byte) 0b11110111,
					(byte) 0b11111011, (byte) 0b11111101, (byte) 0b11111110 },
			{ (byte) 0b00111111, (byte) 0b10011111, (byte) 0b11001111, (byte) 0b11100111, (byte) 0b11110011,
					(byte) 0b11111001, (byte) 0b11111100 },
			{ (byte) 0b00011111, (byte) 0b10001111, (byte) 0b11000111, (byte) 0b11100011, (byte) 0b11110001,
					(byte) 0b11111000 },
			{ (byte) 0b00001111, (byte) 0b10000111, (byte) 0b11000011, (byte) 0b11100001, (byte) 0b11110000 },
			{ (byte) 0b00000111, (byte) 0b10000011, (byte) 0b11000001, (byte) 0b11100000 },
			{ (byte) 0b00000011, (byte) 0b10000001, (byte) 0b11000000 }, { (byte) 0b00000001, (byte) 0b10000000 },
			{ (byte) 0b00000000 } };

	private static byte[] A_1_IN = { (byte) 0b10000000, (byte) 0b01000000, (byte) 0b00100000, (byte) 0b00010000,
			(byte) 0b00001000, (byte) 0b00000100, (byte) 0b00000010, (byte) 0b00000001, };

	private static byte[] LEFTMOST_1_POS_OF = { 8, 7, 6, 6, 5, 5, 5, 5, 4, 4, 4, 4, 4, 4, 4, 4, 3, 3, 3, 3, 3, 3, 3, 3,
			3, 3, 3, 3, 3, 3, 3, 3, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
			2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
			1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

	private static byte[] BIT_COUNT_OF = { 0, 1, 1, 2, 1, 2, 2, 3, 1, 2, 2, 3, 2, 3, 3, 4, 1, 2, 2, 3, 2, 3, 3, 4, 2, 3,
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

			int byteOffset = bitOffset / 8;
			int bitPos = bitOffset % 8;
			int availableSpace = 8 - bitPos;

			if (availableSpace >= val) {

				in[byteOffset] &= 0xFF & N_0s_FROM[val][bitPos];
				bitOffset += val;
				val = 0;

			} else {

				in[byteOffset] &= 0xFF & N_0s_FROM[availableSpace][bitPos];
				bitOffset += availableSpace;
				val -= availableSpace;
			}

		}

		int byteOffset = bitOffset / 8;
		int bitPos = bitOffset % 8;
		in[byteOffset] |= A_1_IN[bitPos];
	}

	public int readUnary(byte[] in, int bitOffset) {

		int val = 0;

		while (true) {

			int byteOffset = bitOffset / 8;
			int bitPos = bitOffset % 8;

			int x = in[byteOffset] & (0xFF & N_0s_FROM[bitPos][0]);

			if (x == 0) {

				int inc = 8 - bitPos;
				val += inc;
				bitOffset += inc;

			} else {

				int l1p = LEFTMOST_1_POS_OF[x];
				val += l1p - bitPos;
				return val;

			}

		}

	}

	public int bitCount(int val) {

		return BIT_COUNT_OF[val];
	}
}
