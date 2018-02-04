package eu.nicecode.eliasfano.benchmark;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.apache.mahout.math.Varint;

import eu.nicecode.eliasfano.Bits;
import eu.nicecode.eliasfano.EliasFano;
import eu.nicecode.eliasfano.EliasFanoIterator;
import me.lemire.integercompression.BinaryPacking;
import me.lemire.integercompression.Composition;
import me.lemire.integercompression.IntWrapper;
import me.lemire.integercompression.IntegerCODEC;
import me.lemire.integercompression.VariableByte;
import me.lemire.integercompression.differential.Delta;
import me.lemire.integercompression.synth.ClusteredDataGenerator;

/**
 * 
 * Simple class meant to compare the speed of different schemes.
 * 
 * @author Daniel Lemire (refactored by Matteo Catena)
 * 
 */
public class Benchmark {

	/**
	 * Standard benchmark
	 * 
	 * @param data
	 *            arrays of input data
	 * @param repeat
	 *            How many times to repeat the test
	 * @param verbose
	 *            whether to output result on screen
	 */
	private static void testEF(int sparsity, int[][][] data, int repeat, boolean verbose) {

		if (verbose) {
			System.out.println("# EliasFano (stream)");
			System.out.println("# memory footprint (kb), intersection time (ms)");
		}

		int N = data.length;

		// This variable hold time in microseconds (10^-6).
		long intersectTime = 0;
		double totalsize = 0;

		for (int r = 0; r < repeat; ++r) {
			for (int k = 0; k < N; ++k) {

				int[] data0 = data[k][0];
				int[] data1 = data[k][1];

				// compress data.
				totalsize += EliasFano.getCompressedSize(data0[data0.length-1], data0.length);
				byte[] compressBuffer0 = EliasFano.compress(data0, 0, data0.length);
				totalsize += EliasFano.getCompressedSize(data1[data1.length-1], data1.length);
				byte[] compressBuffer1 = EliasFano.compress(data1, 0, data1.length);
				int L0 = EliasFano.getL(data0[data0.length - 1], data0.length);
				int L1 = EliasFano.getL(data1[data1.length - 1], data1.length);
				EliasFanoIterator efi0 = new EliasFanoIterator(compressBuffer0, 0, data0.length, L0);
				EliasFanoIterator efi1 = new EliasFanoIterator(compressBuffer1, 0, data1.length, L1);

				// intersect compressed data
				long beforeIntersect = System.nanoTime();

				int[] c = new int[Math.min(data0.length, data1.length)];
				int ci = 0;
				int a = efi0.nextPrimitiveInt();
				int b = efi1.nextPrimitiveInt();
				while (true) {
					if (a < b) {
						if (efi0.hasNext())
							a = efi0.nextPrimitiveInt(b);
						else
							break;
					} else if (a > b) {
						if (efi1.hasNext())
							b = efi1.nextPrimitiveInt(a);
						else
							break;
					} else {
						if (ci == 0 || a != c[ci - 1]) {
							c[ci++] = a;
						}
						if (efi0.hasNext() && efi1.hasNext()) {
							a = efi0.nextPrimitiveInt();
							b = efi1.nextPrimitiveInt();
						} else {
							break;
						}
					}
				}

				int[] intersect = Arrays.copyOfRange(c, 0, ci);
				long afterIntersect = System.nanoTime();

				// measure time of intersection.
				intersectTime += TimeUnit.NANOSECONDS.toMillis(afterIntersect - beforeIntersect);
				if (intersect.length != data[k][2].length)
					throw new RuntimeException("we have a bug (diff length) " + "EliasFano expected "
							+ data[k][2].length + " got " + intersect.length);

				// verify intersection

				for (int m = 0; m < intersect.length; ++m) {
					if (intersect[m] != data[k][2][m]) {
						throw new RuntimeException("we have a bug (actual difference), expected " + data[k][2][m]
								+ " found " + intersect[m] + " at " + m + " out of " + intersect.length);
					}
				}
			}
		}

		if (verbose) {

			double memoryfootprint = (totalsize / (N * repeat)) / 1024.0;
			double intersectSpeed = ((double) intersectTime) / (N * repeat);
			System.out.println(String.format("\t%1$.2f\t%2$.2f", memoryfootprint, intersectSpeed));
		}
	}

	private static void testBP(int sparsity, int[][][] data, int repeat, boolean verbose) {

		IntegerCODEC codec = new Composition(new BinaryPacking(), new VariableByte());

		if (verbose) {
			System.out.println("# " + codec.toString());
			System.out.println("# memory footprint (kb), decompression + intersection time (ms) ");
		}

		int N = data.length;

		int maxLength = 0;
		for (int k = 0; k < N; ++k) {
			for (int j = 0; j < 3; j++) {
				if (data[k][j].length > maxLength) {
					maxLength = data[k][j].length;
				}
			}
		}

		// 4x + 1024 to account for the possibility of some negative
		// compression.
		int[] compressBuffer0 = new int[4 * maxLength + 1024];
		int[] compressBuffer1 = new int[4 * maxLength + 1024];

		// This variable holds time in microseconds (10^-6).
		long intersectTime = 0;
		double totalsize = 0;

		IntWrapper inpos0 = new IntWrapper();
		IntWrapper outpos0 = new IntWrapper();
		IntWrapper inpos1 = new IntWrapper();
		IntWrapper outpos1 = new IntWrapper();

		for (int r = 0; r < repeat; ++r) {
			for (int k = 0; k < N; ++k) {

				int[] backupdata0 = Arrays.copyOf(data[k][0], data[k][0].length);
				int[] backupdata1 = Arrays.copyOf(data[k][1], data[k][1].length);

				totalsize += data[k][0].length + data[k][1].length;

				// compress data.
				inpos0.set(0);
				outpos0.set(0);
				inpos1.set(0);
				outpos1.set(0);
				Delta.delta(backupdata0);
				Delta.delta(backupdata1);
				codec.compress(backupdata0, inpos0, backupdata0.length, compressBuffer0, outpos0);
				codec.compress(backupdata1, inpos1, backupdata1.length, compressBuffer1, outpos1);

				final int thiscompsize0 = outpos0.get();
				final int thiscompsize1 = outpos1.get();
				
				// uncompress and intersect data
				long beforeIntersect = System.nanoTime();
				inpos0.set(0);
				outpos0.set(0);
				inpos1.set(0);
				outpos1.set(0);
				codec.uncompress(compressBuffer0, inpos0, thiscompsize0, backupdata0, outpos0);
				codec.uncompress(compressBuffer1, inpos1, thiscompsize1, backupdata1, outpos1);
				Delta.fastinverseDelta(backupdata0);
				Delta.fastinverseDelta(backupdata1);

				int[] intersect = intersectSortedArrays(backupdata0, backupdata1);
				long afterIntersect = System.nanoTime();

				// measure time of intersection.
				intersectTime += TimeUnit.NANOSECONDS.toMillis(afterIntersect - beforeIntersect);
				if (intersect.length != data[k][2].length)
					throw new RuntimeException("we have a bug (diff length) " + codec + " expected " + data[k][2].length
							+ " got " + intersect.length);

				// verify intersection.

				for (int m = 0; m < intersect.length; ++m) {
					if (intersect[m] != data[k][2][m]) {
						throw new RuntimeException("we have a bug (actual difference), expected " + data[k][2][m]
								+ " found " + intersect[m] + " at " + m + " out of " + intersect.length);
					}
				}
			}
		}

		if (verbose) {

			double memoryfootprint = (( totalsize / (N * repeat)) * Integer.BYTES ) / 1024.0;
			double intersectSpeed = ((double) intersectTime) / (N * repeat);
			System.out.println(String.format("\t%1$.2f\t%2$.2f", memoryfootprint, intersectSpeed));
		}
	}

	private static void testVBS(int sparsity, int[][][] data, int repeat, boolean verbose) throws IOException {

		if (verbose) {
			System.out.println("# VariableByte (stream)");
			System.out.println("# memory footprint (kb), decompression + intersection time (ms) ");
		}

		int N = data.length;

		int maxLength = 0;
		for (int k = 0; k < N; ++k) {
			for (int j = 0; j < 3; j++) {
				if (data[k][j].length > maxLength) {
					maxLength = data[k][j].length;
				}
			}
		}

		// 4x + 1024 to account for the possibility of some negative
		// compression.
		ByteArrayOutputStream compressBuffer0 = new ByteArrayOutputStream(4 * maxLength + 1024);
		ByteArrayOutputStream compressBuffer1 = new ByteArrayOutputStream(4 * maxLength + 1024);

		// This variable holds time in microseconds (10^-6).
		long intersectTime = 0;
		double totalsize = 0;

		for (int r = 0; r < repeat; ++r) {
			for (int k = 0; k < N; ++k) {

				int[] data0 = data[k][0];
				int[] data1 = data[k][1];

				compressBuffer0.reset();
				compressBuffer1.reset();
				DataOutput dout0 = new DataOutputStream(compressBuffer0);
				DataOutput dout1 = new DataOutputStream(compressBuffer1);
				Varint.writeUnsignedVarInt(data0[0], dout0);
				for (int i = 1; i < data0.length; i++)
					Varint.writeUnsignedVarInt(data0[i] - data0[i - 1], dout0);
				Varint.writeUnsignedVarInt(data1[0], dout1);
				for (int i = 1; i < data1.length; i++)
					Varint.writeUnsignedVarInt(data1[i] - data1[i - 1], dout1);
				compressBuffer0.close();
				compressBuffer1.close();

				byte[] b0 = compressBuffer0.toByteArray();
				byte[] b1 = compressBuffer1.toByteArray();
				totalsize += b0.length + b1.length;

				DataInput din0 = new DataInputStream(new ByteArrayInputStream(b0));
				DataInput din1 = new DataInputStream(new ByteArrayInputStream(b1));

				// intersect data
				long beforeIntersect = System.nanoTime();

				int[] c = new int[Math.min(data0.length, data1.length)];
				int ai = 0, bi = 0, ci = 0;
				int a = Varint.readUnsignedVarInt(din0);
				int b = Varint.readUnsignedVarInt(din1);
				while (true) {
					if (a < b) {
						ai++;
						if (ai < data0.length)
							a += Varint.readUnsignedVarInt(din0);
						else
							break;
					} else if (a > b) {
						bi++;
						if (bi < data1.length)
							b += Varint.readUnsignedVarInt(din1);
						else
							break;
					} else {
						if (ci == 0 || a != c[ci - 1]) {
							c[ci++] = a;
						}

						ai++;
						bi++;

						if (ai < data0.length && bi < data1.length) {

							a += Varint.readUnsignedVarInt(din0);
							b += Varint.readUnsignedVarInt(din1);

						} else {

							break;
						}
					}
				}
				int[] intersect = Arrays.copyOfRange(c, 0, ci);

				long afterIntersect = System.nanoTime();

				// measure time of intersection.
				intersectTime += TimeUnit.NANOSECONDS.toMillis(afterIntersect - beforeIntersect);
				if (intersect.length != data[k][2].length)
					throw new RuntimeException("we have a bug (diff length) " + "VariableByte (stream) expected "
							+ data[k][2].length + " got " + intersect.length);

				// verify intersection.

				for (int m = 0; m < intersect.length; ++m) {
					if (intersect[m] != data[k][2][m]) {
						throw new RuntimeException("we have a bug (actual difference), expected " + data[k][2][m]
								+ " found " + intersect[m] + " at " + m + " out of " + intersect.length);
					}
				}
			}
		}

		if (verbose) {

			double memoryfootprint = (totalsize / (N * repeat))/1024.0;
			double intersectSpeed = ((double) intersectTime) / (N * repeat);
			System.out.println(String.format("\t%1$.2f\t%2$.2f", memoryfootprint, intersectSpeed));
		}
	}

	/**
	 * Main method.
	 * 
	 * @param args
	 *            command-line arguments
	 * @throws FileNotFoundException
	 *             when we fail to create a new file
	 */
	public static void main(String args[]) throws FileNotFoundException {
		System.out.println("# benchmark based on the ClusterData model from:");
		System.out.println("# 	 Vo Ngoc Anh and Alistair Moffat. ");
		System.out.println("#	 Index compression using 64-bit words.");
		System.out.println("# 	 Softw. Pract. Exper.40, 2 (February 2010), 131-147. ");
		System.out.println();

		test(10, 20, 20);
	}

	/**
	 * Generate test data.
	 * 
	 * @param N
	 *            How many input arrays to generate
	 * @param nbr
	 *            How big (in log2) should the arrays be
	 * @param sparsity
	 *            How sparse test data generated
	 */
	private static int[][][] generateTestData(ClusteredDataGenerator dataGen, int N, int nbr, int sparsity) {
		final int[][][] data = new int[N][3][];
		final int dataSize = (1 << (nbr + sparsity));
		for (int i = 0; i < N; ++i) {
			data[i][0] = dataGen.generateClustered((1 << nbr), dataSize);
			data[i][1] = dataGen.generateClustered((1 << nbr), dataSize);
			data[i][2] = intersectSortedArrays(data[i][0], data[i][1]);
		}
		return data;
	}

	private static int[] intersectSortedArrays(int[] a, int[] b) {
		int[] c = new int[Math.min(a.length, b.length)];
		int ai = 0, bi = 0, ci = 0;
		while (ai < a.length && bi < b.length) {
			if (a[ai] < b[bi]) {
				ai++;
			} else if (a[ai] > b[bi]) {
				bi++;
			} else {
				if (ci == 0 || a[ai] != c[ci - 1]) {
					c[ci++] = a[ai];
				}
				ai++;
				bi++;
			}
		}
		return Arrays.copyOfRange(c, 0, ci);
	}

	/**
	 * Generates data and calls other tests.
	 * 
	 * @param csvLog
	 *            Writer for CSV log.
	 * @param N
	 *            How many input arrays to generate
	 * @param nbr
	 *            how big (in log2) should the arrays be
	 * @param repeat
	 *            How many times should we repeat tests.
	 */
	private static void test(int N, int nbr, int repeat) {

		ClusteredDataGenerator cdg = new ClusteredDataGenerator();
		final int max_sparsity = 31 - nbr;
		for (int sparsity = 1; sparsity < max_sparsity; ++sparsity) {
			System.out.println("# sparsity " + sparsity);
			System.out.println("# generating random data...");
			int[][][] data = generateTestData(cdg, N, nbr, sparsity);
			System.out.println("# generating random data... ok.");

			testBP(sparsity, data, repeat, false);
			testBP(sparsity, data, repeat, false);
			testBP(sparsity, data, repeat, true);

			try {
				testVBS(sparsity, data, repeat, false);
				testVBS(sparsity, data, repeat, false);
				testVBS(sparsity, data, repeat, true);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			testEF(sparsity, data, repeat, false);
			testEF(sparsity, data, repeat, false);
			testEF(sparsity, data, repeat, true);

		}
	}
}
