

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

kernel void reduce(global int* data) 
{
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
	
	
	
	
	
	
	int stride = 1;
	int grp_size = get_local_size(0);
	int lid = get_local_id(0);

	for(int d = grp_size; d > 0; d>>=1)
	{
		barrier(CLK_GLOBAL_MEM_FENCE);

		if(lid < d)
		{
			int ai = stride*(2*lid+1)-1+offset;
			int bi = stride*(2*lid+2)-1+offset;
			input[bi] += input[ai];
		}

		stride *= 2;
	}
  
	 int grp_size = get_local_size(0);
	 int lid = get_local_id(0);
	 int stride = grp_size*2;

	 for(int d = 1; d <= grp_size; d *=2)
	 {
		barrier(CLK_GLOBAL_MEM_FENCE);

		stride >>=1;

		if(lid+1 < d)
		{
			int src = 2*(lid + 1)*stride-1+offset;
			int dest = src + stride;
			input[dest]+=input[src];
		}
  }
}