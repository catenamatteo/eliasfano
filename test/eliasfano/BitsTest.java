package eliasfano;

import org.junit.Test;
import static org.junit.Assert.*;

public class BitsTest {

	@Test
	public void testUnary() {
		
		Bits bits = new Bits();
		byte[] in = new byte[4];
		bits.writeUnary(in, 0, 0);
		bits.writeUnary(in, 1, 1);
		bits.writeUnary(in, 3, 2);
		bits.writeUnary(in, 6, 20);
		assertEquals(0, bits.readUnary(in, 0));
		assertEquals(1, bits.readUnary(in, 1));
		assertEquals(2, bits.readUnary(in, 3));
		assertEquals(20, bits.readUnary(in, 6));
	}
	
	@Test
	public void testBinary() {
		
		Bits bits = new Bits();
		byte[] in = new byte[4];
		bits.writeBinary(in, 0, 0, 0);
		bits.writeBinary(in, 0, 1, 1);
		bits.writeBinary(in, 1, 1, 1);
		bits.writeBinary(in, 2, 2, 2);
		bits.writeBinary(in, 4, 2, 3);
		bits.writeBinary(in, 7, 20, 6);
		assertEquals(0, bits.readBinary(in, 0, 0));
		assertEquals(1, bits.readBinary(in, 0, 1));
		assertEquals(1, bits.readBinary(in, 1, 1));
		assertEquals(2, bits.readBinary(in, 2, 2));
		assertEquals(2, bits.readBinary(in, 4, 3));
		assertEquals(20, bits.readBinary(in, 6, 7));
	}
	
}
