package eu.nicecode.eliasfano;

public class Bits {
	
	public void writeBinary(long[] in, long bitOffset, int val, int numbits) {
		
		long val2 = val;
		
		while (numbits > 0) {

			int longOffset = (int) (bitOffset / Long.SIZE);
			int bitPosition = (int) (bitOffset % Long.SIZE);
			int availableSpace = Long.SIZE - bitPosition;
			int bitsToWrite = Math.min(numbits, availableSpace);
			int shift = Math.abs(numbits - availableSpace);
			
			if (availableSpace < numbits) {

				in[longOffset] |= val2 >>> shift;

			} else {

				in[longOffset] |= val2 << shift;

			}
			
			val2 &= 0xFFFFFFFFFFFFFFFFl >>> (Long.SIZE - shift);
			bitOffset += bitsToWrite;
			numbits -= bitsToWrite;
		}
	}

	public int readBinary(long[] in, long bitOffset, int numbits) {

		long val = 0;

		while (numbits > 0) {

			int longOffset = (int) (bitOffset / Long.SIZE);
			int bitPosition = (int) (bitOffset % Long.SIZE);
			int availableSpace = Long.SIZE - bitPosition;
			int bitsToWrite = Math.min(numbits, availableSpace);
			
			val <<= bitsToWrite;
			val |= ((0xFFFFFFFFFFFFFFFFl >>> bitPosition) & in[longOffset]) >>> Math.max(0, availableSpace - numbits);
			
			bitOffset += bitsToWrite;
			numbits -= bitsToWrite;
		}

		return (int) val;
	}

	public void writeUnary(long[] in, long bitOffset, int val) {
		
		while (val > 0) {

			int longOffset = (int) (bitOffset / Long.SIZE);
			int bitPosition = (int) (bitOffset % Long.SIZE);
			int availableSpace = Long.SIZE - bitPosition;
			
			int numZeros = (int) Math.min(val, availableSpace);
			long zeros = 0xFFFFFFFFFFFFFFFFl >>> numZeros;
			
			in[longOffset] &= (zeros >>> bitPosition) | (zeros << (Long.SIZE - bitPosition));
			bitOffset += numZeros;
			val -= numZeros;
		}

		int longOffset = (int) (bitOffset / Long.SIZE);
		int bitPosition = (int) (bitOffset % Long.SIZE);
		in[longOffset] |= 0x8000000000000000l >>> bitPosition;
	}

	public int readUnary(long[] in, long bitOffset) {

		int val = 0;

		while (true) {

			int longOffset = (int) (bitOffset / Long.SIZE);
			int bitPosition = (int) (bitOffset % Long.SIZE);

			long x = in[longOffset] & (0xFFFFFFFFFFFFFFFFl >>> bitPosition);
			
			if (x == 0) {

				val += Long.SIZE - bitPosition;
				bitOffset += Long.SIZE - bitPosition;

			} else {

				val += Long.numberOfLeadingZeros(x) - bitPosition;
				break;

			}
		}
		
		return val;
	}
}
