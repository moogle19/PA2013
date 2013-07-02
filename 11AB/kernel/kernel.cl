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


kernel void scan(global int* data) 
{	
	local int dataL[SIZE*2];

    uint id = get_global_id(0);
    uint idL = get_local_id(0);
	
	uint globalSize = get_global_size(0);
	uint blockSize = get_local_size(0);
	
	int value = 0;
	
	if(id * 2 < globalSize) {
		dataL[idL * 2] = data[id * 2];
		dataL[idL * 2 + 1] = data[id * 2 + 1];
	}
	
	for(uint s = 1; s <= blockSize*2; s *= 2)
    {
        if( (idL*2) % (s*2) == value && idL*2+s < 1024) {
			dataL[idL*2+s] = dataL[idL*2] + dataL[idL*2+s];
		}
		if( (idL*2 + 1) % (s*2) == value && idL*2+s+1 < 1024) {
			dataL[idL*2+1+s] = dataL[idL*2+1+s] + dataL[idL*2+1];
		}
		barrier(CLK_GLOBAL_MEM_FENCE);
		value *= 2;
		value += 1;
	}
	
	//reverse
	
	uint stride = blockSize;
	value = blockSize * 2 - 1;
	for(uint s = blockSize; s > 1; s /= 2)
    {
		if( (idL*2) % (s*2) == value && idL*2+s < 1024) {
			int first = dataL[idL*2];
			int second = dataL[idL*2+s];
			dataL[idL*2+s] = dataL[idL*2] + dataL[idL*2+s];
			dataL[idL*2] = second;
		}
		
		if( (idL*2 + 1) % (s*2) == value && idL*2+s+1 < 1024) {
			int first = dataL[idL*2+1];
			int second = dataL[idL*2+1+s];
			dataL[idL*2+1+s] = dataL[idL*2+1] + dataL[idL*2+1+s];
			dataL[idL*2+1] = second;
		}
		
		barrier(CLK_GLOBAL_MEM_FENCE);
		value -= 1;
		value /= 2;
	}
		
	if(id * 2 < globalSize) {
		data[id * 2] = dataL[idL * 2] ;
		data[id * 2 + 1] = dataL[idL * 2 + 1];
	}
}