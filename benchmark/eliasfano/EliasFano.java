package eliasfano;

import java.util.Arrays;

import me.lemire.integercompression.IntWrapper;
import me.lemire.integercompression.differential.IntegratedByteIntegerCODEC;

public class EliasFano implements IntegratedByteIntegerCODEC {

	private Bits bits = new Bits();
	private EliasFanoWriter efw = new EliasFanoWriter();
	private EliasFanoReader efr = new EliasFanoReader();
	
	public void compress(int[] arg0, IntWrapper arg1, int arg2, byte[] arg3,
			IntWrapper arg4) {
		 
		Arrays.fill(arg3, arg4.get(), efw.getSafeCompressedLength(arg0, arg1.get(), arg2)+8, (byte) 0);
		//save max val
		bits.writeBinary(arg3, arg4.get()*Byte.SIZE, arg0[arg1.get()+arg2-1], Integer.SIZE);
		//System.err.println(arg0[arg1.get()+arg2-1]+ " vs "+bits.readBinary(arg3, arg4.get()*Byte.SIZE, Integer.SIZE));
		arg4.add(4);
		//save len
		bits.writeBinary(arg3, arg4.get()*Byte.SIZE, arg2, Integer.SIZE);
		//System.err.println(arg2 +" vs "+bits.readBinary(arg3, arg4.get()*Byte.SIZE, Integer.SIZE));
		arg4.add(4);
		int size = efw.compress(arg0, arg1.get(), arg2, arg3, arg4.get(), false);
		arg1.add(arg2);
		arg4.add(size);
	}

	public void uncompress(byte[] arg0, IntWrapper arg1, int arg2, int[] arg3,
			IntWrapper arg4) {

		//System.err.println(arg1.get());
		int u = bits.readBinary(arg0, arg1.get()*Byte.SIZE, Integer.SIZE);
		//System.err.println(u);
		arg1.add(4);
		int len = bits.readBinary(arg0, arg1.get()*Byte.SIZE, Integer.SIZE);
		arg1.add(4);
		//System.err.println(len);
		
		int read = efr.decompress(arg0, arg1.get(), u, len, arg3, arg4.get());
		arg1.add(read);
		arg4.add(len);

	}

}
