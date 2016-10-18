//package eliasfano;
//
//import static org.junit.Assert.*;
//
//import java.util.Arrays;
//import java.util.Collection;
//
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.junit.runners.Parameterized;
//import org.junit.runners.Parameterized.Parameter;
//import org.junit.runners.Parameterized.Parameters;
//
//@RunWith(Parameterized.class)
//public class OffsetAndLengthTest {
//
//    @Parameters
//    public static Collection<Object[]> data() {
//        return Arrays.asList(new Object[][] {
//                 { 0, in.length }, 
//                 { 0, in.length - 1}, 
//                 { 1, in.length - 1},
//                 { 2, in.length - 2},
//                 { 0, 1 }, 
//                 { 1, 1 }  
//           });
//    }
//	
//	public static int[] in = {0, 1, 2, 3, 4, 5, 6};
//	@Parameter(value = 0)
//	public int offset;
//	@Parameter(value = 1)
//	public int len;
//
//
//
//	@Test
//	public void getTest() {
//		
//		EliasFanoWriter efw = new EliasFanoWriter();
//		byte[] comp = efw.compress(in, offset, len);
//		EliasFanoReader efr = new EliasFanoReader(comp, in[offset + len - 1], len);
//		for (int i = 0; i < len; i++) 
//			assertEquals(in[i+offset], efr.get(i));
//
//	}
//	
//	/*
//	 * Decompress using offset and length on the original array
//	 */
//	@Test
//	public void decompressTest1() {
//		
//		EliasFanoWriter efw = new EliasFanoWriter();
//		byte[] comp = efw.compress(in, offset, len);
//		EliasFanoReader efr = new EliasFanoReader(comp, in[offset + len - 1], len);
//		int[] b = new int[len];
//		efr.decompress(0, len, b, 0);
//		assertArrayEquals(Arrays.copyOfRange(in, offset, offset+len), b);
//
//	}
//
//	/*
//	 * Decompress using offset and length on the compressed array
//	 */
//	@Test
//	public void decompressTest2() {
//		
//		EliasFanoWriter efw = new EliasFanoWriter();
//		byte[] comp = efw.compress(in, 0, in.length);
//		EliasFanoReader efr = new EliasFanoReader(comp, in[in.length-1], in.length);
//		int[] b = new int[len];
//		efr.decompress(offset, len, b, 0);
//		assertArrayEquals(Arrays.copyOfRange(in, offset, offset+len), b);
//
//	}
//}
