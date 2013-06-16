

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

kernel void arrangeNewRays(	global int* exists,
							global int* tmp,
							global int* memoryIndexIn,
							global int* memoryIndexOut,
							const int datalength)
{
	uint id = get_global_id(0);
	
	if (exists[id] == 1)
	{
		for (int i = id; i < datalength; i++)
		{
			atomic_inc(&tmp[i]);
		}
		barrier(CLK_GLOBAL_MEM_FENCE);
		memoryIndexOut[tmp[id]] = memoryIndexIn[id];
	}
}