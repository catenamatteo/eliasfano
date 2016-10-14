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
public class EliasFanoGetTest {

	@Parameters
    public static Collection<int[]> data() {
        return Arrays.asList(new int[][] {
        	{0},
        	{1},
        	{Byte.MAX_VALUE},
        	{Short.MAX_VALUE},
        	{Integer.MAX_VALUE},
        	{0,1,2,3,4},
        	{0,Byte.MAX_VALUE,Short.MAX_VALUE,Integer.MAX_VALUE},
        	{0,1,Byte.MAX_VALUE-1,Byte.MAX_VALUE,Byte.MAX_VALUE+1,Short.MAX_VALUE-1,Short.MAX_VALUE,Short.MAX_VALUE+1,Integer.MAX_VALUE-1,Integer.MAX_VALUE},
        	{0, 0 , 0, Byte.MAX_VALUE, Byte.MAX_VALUE, Byte.MAX_VALUE}
           });
    }
    
    @Parameter
    public int[] param;
	
	@Test
	public void basicTest() {
		
		EliasFanoWriter efw = new EliasFanoWriter();
		byte[] comp = efw.compress(param, 0, param.length);
		EliasFanoReader efr = new EliasFanoReader(comp, param[param.length-1], param.length);
		for (int i = 0; i < param.length; i++) assertEquals(param[i], efr.get(i));
	}

}
