package eliasfano;

public class EliasFanoReader {

	private Bits bits;

	public EliasFanoReader() {
		
		bits = new Bits();
	}
	
	public int decompress(byte[] in, int inOffset, int u, int len, int[] out, int outOffset) {

		int l = (int) Math.max(0, Math.ceil(Math.log10((double)u/(double)len) / Math.log10(2)));
		int lowerBitsOffset = inOffset * Byte.SIZE;
		int higherBitsOffset = lowerBitsOffset + (l * len);
		if (higherBitsOffset % 8 != 0) higherBitsOffset += 8 - (higherBitsOffset % 8); //padding

		int delta = 0;
		for (int i = 0; i < len; i++) {
			
			int low = bits.readBinary(in, lowerBitsOffset, l);
			int high = bits.readUnary(in, higherBitsOffset);
			delta += high;
			out[outOffset + i] = (delta << l) | low;
			lowerBitsOffset += l;
			higherBitsOffset += high + 1;
		}
		
		int size = higherBitsOffset;
		if (higherBitsOffset % 8 != 0) size += 8 - (higherBitsOffset % 8);
		return size / 8;
	}
	
	public int get(byte[] in, int inOffset, int u, int len, int idx) {
		
		int l = (int) Math.max(0, Math.ceil(Math.log10((double)u/(double)len) / Math.log10(2)));
		int lowerBitsOffset = inOffset * Byte.SIZE;
		int higherBitsOffset = lowerBitsOffset + (l * len);
		if (higherBitsOffset % Byte.SIZE != 0) higherBitsOffset += Byte.SIZE - (higherBitsOffset % Byte.SIZE); //padding
		
		int low = bits.readBinary(in, lowerBitsOffset + (l * idx), l);
		
		int higherBytesOffset = higherBitsOffset / 8; //thanks to padding
		int pos = higherBytesOffset;
		int prevSetBits = 0;
		int setBits = 0;
		while (setBits < idx + 1) {
			
			prevSetBits = setBits;
			setBits += bits.bitCount(0xFF & (in[pos]));
			pos++;
		}
		pos--;
		int high = ((pos - higherBytesOffset) * 8) - prevSetBits; //delta
		int readFrom = (pos * 8);
		for (int i = 0; i < (idx + 1) - prevSetBits; i++) {
			
			int read = bits.readUnary(in, readFrom);
			high += read;
			readFrom += read + 1;
			
		}
		
		return (high << l) | low;
	}
	
	public int select(byte[] in, int inOffset, int u, int len, int val) {
		
		int l = (int) Math.max(0, Math.ceil(Math.log10((double)u/(double)len) / Math.log10(2)));
		int higherBitsOffset = (inOffset * Byte.SIZE) + (l * len);
		if (higherBitsOffset % Byte.SIZE != 0) higherBitsOffset += Byte.SIZE - (higherBitsOffset % Byte.SIZE); //padding
		
		int h = val >>> l;
		
		int higherBytesOffset = higherBitsOffset / Byte.SIZE; //thanks to padding
		int prev_1Bits = 0;
		int _0Bits = 0;
		int _1Bits = 0;
		while (_0Bits < h && prev_1Bits < len) {
			
			prev_1Bits = _1Bits;
			if (prev_1Bits >= len) break;
			int pc = bits.bitCount(0xFF & (in[higherBytesOffset++]));
			_1Bits += pc;
			_0Bits += Byte.SIZE - pc;
		}
		
		for (int i = prev_1Bits; i < len; i++)
			if (get(in, inOffset, u, len, i)>=val) return i;
		
		return -1;
		
	}
	
	public int rank(byte[] in, int inOffset, int u, int len, int val) {
		
		int idx = select(in, inOffset, u, len, val);
		
		if (idx == -1) {
		
			return 0;
		
		} else {
			
			int cnt = 1;
			
			for (int i = idx+1; i < len; i++) {
				
				if (get(in, inOffset, u, len, i) == val) {
					
					cnt++;
					
				} else {
					
					break;
				}
				
			}
		
			return cnt;
		}
	}
}
