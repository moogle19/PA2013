

#define SIZE 64

kernel void rngrngrng(global int* data)
{
	uint id = get_global_id(0);
	uint N = get_global_size(0);
	
	for(uint i = 0; i < SIZE; ++i)
	{
		data[(16 * (id + i)) % N] = data[(4 * (id + i)) % N];
	}
}

kernel void arrangeNewRays( global int* exists,
							global int* memAccess,
							const int log2n)
{
	uint id = get_global_id(0);
	uint gws = get_global_size(0);
	
	int stride = 1;
	int firstId;
	int secondId;
	int tmpVal;
	
	int state = exists[id];
	
	//reduction
	for (int i = 0; i < log2n; i++)
	{
		if(id % (2 * stride) == 0)
		{
			firstId = 2*stride*id + stride -1;
			secondId = firstId + stride;
			exists[secondId] = exists[firstId] + exists[secondId];
		}
		barrier(CLK_GLOBAL_MEM_FENCE);
		stride *= 2;
	}
	exists[gws-1] = 0;
	stride = gws/2;
	
	//2nd reduction 
	for (int i = 0; i < log2n; i++)
	{
		if(id % (2 * stride) == 0)
		{
			firstId = 2*stride*id + stride -1;
			secondId = firstId + stride;
			tmpVal = exists[secondId];
			exists[secondId] = exists[firstId] + tmpVal;
			exists[firstId] = tmpVal;
		}
		barrier(CLK_GLOBAL_MEM_FENCE);
		stride /= 2;
	}
	//compress
	memAccess[id] = exists[id];
	if (state == 1)
	{
		
		//memAccess[exists[id]] = memAccess[id];
		//atomic_inc(&raysCount[0]);
	}
}
