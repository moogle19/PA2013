package main;

import java.nio.IntBuffer;
import java.util.Random;

import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CLCommandQueue;
import org.lwjgl.opencl.CLContext;
import org.lwjgl.opencl.CLKernel;
import org.lwjgl.opencl.CLMem;
import org.lwjgl.opencl.CLProgram;

import pa.cl.CLUtil;
import pa.cl.CLUtil.PlatformDevicePair;
import pa.cl.OpenCL;
import pa.util.IOUtil;

public class Aufgabe1Blatt9 {
    
    private static CLContext context;
    private static CLCommandQueue queue;
    private static CLProgram program;
    
    /**
     * our image size, lets keep it simple an only take data of size 2^n 
     */
    private static int width = 4;
    private static int height = 4;
    
    private static IntBuffer rayExists = BufferUtils.createIntBuffer(width * height);
    private static IntBuffer rayMemoryAdress = BufferUtils.createIntBuffer(width * height);
    
	private static CLMem clExists;
	private static CLMem clMemoryAdress;
	
	private static CLKernel arrangeNewRays;
    
	private static PointerBuffer gws;
	
    private static int arrangeRays(int raysCount)
    {   
        //TODO most stuff happens here
    	if(clExists != null) OpenCL.clReleaseMemObject(clExists);
    	if(clMemoryAdress != null) OpenCL.clReleaseMemObject(clMemoryAdress);
    	
    	clExists = OpenCL.clCreateBuffer(context, OpenCL.CL_MEM_READ_ONLY | OpenCL.CL_MEM_COPY_HOST_PTR, rayExists);
    	clMemoryAdress = OpenCL.clCreateBuffer(context, OpenCL.CL_MEM_READ_WRITE | OpenCL.CL_MEM_COPY_HOST_PTR, rayMemoryAdress);
    	
    	arrangeNewRays.setArg(0, clExists);
    	arrangeNewRays.setArg(1, clMemoryAdress);
    	arrangeNewRays.setArg(2, (int) (Math.log(raysCount)/ Math.log(2)));
//    	arrangeNewRays.setArg(3, );
    	
    	System.out.print("Old indices: 	");
    	pa.util.BufferHelper.printBuffer(rayMemoryAdress, 16);
    	
        OpenCL.clEnqueueNDRangeKernel(queue, arrangeNewRays, 1, null, gws, null, null, null);
        OpenCL.clEnqueueReadBuffer(queue, clMemoryAdress, OpenCL.CL_TRUE, 0, rayMemoryAdress, null, null);
    	
        System.out.print("Existing: 	");
        pa.util.BufferHelper.printBuffer(rayExists, 16);
        System.out.print("New indices:	");
        pa.util.BufferHelper.printBuffer(rayMemoryAdress, 16);
        System.out.println();
        System.out.println(raysCount);

        return 0; //returns the number of rays for the next iteration step
    }
    
    public static void main(String[] args)
    {
        CLUtil.createCL();
        
        PlatformDevicePair pair = CLUtil.choosePlatformAndDevice();

        context = OpenCL.clCreateContext(pair.platform, pair.device, null, null);
        queue = OpenCL.clCreateCommandQueue(context, pair.device, OpenCL.CL_QUEUE_PROFILING_ENABLE);
        program = OpenCL.clCreateProgramWithSource(context, IOUtil.readFileContent("kernel/kernel.cl"));
        OpenCL.clBuildProgram(program, pair.device, "", null);
        
        //TODO kernels, memory etc. here        
        arrangeNewRays = OpenCL.clCreateKernel(program, "arrangeNewRays");

        gws = BufferUtils.createPointerBuffer(1);
        gws.put(0, height * width);
        

        int raysCount = width * height;
        
        initRays(rayExists);
        initAdress(rayMemoryAdress);
        
        while(raysCount > 0)
        {
            //calculate hits and colors here, we ignore it but feel free to implement it ;)
            computeNewRays(rayExists);
            raysCount = arrangeRays(raysCount);
        }
        
        //TODO release stuff here
        OpenCL.clReleaseMemObject(clMemoryAdress);
        OpenCL.clReleaseMemObject(clExists);
        OpenCL.clReleaseKernel(arrangeNewRays);
        OpenCL.clReleaseCommandQueue(queue);
        OpenCL.clReleaseProgram(program);
        OpenCL.clReleaseContext(context);
        
        CLUtil.destroyCL();
    }
    
    //some helper
    
    private static interface Operation
    {
        void operate(IntBuffer data, int index);
    }
    
    private static class ComputeRays implements Operation
    {
        private static Random R = new Random(0);
        @Override
        public void operate(IntBuffer data, int index) {
            data.put(index, data.get(index) * R.nextInt(2));
        }
    }
    
    private static class InitRays implements Operation
    {
        @Override
        public void operate(IntBuffer data, int index) {
            data.put(index, 1);
        }
    }
    
    private static class InitIndex implements Operation
    {
        @Override
        public void operate(IntBuffer data, int index) {
            data.put(index, index);
        }
    }
    
    private static void doRays(IntBuffer rays, int max, Operation op)
    {
        for(int i = 0; i < max; ++i)
        {
            op.operate(rays, i);
        }
    }
    
    private static void computeNewRays(IntBuffer rayBool)
    {
        doRays(rayBool, rayBool.capacity(), new ComputeRays());
    }
    
    private static void initRays(IntBuffer rayBool)
    {
        doRays(rayBool, rayBool.capacity(), new InitRays());
    }
    
    private static void initAdress(IntBuffer memoryAdress)
    {
        doRays(memoryAdress, memoryAdress.capacity(), new InitIndex());
    }

    private static void download(CLMem clRays, IntBuffer data, int offset)
    {
        data.position(0);
        OpenCL.clEnqueueReadBuffer(queue, clRays, 1, offset, data, null, null);
    }
    
    private static void upload(CLMem clRays, IntBuffer data, int offset)
    {
        data.position(0);
        OpenCL.clEnqueueWriteBuffer(queue, clRays, 1, offset, data, null, null);
    }
    
    private static void fillZero(IntBuffer rays, int offset)
    {
        for(int i = offset; i < rays.capacity(); ++i)
        {
            rays.put(i, 0);
        }
    }
}
