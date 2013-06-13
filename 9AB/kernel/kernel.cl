

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