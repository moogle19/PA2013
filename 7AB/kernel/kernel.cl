kernel void max1(
	global int* vals, 
	const int stride, 
	const int count)
{
    int firstid = 2 * stride * get_global_id(0);
    int secondid = firstid + stride;
    vals[firstid] = max(vals[firstid], vals[secondid]);
}

kernel void max2(
    global int* vals,
    const int log2n
    )
{
	int id = get_local_id(0);
	int stride = 1;
	for(int i = 0; i < log2n; i++)
    {
		if(id % (2 * stride) == 0)
			vals[id] = max(vals[id], vals[id + stride]);
		barrier(CLK_GLOBAL_MEM_FENCE);
		stride *= 2;
	}
}

kernel void max3(
    global int* vals,
    const int valSize,
    local int* resultLoc
    )
{
    int gID = get_global_id(0);
    int wID = get_local_id(0);
    int wSize = get_local_size(0);
    int gSize = get_global_size(0);
    int grID = get_group_id(0);
    int grCnt = get_num_groups(0);

    int i;
    int workAmount  = valSize/grCnt;
    int startOffset = workAmount * grID + wID;

    if (gSize > valSize)
    {
        gSize = valSize;
    }
    
    resultLoc[wID] = 0;
    //gws / lws times
    for (i = startOffset; i < gSize; i += wSize)
    {
        resultLoc[wID] = max(resultLoc[wID], vals[i]);
    }
    barrier(CLK_LOCAL_MEM_FENCE);

    if (gID == 0)
    {
        //lws times
        for (i = 1; i < wSize; i++)
        {
            resultLoc[0] = max(resultLoc[0], resultLoc[i]);
        }
        vals[0] = resultLoc[0];
    }
}