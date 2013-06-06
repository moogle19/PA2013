
constant float a[5] = {1,2,3,4,5}; //global constant array
constant float sx[9] = {1, 0, -1, 2, 0, -2, 1, 0, -1};
constant float sy[9] = {1, 2, -1, 0, 0, 0, -1, -2, -1};

kernel void edges(
	global float4* sourceImage, 
	global float4* destImage,
	const int imageWidth,
	const int imageHeight)
{	
   	int id = get_global_id(0);
   	
   	float4 resh = sourceImage[id-imageWidth-1];
   	resh += 2 * sourceImage[id-imageWidth];
   	resh += sourceImage[id-imageWidth+1];
	resh -= sourceImage[id+imageWidth-1];
	resh -= 2 * sourceImage[id+imageWidth];
	resh -= sourceImage[id+imageWidth+1];
	
	float4 resv = sourceImage[id-imageWidth-1];
	resv -= sourceImage[id-imageWidth+1];
	resv += 2 * sourceImage[id-1];
	resv -= 2 * sourceImage[id+1];
	resv += sourceImage[id+imageWidth-1];
	resv -= sourceImage[id+imageWidth-1];
	
	float4 res = sqrt(resh*resh+resv*resv);
	destImage[id] = res;
	
}

kernel void blurrr(global float4* sourceImage, 
	global float4* destImage,
	const int imageWidth,
	const int imageHeight,
	global float4* convolutionMask,
	const int maskSize
)
{
	int id = get_global_id(0);
	int offsetx = -((maskSize-1) / 2);
	int offsety = -((maskSize-1) / 2);
	
	float4 res = 0;
	
	for(int i = 0; i < maskSize; i++) {
		for(int j = 0; j < maskSize; j++) {
			res += sourceImage[id+offsety*imageWidth+offsetx];
			offsety++;
		}
		offsetx++;
		offsety = -((maskSize-1) / 2);
		
	}
	res /= maskSize * maskSize;
	destImage[id] = res;
}