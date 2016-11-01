package benchmark;

import java.io.FileNotFoundException;
import java.util.Arrays;

import eu.nicecode.eliasfano.EliasFano;
import me.lemire.integercompression.synth.ClusteredDataGenerator;

/**
 * 
 * Simple class meant to compare the speed of different schemes.
 * 
 * @author Daniel Lemire
 * 
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
    private static void testEF(int sparsity, int[][][] data, int repeat, boolean verbose) {
            
    		EliasFano ef = new EliasFano();
    	
    		if (verbose) {
                    System.out.println("# EliasFano");
                    System.out
                            .println("# memory footprint (bytes), intersect time (micros)");
            }

            int N = data.length;

            int maxLength = 0;
            for (int k = 0; k < N; ++k) {
            	for (int j = 0; j < 3; j++) {
            		if (data[k][j].length == 0) continue;
                    int len = ef.getCompressedSize(data[k][j][data[k][j].length - 1], data[k][j].length);
                    if (len > maxLength) {
                    	
                    	maxLength = len;
                    }
            	}
            }

            // 4x + 1024 to account for the possibility of some negative
            // compression.
            byte[] compressBuffer0 = new byte[maxLength];
            byte[] compressBuffer1 = new byte[maxLength];

            // These variables hold time in microseconds (10^-6).
            long intersectTime = 0;
            long totalsize = 0;

            for (int r = 0; r < repeat; ++r) {
                    for (int k = 0; k < N; ++k) {
                            
                    		int[] data0 = data[k][0];
                    		int[] data1 = data[k][1];
                    		
                            // compress data.
                    		Arrays.fill(compressBuffer0, (byte) 0);
                    		Arrays.fill(compressBuffer1, (byte) 0);
                    		totalsize += ef.compress(data0, 0, data0.length, compressBuffer0, 0);
                    		totalsize += ef.compress(data1, 0, data1.length, compressBuffer1, 0);
                    		int L0 = ef.getL(data0[data0.length-1], data0.length);
                    		int L1 = ef.getL(data1[data1.length-1], data1.length);
                    		
                    		// extract (uncompress) data
                            long beforeIntersect = System.nanoTime() / 1000;

                            int[] c = new int[Math.min(data0.length, data1.length)]; 
                            int ai = 0, bi = 0, ci = 0;
                            while ((ai != -1 && ai < data0.length) && (bi != -1 && bi < data1.length)) {
                                
                            	int a = ef.get(compressBuffer0, 0, data0.length, L0, ai);
                            	int b = ef.get(compressBuffer1, 0, data1.length, L1, bi);
                            	if (a < b) {
                                    ai = ef.select(compressBuffer0, 0, data0.length, L0, b);
                                } else if (a > b) {
                                    bi = ef.select(compressBuffer1, 0, data1.length, L1, a);
                                } else {
                                    if (ci == 0 || a != c[ci - 1]) {
                                        c[ci++] = a;
                                    }
                                    ai++; bi++;
                               }
                            }
                            
                            int[] intersect = Arrays.copyOfRange(c, 0, ci);
                            long afterIntersect = System.nanoTime() / 1000;

                            // measure time of extraction (uncompression).
                            intersectTime += afterIntersect
                                    - beforeIntersect;
                            if (intersect.length != data[k][2].length)
                                    throw new RuntimeException(
                                            "we have a bug (diff length) "
                                                    + "EliasFano expected "
                                                    + data[k][2].length
                                                    + " got "
                                                    + intersect.length);

                            // verify: compare original array with
                            // compressed and
                            // uncompressed.

                            for (int m = 0; m < intersect.length; ++m) {
                                    if (intersect[m] != data[k][2][m]) {
                                            throw new RuntimeException(
                                                    "we have a bug (actual difference), expected "
                                                            + data[k][2][m]
                                                            + " found "
                                                            + intersect[m]
                                                            + " at " + m + " out of " + intersect.length);
                                    }
                            }
                    }
            }

            if (verbose) {
                    
            	double memoryfootprint = ((double) totalsize) / (data.length * repeat);
            	double intersectSpeed = ((double) intersectTime) / (data.length * repeat);
                        ;
                    System.out.println(String.format(
                            "\t%1$.2f\t%2$.2f", memoryfootprint, intersectSpeed));
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
         */
        private static void testUncompressed(int sparsity, int[][][] data, int repeat, boolean verbose) {
                        	
        		if (verbose) {
                        System.out.println("# Uncompressed");
                        System.out
                                .println("# memory footprint (bytes), intersection time (micros)");
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

                // These variables hold time in microseconds (10^-6).
                long intersectTime = 0;
                long totalsize = 0;

                for (int r = 0; r < repeat; ++r) {
                        for (int k = 0; k < N; ++k) {
                                
                        		
                        		totalsize += data[k][0].length + data[k][1].length;
                                
                                // extract (uncompress) data
                                long beforeIntersect = System.nanoTime() / 1000;
                                int[] intersect = intersectSortedArrays(data[k][0], data[k][1]);
                                long afterIntersect = System.nanoTime() / 1000;

                                // measure time of extraction (uncompression).
                                intersectTime += afterIntersect
                                        - beforeIntersect;
                                if (intersect.length != data[k][2].length)
                                        throw new RuntimeException(
                                                "we have a bug (diff length) "
                                                        + "Uncompressed expected "
                                                        + data[k][2].length
                                                        + " got "
                                                        + intersect.length);

                                // verify: compare original array with
                                // compressed and
                                // uncompressed.

                                for (int m = 0; m < intersect.length; ++m) {
                                        if (intersect[m] != data[k][2][m]) {
                                                throw new RuntimeException(
                                                        "we have a bug (actual difference), expected "
                                                                + data[k][2][m]
                                                                + " found "
                                                                + intersect[m]
                                                                + " at " + m + " out of " + intersect.length);
                                        }
                                }
                        }
                }

                if (verbose) {
                        
                		double memoryfootprint = (4.0 * totalsize) / (data.length * repeat); 
                        double intersectSpeed = ((double) intersectTime) / (data.length * repeat)
                           ;
                        System.out.println(String.format(
                                "\t%1$.2f\t%2$.2f", memoryfootprint, intersectSpeed));
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

                test(100, 9, 100);
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
        private static int[][][] generateTestData(ClusteredDataGenerator dataGen,
                int N, int nbr, int sparsity) {
                final int[][][] data = new int[N][3][];
                final int dataSize = (1 << (nbr + sparsity));
                for (int i = 0; i < N; ++i) {
                        data[i][0] = dataGen.generateClustered((1 << nbr),
                                dataSize);
                        data[i][1] = dataGen.generateClustered((1 << nbr),
                                dataSize);
                        data[i][2] = intersectSortedArrays(data[i][0], data[i][1]);
                }
                return data;
        }

        private static int[] intersectSortedArrays(int[] a, int[] b){
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
                    ai++; bi++;
                }
            }
            return Arrays.copyOfRange(c, 0, ci); 
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
                        int[][][] data = generateTestData(cdg, N, nbr, sparsity);
                        System.out.println("# generating random data... ok.");

                        testUncompressed(sparsity, data, repeat, false);
                        testUncompressed(sparsity, data, repeat, false);
                        testUncompressed(sparsity, data, repeat, true);
                        
                        testEF(sparsity, data, repeat, false);
                        testEF(sparsity, data, repeat, false);
                        testEF(sparsity, data, repeat, true);

                }
        }
}
