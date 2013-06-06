package convo;


import static pa.cl.OpenCL.clCreateBuffer;
import static pa.cl.OpenCL.clEnqueueNDRangeKernel;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.CLCommandQueue;
import org.lwjgl.opencl.CLContext;
import org.lwjgl.opencl.CLKernel;
import org.lwjgl.opencl.CLMem;
import org.lwjgl.opencl.CLProgram;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import pa.cl.CLUtil;
import pa.cl.CLUtil.PlatformDevicePair;
import pa.cl.OpenCL;
import pa.util.IOUtil;
import pa.util.IOUtil.TextureData;
import pa.util.SizeOf;
import visualize.gl.GLUtil;
import visualize.gl.Texture;

public class Blur extends BlurBasis
{    
    private Texture glTextureSrc;
    private Texture glTextureDst;
    private CLContext context;
    private CLCommandQueue queue;
    private CLProgram clProgram;
    private PlatformDevicePair pair;
    
    private FloatBuffer readBuffer;
    
    //you have to init und use four three objects 
    private CLMem sourceImage = null;
    private CLMem blurredImage = null;
    private CLKernel kernel = null;
    private PointerBuffer globalWorkSize;
    private CLMem convolutionMask = null;
    
    private static class Settings
    {
        public static int filterSize = 11;
        public static double sigma = 5;
        public static double sigmaDelta = 10.5;
        public static double filterSizeDelta = 2;
        public final static int MAX_FILTER_SIZE = 55;
        
        public static void print()
        {
            System.out.println(String.format("FilterSize=%d, Sigma=%f", Settings.filterSize, Settings.sigma));
        }
    }
    
    public Blur() 
    {
        super(1024, 768);
    }

    @Override
    public void render() 
    {
        //TODO kernel call
        clEnqueueNDRangeKernel(queue, kernel, 1, null, globalWorkSize, null, null, null);
        if(blurredImage != null) //this is here to make sure the app doesnt crash if everything isn't initialized, it can be removed later
        {
            OpenCL.clEnqueueReadBuffer(queue, blurredImage, 1, 0, readBuffer, null, null);
            glTextureDst.loadFloatData(readBuffer);   
        }
        
        GLUtil.transformScreenQuad(0, 0, getWidth(), getHeight()).bind();
        GLUtil.drawTexture(1);
        GLUtil.transformScreenQuad(0, 0, getWidth() / 3, getHeight() / 3).bind();
        GLUtil.drawTexture(0);
    }
    
    @Override
    public void init() 
    {    
        TextureData textureData = IOUtil.readTextureData("textures/brands.jpg");
        FloatBuffer rgbadata = Texture.createRGBAFromX(textureData);
        
        glTextureSrc = Texture.createRGBA2DTexture(textureData.w, textureData.h, 0, rgbadata);
        
        glTextureDst = Texture.createRGBA2DTexture(textureData.w, textureData.h, 1, rgbadata);

        GLUtil.transformScreenQuad(0, 0, getWidth(), getHeight()).bind();
        
        CLUtil.createCL();
        
        readBuffer = BufferUtils.createFloatBuffer(textureData.h * textureData.w * 4);
        
        pair = CLUtil.choosePlatformAndDevice();
        
        context = OpenCL.clCreateContext(pair.platform, pair.device, null, Display.getDrawable());
        
        queue = OpenCL.clCreateCommandQueue(context, pair.device, 0);
        
        clProgram = OpenCL.clCreateProgramWithSource(context, IOUtil.readFileContent("kernel/convolution.cl"));
        
        OpenCL.clBuildProgram(clProgram, pair.device, "", null);
        
        int imageWidth = textureData.w;
        int imageHeight = textureData.h;
        
        globalWorkSize = new PointerBuffer(1);
        globalWorkSize.put(0, imageWidth * imageHeight);
        
        int convSize = Settings.filterSize;
        convSize *= convSize;
        FloatBuffer convoBuffer = BufferUtils.createFloatBuffer(convSize+1);
        convoBuffer.put(this.getGaussianBlurConvMask(Settings.filterSize, Settings.sigma));
        
        sourceImage = clCreateBuffer(context, CL10.CL_MEM_READ_WRITE | CL10.CL_MEM_COPY_HOST_PTR, rgbadata);
        blurredImage = clCreateBuffer(context, CL10.CL_MEM_READ_WRITE | CL10.CL_MEM_COPY_HOST_PTR , readBuffer);
        convolutionMask = clCreateBuffer(context, CL10.CL_MEM_READ_WRITE | CL10.CL_MEM_COPY_HOST_PTR , convoBuffer);

        
        kernel = OpenCL.clCreateKernel(clProgram, "blurrr");
        
        pa.cl.OpenCL.clSetKernelArg(kernel, 0, sourceImage);
        pa.cl.OpenCL.clSetKernelArg(kernel, 1, blurredImage);
        pa.cl.OpenCL.clSetKernelArg(kernel, 2, imageWidth);
        pa.cl.OpenCL.clSetKernelArg(kernel, 3, imageHeight);
        pa.cl.OpenCL.clSetKernelArg(kernel, 4, convolutionMask);
        pa.cl.OpenCL.clSetKernelArg(kernel, 5, Settings.filterSize);

        //TODO create kernel and buffer
        
    }
    
    public float[] getGaussianBlurConvMask(int size, double sigma)
    {	
    	int newsize = size;
        float data[] = new float[newsize * newsize];
        int halfnewsize = ((int) (newsize-1)) / 2;
        System.out.println(halfnewsize);
        int index = 0;
        for(int i = -halfnewsize; i <= halfnewsize; i++) {
        	for(int j = -halfnewsize; j < halfnewsize; j++) {
        		data[index] = (float) ( 1/(Math.sqrt(2*Math.PI*sigma*sigma)*(Math.pow(Math.E, -((i*i+j*j)/2*sigma*sigma) ) ) ));
        		++index;
        	}
        }
        //TODO create a gaussian blur filter
        
        return data;
    }
    
    public void onSettingsChanged()
    {
        //TODO create the convo mask buffer, make sure to not create mem leaks. don't forget to set the new kernel args
        FloatBuffer convoBuffer = BufferUtils.createFloatBuffer(Settings.filterSize * Settings.filterSize + 1);
        convoBuffer.put(this.getGaussianBlurConvMask(Settings.filterSize, Settings.sigma));
        if(convolutionMask != null) {
        	OpenCL.clReleaseMemObject(convolutionMask);
        }
        convolutionMask = clCreateBuffer(context, CL10.CL_MEM_READ_WRITE | CL10.CL_MEM_COPY_HOST_PTR , convoBuffer);
        pa.cl.OpenCL.clSetKernelArg(kernel, 4, convolutionMask);
        pa.cl.OpenCL.clSetKernelArg(kernel, 5, Settings.filterSize);

        Settings.print();
    }

    @Override
    public void increaseMaskSize() {
        // TODO increase filterSize
        if(Settings.filterSize <= Settings.MAX_FILTER_SIZE - Settings.filterSizeDelta) {
        	Settings.filterSize += Settings.filterSizeDelta;
        }
        onSettingsChanged();
    }

    @Override
    public void decreaseMaskSize() {
        // TODO decrease filterSize
    	if(Settings.filterSize > Settings.filterSizeDelta) {
        	Settings.filterSize -= Settings.filterSizeDelta;
        }
        onSettingsChanged();
    }

    @Override
    public void increaseStandardDevation() {
        //TODO increase sigma
        Settings.sigma += Settings.sigmaDelta;
        onSettingsChanged();
    }

    @Override
    public void decreaseStandardDevation() {
        // TODO decrease sigma
        Settings.sigma -= Settings.sigmaDelta;
        onSettingsChanged();
    }
    
    @Override
    public void close() 
    {
        if(kernel != null)
        {
            OpenCL.clReleaseKernel(kernel);
        }
        
        if(blurredImage != null)
        {
            OpenCL.clReleaseMemObject(blurredImage);
        }
        
        if(sourceImage != null)
        {
            OpenCL.clReleaseMemObject(sourceImage);
        }
        
        if(convolutionMask != null)
        {
            OpenCL.clReleaseMemObject(convolutionMask);
        }

        OpenCL.clReleaseProgram(clProgram);
        OpenCL.clReleaseCommandQueue(queue);
        OpenCL.clReleaseContext(context);
        CLUtil.destroyCL();
        glTextureSrc.delete();
        glTextureDst.delete();
    }
}
