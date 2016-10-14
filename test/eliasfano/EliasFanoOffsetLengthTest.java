package eliasfano;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class EliasFanoOffsetLengthTest {

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                 { 0, in.length }, 
                 { 0, in.length - 1}, 
                 { 1, in.length - 1},
                 { 2, in.length - 2},
                 { 0, 1 }, 
                 { 1, 1 }  
           });
    }
	
	public static int[] in = {0, 1, 2, 3, 4, 5, 6};
	@Parameter(value = 0)
	public int inOffset;
	@Parameter(value = 1)
	public int len;



	@Test
	public void basicTest() {
		
		EliasFanoWriter efw = new EliasFanoWriter();
		byte[] comp = efw.compress(in, inOffset, len);
		EliasFanoReader efr = new EliasFanoReader(comp, in[inOffset + len - 1], len);
		for (int i = 0; i < len; i++) 
			assertEquals(in[i+inOffset], efr.get(i));

		
	}
	
}
