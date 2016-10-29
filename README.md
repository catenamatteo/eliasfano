eliasfano
=========

A simpl(istic) Java implementation of the ***Elias-Fano*** compression schema, a technique
for compressing arrays of *monotonically increasing* integers. 
The Elias-Fano compression schema permits to decompress the i-th element in the 
compressed data, without decompressing the whole array.
Similary, it permits to find the index of the first element in the compressed data which is
greater or equal to a given value -- without decompressing the whole array.


###Usage
```java
int[] a = ...; //an array of monotonically increasing integers;
//compress the array
EliasFano ef = new EliasFano();
int u = a[a.length - 1]; //the maximum value in a;
int size = ef.getCompressedSize(u, a.length); //the size of the compressed array
long[] compressed = new long[size];
ef.compress(a, 0, a.length, compressed, 0);
//decompress the array
int[] b = new int[a.length];
int L = ef.getL(u, a.length); //the number of lower bits (see references)
ef.decompress(compressed, 0, a.length, L, b, 0);
//get the value of the 4-th element in the compressed data
int val = ef.get(compressed, 0, a.length, L, 3);
//get the index of the first element, in the compressed data, greater or equal than 1000
int idx = ef.select(compressed, 0, a.length, L, 1000);
```

####So, I can't use it to compress non-increasing arrays, isn't it?
Sure you can! You just need to transform your array into a monotonically increasing one, 
by adding to the i-th value the sum of the previous values in the array. Then, you can 
recompute the original i-th element doing i-th minus (i-1)-th value.
```java
EliasFano ef = new EliasFano();
int[] a1 = ...; //a generic array
//make a2 monotonically increasing
int[] a2 = new int[a1.length];
a2[0] = a1[0];
for (int i = 1; i < a1.length; i++) a2[i]=a1[i]+a2[i-1];
//compress a2
int u = a2[a2.length-1]; //the max value in a2
int size = ef.getCompressedSize(a2[a2.length-1], a2.length);
long[] compressed = new long[size];
ef.compress(a2, 0, a2.length, compressed, 0);
//get the original i-th value of a1, as i-th minus (i-1)-th of a2
int L = ef.getL(u, a2.length);
int val = ef.get(compressed, 0, a2.length, L, i)-ef.get(compressed, 0, a2.length, L2, i-1);
```

###Dependecies 
* JUnit 4

###References
For a general understanding of the Elias-Fano technique, see:<br/>
Sebastiano Vigna, "The Revenge of Elias and Fano" ([link](http://shonan.nii.ac.jp/seminar/029/wp-content/uploads/sites/12/2013/07/Sebastiano_Shonan.pdf))<br/>

More advanced material:<br/>
Sebastiano Vigna, "Quasi-succinct indices", WSDM'13<br/>
Giuseppe Ottaviano and Rossano Venturini, "Partitioned Elias-Fano indexes", SIGIR'14<br/>
