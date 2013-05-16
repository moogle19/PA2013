
kernel void nbody(global float* p, global float* pNeu, global float* v, 
const float DELTA_T, const float EPSILON_SQUARED)  
{	
	int index = get_global_id(0);
	
	pNeu[index*3] = p[index*3];
	pNeu[index*3 + 1] = p[index*3 + 1];
	pNeu[index*3 + 2] = p[index*3 + 2];
	
	private float rx, ry, rz, Fx = 0, Fy = 0, Fz = 0, Fpow;
	for(int i = 0; i < get_global_size(0); i++) {
		float pw = 3/2;
		rx = -p[index*3] + p[i*3];
		ry = -p[index*3 + 1] + p[i*3 + 1];
		rz = -p[index*3 + 2] + p[i*3 + 2];
		
		Fpow = ((rx*rx+ry*ry+rz*rz)+EPSILON_SQUARED) * sqrt(((rx*rx+ry*ry+rz*rz)+EPSILON_SQUARED));
		//Fpow = powr( ((rx*rx+ry*ry+rz*rz)+EPSILON_SQUARED), pw);
		
		Fx += rx/Fpow;
		Fy += ry/Fpow;
		Fz += rz/Fpow;
	}
	v[index*3] += Fx * DELTA_T;
	v[index*3 + 1] += Fy * DELTA_T;
	v[index*3 + 2] += Fz * DELTA_T;
	
	
	pNeu[index*3] = p[index*3] + v[index*3] * DELTA_T;
	pNeu[index*3 + 1] = p[index*3 + 1] + v[index*3 + 1] * DELTA_T;
	pNeu[index*3 + 2] = p[index*3 + 2] + v[index*3 + 2] * DELTA_T;
	
	p[index*3] = pNeu[index*3];
	p[index*3 + 1] = pNeu[index*3 + 1];
	p[index*3 + 2] = pNeu[index*3 + 2];
}