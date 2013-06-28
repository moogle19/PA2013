

#define SIZE 512

kernel void rngrngrng(global uint* data)
{
	uint id = get_global_id(0);
	uint N = get_global_size(0);
	
	for(uint i = 0; i < SIZE; ++i)
	{
		data[(47 * (id + i)) % N] = data[(27 * (id + i)) % N];
	}
}

kernel void radix(global uint* data, global uint* falses, global uint* falsesS, global uint* address)
{
	uint id = get_global_id(0);
	uint N = get_global_size(0);

	for( int i = 0; i < 803 ; i = i *2 + 1) {
		if( data & i > 0) {
			falses[id] = 0;
		}
		else {
			falses[id] = 1;
		}
	}
	
	
	
}