package convo;


import static pa.cl.OpenCL.CL_MEM_COPY_HOST_PTR;
import static pa.cl.OpenCL.CL_MEM_READ_WRITE;
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
import visualize.FrameWork;
import visualize.gl.GLUtil;
import visualize.gl.Texture;

public class EdgeDetection extends FrameWork
{
    private Texture glTextureSrc;
    private Texture glTextureDst;
    private CLContext context;
    private CLCommandQueue queue;
    private CLProgram clProgram;

    private PlatformDevicePair pair;
    
    private FloatBuffer readBuffer;
    private FloatBuffer pixel;
    
    //you have to init und use these three objects 
    private CLMem sourceImage;
    private CLMem edgedImage;
    private CLKernel kernel;
    private PointerBuffer globalWorkSize;
    
    public EdgeDetection() 
    {
        super(1024, 768, false, false);
    }

    @Override
    public void render() 
    {	        
    	//TODO kernel call
        clEnqueueNDRangeKernel(queue, kernel, 1, null, globalWorkSize, null, null, null);
        if(edgedImage != null) //this is here to make sure the app doesnt crash if everything isn't initialized, it can be removed later
        {
            OpenCL.clEnqueueReadBuffer(queue, edgedImage, 1, 0, readBuffer, null, null);
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
        TextureData textureData = IOUtil.readTextureData("textures/valve.png");
                
        pixel = Texture.createRGBAFromX(textureData);
        
        glTextureSrc = Texture.createRGBA2DTexture(textureData.w, textureData.h, 0, pixel);
        
        glTextureDst = Texture.createRGBA2DTexture(textureData.w, textureData.h, 1, pixel);

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
        
        sourceImage = clCreateBuffer(context, CL10.CL_MEM_READ_WRITE | CL10.CL_MEM_COPY_HOST_PTR, pixel);
        edgedImage = clCreateBuffer(context, CL10.CL_MEM_READ_WRITE | CL10.CL_MEM_COPY_HOST_PTR , readBuffer);
        
        kernel = OpenCL.clCreateKernel(clProgram, "edges");
        
        pa.cl.OpenCL.clSetKernelArg(kernel, 0, sourceImage);
        pa.cl.OpenCL.clSetKernelArg(kernel, 1, edgedImage);
        pa.cl.OpenCL.clSetKernelArg(kernel, 2, imageWidth);
        pa.cl.OpenCL.clSetKernelArg(kernel, 3, imageHeight);
        
        //TODO create kernel and buffer

    }

    @Override
    public void close() 
    {
        if(kernel != null)
        {
            OpenCL.clReleaseKernel(kernel);
        }
        
        if(edgedImage != null)
        {
            OpenCL.clReleaseMemObject(edgedImage);
        }
        
        if(sourceImage != null)
        {
            OpenCL.clReleaseMemObject(sourceImage);
        }

        OpenCL.clReleaseProgram(clProgram);
        OpenCL.clReleaseCommandQueue(queue);
        OpenCL.clReleaseContext(context);
        CLUtil.destroyCL();
        glTextureSrc.delete();
        glTextureDst.delete();
    }
}
