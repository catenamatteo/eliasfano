package eliasfano;

import java.util.BitSet;

public class EliasFanoReader {

	private BitSet bitSet;
	private int higherOffset;
	private int l;
	private int len;

	public EliasFanoReader(byte[] in, int u, int len) {
		
		this.bitSet = BitSet.valueOf(in);
		l = (int) Math.max(0, Math.ceil(Math.log10((double)u/(double)len) / Math.log10(2)));
		higherOffset = l * len;
		this.len = len;
	}
	
	public void decompress(int inOffset, int len, int[] out, int outOffset) {
		
		int prevPos = -1;
		int high = 0;
		for (int j = 0; j < inOffset; j++) {
			
			int pos = bitSet.nextSetBit(higherOffset + prevPos + 1) - higherOffset;
			high += pos - prevPos - 1;
			prevPos = pos;
		}
		
		for (int j = 0; j < len; j++) {
			
			int low = 0;
			for (int i = 0; i < l; i++) {
				
				boolean bit = bitSet.get(((inOffset + j) * l) + i);
				if (bit) low |= (1 << i);
			}
			
			int pos = bitSet.nextSetBit(higherOffset + prevPos + 1) - higherOffset;
			high += pos - prevPos - 1;
			prevPos = pos;
			
			out[outOffset + j] = (high << l) | low;
		}
	}
	
	public int get(int idx) {
		
		int low = 0;
		for (int i = 0; i < l; i++) {
			
			boolean bit = bitSet.get((idx * l) + i);
			if (bit) low |= (1 << i);
		}
		
		int prevPos = -1;
		int high = 0;
		for (int i = 0; i < idx + 1; i++) {
			
			int pos = bitSet.nextSetBit(higherOffset + prevPos + 1) - higherOffset;
			high += pos - prevPos - 1;
			prevPos = pos;
		}
		return (high << l) | low;
	}
	
	public int select(int x) {
		
		int h = x >>> l;

		int idx = 0;
		int prevPos = -1;
		int high = 0;
		for (; idx < len + 1; idx++) {
			
			int pos = bitSet.nextSetBit(higherOffset + prevPos + 1) - higherOffset;
			high += pos - prevPos - 1;
			prevPos = pos;
			if (high >= h) break;
		}
		
		for (int i = idx; i < len; i++) {
			
			if (get(i)>=x) return i;
			
		}
		
		return -1;
	}
	
	public int rank(int x) {
		
		int h = x >>> l;

		int idx = 0;
		int prevPos = -1;
		int high = 0;
		for (; idx < len + 1; idx++) {
			
			int pos = bitSet.nextSetBit(higherOffset + prevPos + 1) - higherOffset;
			high += pos - prevPos - 1;
			prevPos = pos;
			if (high >= h) break;
		}
		
		int cnt = 0;
		for (int i = idx; i < len; i++) {
			
			if (get(i)==x) cnt++;
			
		}
		
		return cnt;
	}
}
