package eliasfano;

import java.util.BitSet;

public class EliasFanoWriter {

	public byte[] compress(int[] in, int inOffset, int len) {
		
		for (int i = inOffset+1; i < len; i++)
			if (in[i-1] > in[i]) 
				throw new IllegalArgumentException("int in[] must be monotonically increasing");
		
		int u = in[inOffset + len - 1];
		int l = (int) Math.max(0, Math.ceil(Math.log10((double)u/(double)len) / Math.log10(2.0)));
		
		BitSet bitSet = new BitSet((2 + l) * len);
		int bitIndex = 0;
		
		for (int i = 0; i < len; i++) {
			
			for (int j = 0; j < l; j++) {
				
				int low = (in[i+inOffset] >>> j) & 0x1;
				bitSet.set(bitIndex++, low == 1);
			}
			
		}
		
		int prev = 0;
		for (int i = 0; i < len; i++) {
			
			int high = in[i+inOffset] >>> l;
			for (int j = 0; j < high - prev; j++) 
				bitSet.set(bitIndex++, false);
			bitSet.set(bitIndex++, true);
			prev = high;
		}
		
		return bitSet.toByteArray();
	}
	
}
