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

void reduce(int* data, uint id, uint blockSize) 
{
    for(uint s = 1; s <= blockSize; s *= 2)
    {
        if(id <= blockSize-s)
        {
            data[2 * id * s] = data[2 * id * s] + data[s * 2 * id + s];
        }
        barrier(CLK_GLOBAL_MEM_FENCE);
    }
}

void reduceInverse(int* data, uint id, uint blockSize) 
{
    uint stride = 1;
    for(uint s = blockSize; s > 0; s >>= 1)
    {
        if(id < stride)
        {
            int il = 2 * id * s;
            int ir = il + s;
            
            int left = data[il];
            int right = data[ir];
                                
            data[ir] = left;
            
            data[il] = left + right;
        }
        stride *= 2;
        barrier(CLK_GLOBAL_MEM_FENCE);
    }
}

kernel void scan(global int* data) 
{	
	uint id = get_global_id(0);
	uint blockSize = get_global_size(0);
	
	reduce(data, id, blockSize);
	reduceInverse(data, id, blockSize);
	/*local int dataL[SIZE*2];

    uint id = get_global_id(0);
    uint idL = get_local_id(0);
	
	uint globalSize = get_global_size(0);
	uint blockSize = get_local_size(0);
	
	if( id < globalSize/2) {
		dataL[idL * 2] = data[id * 2];
		dataL[idL * 2 + 1] = data[id * 2 + 1];
	}
	
	barrier(CLK_GLOBAL_MEM_FENCE);
	
	int value = 0;
	
     for(uint s = 1; s <= blockSize; s *= 2)
    {
        if((idL * 2) % (2* s) == value)
        {
            dataL[idL * 2] = dataL[idL * 2] + dataL[idL * 2 + s];
        }
		if((idL * 2 + 1) % (2* s) == value)
        {
            dataL[(idL * 2 + 1) + s] = dataL[(idL * 2 + 1)] + dataL[(idL * 2 + 1) + s];
        }
        barrier(CLK_GLOBAL_MEM_FENCE);
		value *= 2;
		value += 1;
    }
	
	barrier(CLK_GLOBAL_MEM_FENCE);
    
	value -= 1;
	value /= 2;
	
    for(uint s = blockSize; s > 1; s /= 2)
    {
		if((idL * 2) % (2 * s) == value || (idL * 2 + 1) % (2 * s) == value) {
			int il = idL * 2;
			int ir = il + s;
				
			int left = dataL[il];
			int right = dataL[ir];
			
			if((idL * 2) % (2 * s) == value)
			{                                
				dataL[il] = right;
				dataL[ir] = left + right;
			}
			if((idL * 2 + 1) % (2 * s) == value)
			{
				dataL[il] = right;
				dataL[ir] = left + right;
			}
		}
        barrier(CLK_GLOBAL_MEM_FENCE);

		value -= 1;
		value /= 2;
	}
	
	/*
	uint stride = 1;
    for(uint s = blockSize; s > 0; s >>= 1)
    {
        if(id % stride == value)
        {
            int il = idL;
            int ir = il + s;
            
            int left = dataL[il];
            int right = dataL[ir];
                                
            //dataL[ir] = left;
            
            dataL[ir] = left + right;
			data[value] = 0;
        }
        stride *= 2;
		value -= 1;
		value /= 2;

        barrier(CLK_GLOBAL_MEM_FENCE);
    }
	
	if( id < globalSize/2) {
		data[id * 2] = dataL[idL * 2];
		data[id * 2 + 1] = dataL[idL * 2 + 1];
	}*/
}