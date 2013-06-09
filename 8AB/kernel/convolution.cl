
constant float a[5] = {1,2,3,4,5}; //global constant array
constant float sx[9] = {1, 0, -1, 2, 0, -2, 1, 0, -1};
constant float sy[9] = {1, 2, 1, 0, 0, 0, -1, -2, -1};
constant int masksize = 9;
constant int masksizesq = 3;

kernel void edges(
	global float4* sourceImage, 
	global float4* destImage,
	const int imageWidth,
	const int imageHeight)
{	
   	int id = get_global_id(0);
   	
   	float4 resh = {0,0,0,0};
   	float4 resv = {0,0,0,0};
   	float4 resImage[9];
   	
   	
   	int index = 0;
   	
   	//load values in array
   	for(int i = -((masksizesq-1)/2); i <= (masksizesq-1)/2; i++) {
   		for(int j = -(3-1)/2; j <= (3-1)/2; j++) {
   			resImage[index] = sourceImage[id+i*imageWidth-j];
   			index++;
   		}
   	}
   	
   	for(int i = 0; i < masksize; i++) {
   		resh += sy[i] * resImage[i];
   	}
   	for(int i = 0; i < masksize; i++) {
   		resv += sx[i] * resImage[i];
   	}
   	
	float4 res = sqrt(resh*resh+resv*resv);
	destImage[id] = res;
	
	
}

kernel void blurrr(global float4* sourceImage, 
	global float4* destImage,
	global float* convolutionMask,
	const int imageWidth,
	const int imageHeight,
	int maskSize
)
{
	int id = get_global_id(0);
	int offsetx = -((maskSize-1) / 2);
	int offsety = -((maskSize-1) / 2);

	float4 res = 0;
	float sizeMask = 0;

	for(int i = 0; i < maskSize; i++) {
		for(int j = 0; j < maskSize; j++) {
			int idnew = id+offsety*imageWidth+offsetx;
			if(idnew >= 0 && idnew < imageWidth*imageHeight) {
				res += convolutionMask[j*maskSize+i] * sourceImage[idnew];
				sizeMask += convolutionMask[i*maskSize+j];
			}
			offsety++;
		}
		offsetx++;
		offsety = -((maskSize-1) / 2);

	}
	res /= sizeMask;
	destImage[id] = res;
}