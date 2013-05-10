
int getIndexRowMO(int i, int j, int m)
{
	return i * m + j;
}	
	
kernel void calcMatVecProductComponent(global int* A, global int* b, global int* c, const int m)
{
    private int i = get_global_id(0);
	int sum = 0;
	for(int k = 0; k < m; k++)
    {
		sum += A[getIndexRowMO(i, k, m)] * b[k];
	}
	c[i] = sum;
}

