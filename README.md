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

EliasFano ef = new EliasFano();
int u = a[a.length - 1]; //the maximum value in a;
int size = ef.getCompressedSize(u, a.length); //the size of the compressed array
long[] compressed = new long[size];
//compress the array
ef.compress(a, 0, a.length, compressed, 0);

int[] b = new int[a.length];

int L = ef.getL(u, a.length); //the number of lower bits (see references)

//decompress the array
ef.decompress(compressed, 0, a.length, L, b, 0);

//get the value of the 4-th element in the compressed data
int val = ef.get(compressed, 0, a.length, L, 3);

//get the index of the first element, in the compressed data, greater or equal than 1000
int idx = ef.select(compressed, 0, a.length, L, 1000);
```

###Dependecies 
* JUnit 4

###References
For a general understanding of the Elias-Fano technique, see:

Sebastiano Vigna, The Revenge of Elias and Fano [link](http://shonan.nii.ac.jp/seminar/029/wp-content/uploads/sites/12/2013/07/Sebastiano_Shonan.pdf)

More advanced material:

Sebastiano Vigna, Quasi-succinct indices, WSDM'13 

Giuseppe Ottaviano and Rossano Venturini, Partitioned Elias-Fano indexes, SIGIR'14