package eu.nicecode.eliasfano;

import java.util.Iterator;

/**
 * This class reads an array compressed with {@link eu.nicecode.eliasfano.EliasFano} in a stream fashion.
 * 
 * @author Matteo Catena
 *
 */
public class EliasFanoIterator implements Iterator<Integer>, Iterable<Integer> {

	private int idx = 0;
	private int curr = -1;
	
	private long inOffset;
	private long highBitsOffset;
	private long lowBitsOffset;
		
	private final byte[] in;
	private final int size;
	private final int l;
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
		highBitsOffset = EliasFano.roundUp(lowBitsOffset + (l * size), Byte.SIZE);

	}
	
	public boolean hasNext() {
		
		return idx < size;
	}

	/**
	 * Returns the next element in the iteration,
	 * but as a primitive int (this should be faster.)
	 * 
	 * @return the next element in the iteration
	 */
	public final int nextPrimitiveInt() {
		
		final int low = Bits.readBinary(in, lowBitsOffset, l);
		final int high = Bits.readUnary(in, highBitsOffset);
		delta += high;
		lowBitsOffset += l;
		highBitsOffset += high + 1;
		
		idx++;
		return curr = ((delta << l) | low);
	}
	
	@Override
	public final Integer next() {
		
		return nextPrimitiveInt();
	}
	
	/**
	 * Returns the next value, in the iterator, greater or equal than {@code val}.
	 * If there is no such element, the iterator reaches its end and returns -1.
	 * This returns a primitive int, as it should be faster.
	 * 
	 * @param val
	 * @return
	 */
	public int nextPrimitiveInt(final int val) {
	
		if (val < curr)
			return next();
		
		final int h = val >>> l;
		do {
		
			final int high = Bits.readUnary(in, highBitsOffset);
			delta += high;
			highBitsOffset += high + 1;
			idx++;
		
		} while (delta < h && hasNext());
		
		lowBitsOffset = (inOffset * Byte.SIZE) + (l * (idx - 1));
		final int low = Bits.readBinary(in, lowBitsOffset, l);
		lowBitsOffset += l;		
		int tmp = ((delta << l) | low);
		
		while (tmp < val && hasNext()) {
			
			tmp = next();
		}
		
		if (tmp < val) tmp = -1;
		
		return curr = tmp;
	}	

	/**
	 * Returns the next value, in the iterator, greater or equal than {@code val}.
	 * If there is no such element, the iterator reaches its end and returns -1.
	 * 
	 * @param val
	 * @return
	 */
	public final Integer next(int val) {
	
		return nextPrimitiveInt(val);
	}

	@Override
	public final Iterator<Integer> iterator() {
		
		return this;
	}
}
