int getIndexRowMO(int i, int j, int m)
{
    return i * m + j;
}   
    
kernel
void calcMatrixProduct
(global int* A,
 global int* B,
 global int* C,
 const int a_m,
 const int a_n,
 const int b_n)
{
    private int id0 = get_global_id(0);
    private int id1 = get_global_id(1);
    int sum = 0;
   
    for (int k = 0; k < a_n; ++k)
    {
        sum += A[getIndexRowMO(id1, k, a_n)] * B[getIndexRowMO(k, id0, b_n)];
    }
    C[getIndexRowMO(id1, id0, a_m)] = sum;
}