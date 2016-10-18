package eliasfano;

import java.util.Arrays;

import me.lemire.integercompression.IntWrapper;
import me.lemire.integercompression.differential.IntegratedByteIntegerCODEC;

public class EliasFano implements IntegratedByteIntegerCODEC {

	private EliasFanoWriter efw = new EliasFanoWriter();
	
	
	public void compress(int[] arg0, IntWrapper arg1, int arg2, byte[] arg3,
			IntWrapper arg4) {
		
		byte[] comp = efw.compress(arg0, arg1.get(), arg2);
		
		//save max val
		arg3[arg4.get()] = (byte) arg0[arg1.get()+arg2-1];
		arg3[arg4.get()+1] = (byte) (arg0[arg1.get()+arg2-1] >>> 8);
		arg3[arg4.get()+2] = (byte) (arg0[arg1.get()+arg2-1] >>> 16);
		arg3[arg4.get()+3] = (byte) (arg0[arg1.get()+arg2-1] >>> 24);
		arg4.add(4);
		//save len
		arg3[arg4.get()] = (byte) arg2;
		arg3[arg4.get()+1] = (byte) (arg2 >>> 8);
		arg3[arg4.get()+2] = (byte) (arg2 >>> 16);
		arg3[arg4.get()+3] = (byte) (arg2 >>> 24);
		arg4.add(4);

		arg1.add(arg2);
		
		System.arraycopy(comp, 0, arg3, arg4.get(), comp.length);
		arg4.add(comp.length);
	}

	public void uncompress(byte[] arg0, IntWrapper arg1, int arg2, int[] arg3,
			IntWrapper arg4) {

		int u = arg0[arg1.get()] & 0xFF;
		u |= (arg0[arg1.get()+1] & 0xFF) << 8;
		u |= (arg0[arg1.get()+2] & 0xFF) << 16;
		u |= (arg0[arg1.get()+3] & 0xFF) << 24;
		int len = arg0[arg1.get()+4] & 0xFF;
		len |= (arg0[arg1.get()+5] & 0xFF) << 8;
		len |= (arg0[arg1.get()+6] & 0xFF) << 16;
		len |= (arg0[arg1.get()+7] & 0xFF) << 24;
		
		byte[] compCopy = Arrays.copyOfRange(arg0, arg1.get()+8, arg1.get()+arg2);
		EliasFanoReader efr = new EliasFanoReader(compCopy, u, len);
		efr.decompress(0, len, arg3, arg4.get());
		arg1.add(arg2);
		arg4.add(len);

	}

}
