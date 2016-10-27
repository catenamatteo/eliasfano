/**
 * This code is released under the
 * Apache License Version 2.0 http://www.apache.org/licenses/.
 *
 * (c) Daniel Lemire, http://lemire.me/en/
 */
package eu.nicecode.eliasfano.benchmark;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.mahout.math.Varint;

import eu.nicecode.eliasfano.EliasFano;
import me.lemire.integercompression.differential.Delta;
import me.lemire.integercompression.synth.ClusteredDataGenerator;

/**
 * 
 * Simple class meant to compare the speed of different schemes.
 * 
 * @author Daniel Lemire (modified by Matteo Catena)
 * 
 */
/*
 * TODO:
 * add tests for get() and select()
 */
public class Benchmark {

        /**
         * Standard benchmark
         * 
         * @param data
         *                arrays of input data
         * @param repeat
         *                How many times to repeat the test
         * @param verbose
         *                whether to output result on screen
         */
        private static void testEliasFanoCodec(int sparsity, int[][] data, int repeat, boolean verbose) {
                if (verbose) {
                        System.out.println("# EliasFano");
                        System.out
                                .println("# bits per int, compress speed (mis), decompression speed (mis) ");
                }

                EliasFano ef = new EliasFano();
                
                int N = data.length;

                int totalSize = 0;
                int maxLength = 0;
                for (int k = 0; k < N; ++k) {
                        totalSize += data[k].length;
                        if (data[k].length > maxLength) {
                                maxLength = data[k].length;
                        }
                }


                long[] compressBuffer = new long[maxLength];
                int[] decompressBuffer = new int[maxLength];

                // These variables hold time in microseconds (10^-6).
                long compressTime = 0;
                long decompressTime = 0;

                int size = 0;

                for (int r = 0; r < repeat; ++r) {
                        size = 0;
                        for (int k = 0; k < N; ++k) {
                                
                        		Arrays.fill(compressBuffer, 0);
                        		int[] backupdata = Arrays.copyOf(data[k],
                                        data[k].length);

                        		int L = ef.getL(backupdata[backupdata.length-1], backupdata.length);
                        		
                                // compress data.
                                long beforeCompress = System.nanoTime() / 1000;

                                int csize = ef.compress(backupdata, 0, backupdata.length, compressBuffer, 0);
                                
                                long afterCompress = System.nanoTime() / 1000;

                                // measure time of compression.
                                compressTime += afterCompress - beforeCompress;
                                final int thiscompsize = csize;
                                size += thiscompsize;

                                // extract (uncompress) data
                                long beforeDecompress = System.nanoTime() / 1000;
                                ef.decompress(compressBuffer, 0,
                                        backupdata.length, L, decompressBuffer, 0);

                                long afterDecompress = System.nanoTime() / 1000;

                                // measure time of extraction (uncompression).
                                decompressTime += afterDecompress
                                        - beforeDecompress;

                                // verify: compare original array with
                                // compressed and
                                // uncompressed.

                                for (int m = 0; m < data[k].length; ++m) {
                                        if (decompressBuffer[m] != data[k][m]) {
                                                throw new RuntimeException(
                                                        "we have a bug (actual difference), expected "
                                                                + data[k][m]
                                                                + " found "
                                                                + decompressBuffer[m]
                                                                + " at " + m + " out of " + data[k].length);
                                        }
                                }
                        }
                }

                if (verbose) {
                        double bitsPerInt = size * 64.0 / totalSize;
                        long compressSpeed = totalSize * repeat
                                / (compressTime);
                        long decompressSpeed = totalSize * repeat
                                / (decompressTime);
                        System.out.println(String.format(
                                "\t%1$.2f\t%2$d\t%3$d", bitsPerInt,
                                compressSpeed, decompressSpeed));
                }
        }
        
        /**
         * Standard benchmark
         * 
         * @param data
         *                arrays of input data
         * @param repeat
         *                How many times to repeat the test
         * @param verbose
         *                whether to output result on screen
         * @throws IOException 
         */
        private static void testVarintCodec(int sparsity, int[][] data, int repeat, boolean verbose) throws IOException {
                if (verbose) {
                        System.out.println("# Varint");
                        System.out
                                .println("# bits per int, compress speed (mis), decompression speed (mis) ");
                }

                
                int N = data.length;

                int totalSize = 0;
                int maxLength = 0;
                for (int k = 0; k < N; ++k) {
                        totalSize += data[k].length;
                        if (data[k].length > maxLength) {
                                maxLength = data[k].length;
                        }
                }


                int[] decompressBuffer = new int[maxLength];

                // These variables hold time in microseconds (10^-6).
                long compressTime = 0;
                long decompressTime = 0;

                int size = 0;

                for (int r = 0; r < repeat; ++r) {
                        size = 0;
                        for (int k = 0; k < N; ++k) {
                                
                        		int[] backupdata = Arrays.copyOf(data[k],
                                        data[k].length);

                        		ByteArrayOutputStream compressBuffer = new ByteArrayOutputStream(5 * maxLength);
                        		DataOutput dout = new DataOutputStream(compressBuffer);
                        		
                                // compress data.
                                long beforeCompress = System.nanoTime() / 1000;
                                
                                Delta.delta(backupdata);
                                for (int i = 0; i < backupdata.length; i++) Varint.writeUnsignedVarInt(backupdata[i], dout);
                                compressBuffer.close();
                                
                                int csize = compressBuffer.size();
                                
                                long afterCompress = System.nanoTime() / 1000;

                                // measure time of compression.
                                compressTime += afterCompress - beforeCompress;
                                final int thiscompsize = csize;
                                size += thiscompsize;

                                // extract (uncompress) data
                                DataInput din = new DataInputStream(new ByteArrayInputStream(compressBuffer.toByteArray()));
                                
                                long beforeDecompress = System.nanoTime() / 1000;
                                
                                for (int i = 0; i < backupdata.length; i++) decompressBuffer[i] = Varint.readUnsignedVarInt(din);
                                Delta.fastinverseDelta(decompressBuffer);

                                long afterDecompress = System.nanoTime() / 1000;

                                // measure time of extraction (uncompression).
                                decompressTime += afterDecompress
                                        - beforeDecompress;

                                // verify: compare original array with
                                // compressed and
                                // uncompressed.

                                for (int m = 0; m < data[k].length; ++m) {
                                        if (decompressBuffer[m] != data[k][m]) {
                                                throw new RuntimeException(
                                                        "we have a bug (actual difference), expected "
                                                                + data[k][m]
                                                                + " found "
                                                                + decompressBuffer[m]
                                                                + " at " + m + " out of " + data[k].length);
                                        }
                                }
                        }
                }

                if (verbose) {
                        double bitsPerInt = size * 8.0 / totalSize;
                        long compressSpeed = totalSize * repeat
                                / (compressTime);
                        long decompressSpeed = totalSize * repeat
                                / (decompressTime);
                        System.out.println(String.format(
                                "\t%1$.2f\t%2$d\t%3$d", bitsPerInt,
                                compressSpeed, decompressSpeed));
                }
        }

        
        /**
         * Main method.
         * 
         * @param args
         *                command-line arguments
         * @throws FileNotFoundException when we fail to create a new file
         */
        public static void main(String args[]) throws FileNotFoundException  {
                System.out
                        .println("# benchmark based on the ClusterData model from:");
                System.out.println("# 	 Vo Ngoc Anh and Alistair Moffat. ");
                System.out.println("#	 Index compression using 64-bit words.");
                System.out
                        .println("# 	 Softw. Pract. Exper.40, 2 (February 2010), 131-147. ");
                System.out.println();

                test(20, 18, 10);

        }


        /**
         * Generate test data.
         * 
         * @param N
         *                How many input arrays to generate
         * @param nbr
         *                How big (in log2) should the arrays be
         * @param sparsity
         *                How sparse test data generated
         */
        private static int[][] generateTestData(ClusteredDataGenerator dataGen,
                int N, int nbr, int sparsity) {
                final int[][] data = new int[N][];
                final int dataSize = (1 << (nbr + sparsity));
                for (int i = 0; i < N; ++i) {
                        data[i] = dataGen.generateClustered((1 << nbr),
                                dataSize);
                }
                return data;
        }

        /**
         * Generates data and calls other tests.
         * 
         * @param csvLog
         *                Writer for CSV log.
         * @param N
         *                How many input arrays to generate
         * @param nbr
         *                how big (in log2) should the arrays be
         * @param repeat
         *                How many times should we repeat tests.
         */
        private static void test(int N, int nbr, int repeat) {

                ClusteredDataGenerator cdg = new ClusteredDataGenerator();
                final int max_sparsity = 31 - nbr;
                for (int sparsity = 1; sparsity < max_sparsity; ++sparsity) {
                        System.out.println("# sparsity " + sparsity);
                        System.out.println("# generating random data...");
                        int[][] data = generateTestData(cdg, N, nbr, sparsity);
                        System.out.println("# generating random data... ok.");

                        testEliasFanoCodec(sparsity, data, repeat, false);
                        testEliasFanoCodec(sparsity, data, repeat, false);
                        testEliasFanoCodec(sparsity, data, repeat, true);
                        System.out.println();

                        try {
                        	testVarintCodec(sparsity, data, repeat, false);
							testVarintCodec(sparsity, data, repeat, false);
							testVarintCodec(sparsity, data, repeat, true);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                        System.out.println();

                }
        }
}
