package eu.nicecode.eliasfano;

public class EliasFano {

	private Bits bits;
	
	public EliasFano() {
		
		bits = new Bits();
	}
	
	private long roundUp(long val, long den) {
		
		val = val == 0 ? den : val;
		return (val % den == 0) ? val : val + (den - (val % den));
		
	}
	
	public int getL(long u, int length) {
		
		long x = roundUp(u, length)/length;
		return Long.SIZE - Long.numberOfLeadingZeros(x - 1);
	}
	
	public int compress(long[] in, int inOffset, int length, long[] out, int outOffset) {
		
		long u = in[inOffset + length - 1];
		int l = getL(u, length);
		
		long lowBitsOffset = outOffset * Long.SIZE;
		long highBitsOffset = roundUp(lowBitsOffset + (l * length), Long.SIZE);
		
		long prev = 0;
		for (int i = 0; i < length; i++) {
			
			long low = (0xFFFFFFFFFFFFFFFFl >>> (Long.SIZE - l)) & in[i+inOffset];
			bits.writeBinary(out, lowBitsOffset, low, l);
			lowBitsOffset += l;			
			long high = in[i+inOffset] >>> l;
			bits.writeUnary(out, highBitsOffset, high - prev);
			highBitsOffset += (high - prev) + 1;
			prev = high;
		}
		
		return (int) (roundUp(highBitsOffset, Long.SIZE) / Long.SIZE);
	}
	
	public int compress(int[] in, int inOffset, int length, long[] out, int outOffset) {

		int u = in[inOffset + length - 1];
		int l = getL(u, length);
		
		long lowBitsOffset = outOffset * Long.SIZE;
		long highBitsOffset = roundUp(lowBitsOffset + (l * length), Long.SIZE);
		
		int prev = 0;
		for (int i = 0; i < length; i++) {
			
			int low = (0xFFFFFFFF >>> (Integer.SIZE - l)) & in[i+inOffset];
			bits.writeBinary(out, lowBitsOffset, low, l);
			lowBitsOffset += l;			
			int high = in[i+inOffset] >>> l;
			bits.writeUnary(out, highBitsOffset, high - prev);
			highBitsOffset += (high - prev) + 1;
			prev = high;
		}
		
		return (int) (roundUp(highBitsOffset, Long.SIZE) / Long.SIZE);


	}
	
	public int decompress(long[] in, int inOffset, int length, int l, long[] out, int outOffset) {

		long lowBitsOffset = inOffset * Long.SIZE;
		long highBitsOffset = roundUp(lowBitsOffset + (l * length), Long.SIZE);

		long delta = 0;
		for (int i = 0; i < length; i++) {
			
			long low = bits.readBinary(in, lowBitsOffset, l);
			long high = bits.readUnary(in, highBitsOffset);
			delta += high;
			out[outOffset + i] = (delta << l) | low;
			lowBitsOffset += l;
			highBitsOffset += high + 1;
		}
		
		return (int) (roundUp(highBitsOffset, Long.SIZE) / Long.SIZE);
	}
	
	public int decompress(long[] in, int inOffset, int length, int l, int[] out, int outOffset) {

		long lowBitsOffset = inOffset * Long.SIZE;
		long highBitsOffset = roundUp(lowBitsOffset + (l * length), Long.SIZE);

		int delta = 0;
		for (int i = 0; i < length; i++) {
			
			int low = (int) bits.readBinary(in, lowBitsOffset, l);
			int high = (int) bits.readUnary(in, highBitsOffset);
			delta += high;
			out[outOffset + i] = (delta << l) | low;
			lowBitsOffset += l;
			highBitsOffset += high + 1;
		}
		
		return (int) (roundUp(highBitsOffset, Long.SIZE) / Long.SIZE);
	}
	
	public long get(long[] in, int inOffset, int length, int l, int idx) {
		
		long lowBitsOffset = inOffset * Long.SIZE;
		long highBitsOffset = roundUp(lowBitsOffset + (l * length), Long.SIZE);
		
		long low = bits.readBinary(in, lowBitsOffset + (l * idx), l);
		
		int startOffset = (int) (highBitsOffset / Long.SIZE);
		int offset = startOffset;
		int prevSetBits = 0;
		int setBits = 0;
		while (setBits < idx + 1) {
			
			prevSetBits = setBits;
			setBits += Long.bitCount(in[offset++]);
		}
		offset--; //rollback
		long high = ((offset - startOffset) * Long.SIZE) - prevSetBits; //delta
		int readFrom = offset * Long.SIZE;
		for (int i = 0; i < (idx + 1) - prevSetBits; i++) {
			
			long read = bits.readUnary(in, readFrom);
			high += read;
			readFrom += read + 1;

		}
		
		return (high << l) | low;
	}
	
	public int select(long[] in, int inOffset, int length, int l, long val) {
		
		long lowBitsOffset = inOffset * Long.SIZE;
		long highBitsOffset = roundUp(lowBitsOffset + (l * length), Long.SIZE);
		
		long h = val >>> l;
		
		int offset = (int) (highBitsOffset / Long.SIZE);
		int prev1Bits = 0;
		int _0Bits = 0;
		int _1Bits = 0;
		while (_0Bits < h && prev1Bits < length) {
			
			prev1Bits = _1Bits;
			int bitCount = Long.bitCount(in[offset++]);
			_1Bits += bitCount;
			_0Bits += Long.SIZE - bitCount;
		}
		
		for (int i = prev1Bits; i < length; i++)
			if (get(in, inOffset, length, l, i)>=val) return i;
		
		return -1;
		
	}
	
	public int rank(long[] in, int inOffset, int length, int l, long val) {
		
		int idx = select(in, inOffset, length, l, val);
		
		if (idx == -1) {
		
			return 0;
		
		} else {
			
			int cnt = 1;
			
			for (int i = idx+1; i < length; i++) {
				
				if (get(in, inOffset, length, l, i) == val) {
					
					cnt++;
					
				} else {
					
					break;
				}
				
			}
		
			return cnt;
		}
	}

	public int getCompressedSize(long u, int length) {
		
		int l = getL(u, length);
		long numLowBits = roundUp(l * length, Long.SIZE);
		long numHighBits = roundUp(2 * length, Long.SIZE);
		return (int) ((numLowBits + numHighBits) / Long.SIZE);
	}
	
}
