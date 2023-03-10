## Install and Compile instructions

- Install rust on your linux machine using the following command:

```bash
sudo apt install rustc
```
- To compile the c++ code, located in the assign1/src folder, use the following command inside that same directory:

```bash
g++ -O2 matrixproduct.cpp -o fileout -lpapi 
```
- To compile the rust code, located in the assign1/src folder, use the following command inside that same directory:

```bash
rustc -O matrixproduct.rs
```
- To run the c++ code, located in the assign1/src folder, use the following command inside that same directory:

```bash
./fileout <algorithm> <matrix_size> [number_of_blocks]
```
- Note that the number of blocks is only required for the block multiplication algorithm, and that the algorithm can be either 1, 2 or 3, where 1 is the naive algorithm, 2 is the line multiplication algorithm and 3 is the block multiplication algorithm.
- To run the rust code, located in the assign1/src folder, use the following command inside that same directory:

```bash
./matrixproduct <algorithm> <matrix_size> [number_of_blocks]
```
