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
	
	public static void main(String argv[]) {
		
		EliasFanoWriter efw = new EliasFanoWriter();
		int[] a = {1, 2, 3, 5, 1000, 1000, 1002};
		byte[] comp = efw.compress(a, 0, a.length);
		EliasFanoReader efr = new EliasFanoReader(comp, 1002, a.length);
		for (int i = 0; i < a.length; i++) System.out.println(efr.get(i));
		System.out.println(efr.select(0));
		System.out.println(efr.select(1001));
		System.out.println(efr.select(2001));
		System.out.println(efr.rank(0));
		System.out.println(efr.rank(1));
		System.out.println(efr.rank(5));
		System.out.println(efr.rank(1000));
		System.out.println(efr.rank(1002));
		System.out.println(efr.rank(2001));

	}
}
