package eu.nicecode.eliasfano;

import java.util.Iterator;

/**
 * This class reads an array compressed with {@link eu.nicecode.eliasfano.EliasFano} in a stream fashion.
 * 
 * @author Matteo Catena
 *
 */
public class EliasFanoIterator implements Iterator<Integer> {

	private int idx = 0;
	private int curr = -1;
	
	private long inOffset;
	private long highBitsOffset;
	private long lowBitsOffset;
	
	private Bits bits;
	
	private byte[] in;
	private int size;
	private int l;
	private int delta = 0;
	
	/**
	 * 
	 * @param in the data compressed using {@link eu.nicecode.eliasfano.EliasFano}
	 * @param inOffset {@code in}'s offset
	 * @param size the number of elements in the uncompressed data
	 * @param l the number of lower bits used to compress the data
	 */
	public EliasFanoIterator(byte[] in, int inOffset, int size, int l) {
		
		this.in = in;
		
		this.size = size;
		
		this.l = l;
		
		this.inOffset = inOffset;
		lowBitsOffset = inOffset * Byte.SIZE;
		highBitsOffset = roundUp(lowBitsOffset + (l * size), Byte.SIZE);

		bits = new Bits();
	}
	
	/*
	 * TODO: factorize this method
	 */
	private long roundUp(long val, long den) {

		val = val == 0 ? den : val;
		return (val % den == 0) ? val : val + (den - (val % den));

	}
	
	
	public boolean hasNext() {
		
		return idx < size;
	}

	
	public Integer next() {
		

		int low = bits.readBinary(in, lowBitsOffset, l);
		int high = bits.readUnary(in, highBitsOffset);
		delta += high;
		lowBitsOffset += l;
		highBitsOffset += high + 1;
		
		idx++;
		return curr = ((delta << l) | low);
	}

	/**
	 * Returns the next value, in the iterator, greater or equal than {@code val}.
	 * If there is no such element, the iterator reaches its end and returns -1.
	 * 
	 * @param val
	 * @return
	 */
	public Integer next(int val) {
	
		if (val < curr)
			return next();
		
		int h = val >>> l;
		do {
		
			int high = bits.readUnary(in, highBitsOffset);
			delta += high;
			highBitsOffset += high + 1;
			idx++;
		
		} while (delta < h && hasNext());
		
		lowBitsOffset = (inOffset * Byte.SIZE) + (l * (idx - 1));
		int low = bits.readBinary(in, lowBitsOffset, l);
		lowBitsOffset += l;
		int tmp = ((delta << l) | low);
		
		while (tmp < val && hasNext()) {
			
			tmp = next();
		}
		
		if (tmp < val) tmp = -1;
		
		return curr = tmp;
	}
}
