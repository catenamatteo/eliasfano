package eliasfano;

public class EliasFanoReader {

	private Bits bits;

	public EliasFanoReader() {
		
		bits = new Bits();
	}


//	public EliasFanoReader(byte[] in, int inOffset, int u, int len) {
//		
//		l = (int) Math.max(0, Math.ceil(Math.log10((double)u/(double)len) / Math.log10(2)));
//		lowerOffset = inOffset;
//		higherOffset = l * len;
//		this.len = len;
//	}
//	
//	public void decompress(byte[] in, int inOffset, int u, int len, int[] out, int outOffset, int numval) {
//
//		int l = (int) Math.max(0, Math.ceil(Math.log10((double)u/(double)len) / Math.log10(2)));
//		int lowerOffset = inOffset;
//		int higherOffset = inOffset + (l * len);
//
//		int prevPos = -1;
//		int high = 0;
//		for (int j = 0; j < inOffset; j++) {
//			
//			int pos = bitSet.nextSetBit(higherOffset + prevPos + 1) - higherOffset;
//			high += pos - prevPos - 1;
//			prevPos = pos;
//		}
//		
//		for (int j = 0; j < len; j++) {
//			
//			int low = 0;
//			for (int i = 0; i < l; i++) {
//				
//				boolean bit = bitSet.get(((inOffset + j) * l) + i);
//				if (bit) low |= (1 << i);
//			}
//			
//			int pos = bitSet.nextSetBit(higherOffset + prevPos + 1) - higherOffset;
//			high += pos - prevPos - 1;
//			prevPos = pos;
//			
//			out[outOffset + j] = (high << l) | low;
//		}
//	}
	
	public int decompress(byte[] in, int inOffset, int u, int len, int[] out, int outOffset) {

		int l = (int) Math.max(0, Math.ceil(Math.log10((double)u/(double)len) / Math.log10(2)));
		int lowerBitsOffset = inOffset;
		int higherBitsOffset = inOffset + (l * len);
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
		int lowerBitsOffset = inOffset;
		int higherBitsOffset = inOffset + (l * len);
		if (higherBitsOffset % 8 != 0) higherBitsOffset += 8 - (higherBitsOffset % 8); //padding
		
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
	
//	public int select(int x) {
//		
//		int h = x >>> l;
//
//		int idx = 0;
//		int prevPos = -1;
//		int high = 0;
//		for (; idx < len + 1; idx++) {
//			
//			int pos = bitSet.nextSetBit(higherOffset + prevPos + 1) - higherOffset;
//			high += pos - prevPos - 1;
//			prevPos = pos;
//			if (high >= h) break;
//		}
//		
//		for (int i = idx; i < len; i++) {
//			
//			if (get(i)>=x) return i;
//			
//		}
//		
//		return -1;
//	}
//	
//	public int rank(int x) {
//		
//		int h = x >>> l;
//
//		int idx = 0;
//		int prevPos = -1;
//		int high = 0;
//		for (; idx < len + 1; idx++) {
//			
//			int pos = bitSet.nextSetBit(higherOffset + prevPos + 1) - higherOffset;
//			high += pos - prevPos - 1;
//			prevPos = pos;
//			if (high >= h) break;
//		}
//		
//		int cnt = 0;
//		for (int i = idx; i < len; i++) {
//			
//			if (get(i)==x) cnt++;
//			
//		}
//		
//		return cnt;
//	}
}
