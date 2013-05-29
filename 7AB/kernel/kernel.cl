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
){
	int id = get_local_id(0);
	int stride = 1;
	for(int i = 0; i < log2n; i++){
		if(id % (2 * stride) == 0)
			vals[id] = max(vals[id], vals[id + stride]);
		barrier(CLK_GLOBAL_MEM_FENCE);
		stride *= 2;
	}
}