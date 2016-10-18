package eliasfano;

public class EliasFanoWriter {

	private Bits bits;
	
	public EliasFanoWriter() {
		
		bits = new Bits();
	}
	
	public void compress(int[] in, int inOffset, int len, byte[] out, int outOffset, boolean monotonicityTest) {
		
		if (monotonicityTest)
			for (int i = inOffset+1; i < len; i++)
				if (in[i-1] > in[i]) 
					throw new IllegalArgumentException("int in[] must be monotonically increasing");
		
		int u = in[inOffset + len - 1];
		int l = (int) Math.max(0, Math.ceil(Math.log10((double)u/(double)len) / Math.log10(2.0)));
		
		int bitOffset = outOffset;
		
		for (int i = 0; i < len; i++) {
			
			int low = (0xFFFFFFFF >>> (32 - l)) & in[i+inOffset];
			bits.writeBinary(out, bitOffset, low, l);
			bitOffset += l;			
		}
		
		if (bitOffset % 8 != 0) bitOffset += 8 - (bitOffset % 8); //padding
		
		int prev = 0;
		for (int i = 0; i < len; i++) {
			
			int high = in[i+inOffset] >>> l;
			bits.writeUnary(out, bitOffset, high - prev);
			bitOffset += (high - prev) + 1;
			prev = high;
		}
	}

	public int getSafeCompressedLength(int[] param, int i, int length) {
		
		int u = param[i+length-1];
		int l = (int) Math.max(0, Math.ceil(Math.log10((double)u/(double)length) / Math.log10(2.0)));
		int bitSize = (2 + l) * length;
		if (bitSize % 8 != 0) bitSize += 8 - (bitSize % 8);
		return bitSize / 8;
	}
	
}
