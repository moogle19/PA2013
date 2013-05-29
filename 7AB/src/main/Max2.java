package main;

import static org.lwjgl.opencl.CL10.CL_DEVICE_MAX_WORK_GROUP_SIZE;
import static pa.cl.OpenCL.CL_MEM_COPY_HOST_PTR;
import static pa.cl.OpenCL.CL_MEM_READ_WRITE;
import static pa.cl.OpenCL.clCreateBuffer;
import static pa.cl.OpenCL.clEnqueueNDRangeKernel;
import static pa.cl.OpenCL.clSetKernelArg;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.CLCommandQueue;
import org.lwjgl.opencl.CLContext;
import org.lwjgl.opencl.CLKernel;
import org.lwjgl.opencl.CLMem;
import org.lwjgl.opencl.CLProgram;

import pa.cl.CLUtil;
import pa.cl.CLUtil.PlatformDevicePair;
import pa.cl.OpenCL;
import pa.util.IOUtil;

public class Max2 
{
    private static CLContext context;
    private static CLKernel kernel;
    private static CLCommandQueue queue;
    private static CLProgram program;
    
    public static void main(String[] args)
    {
        CLUtil.createCL();
        
        PlatformDevicePair pair = CLUtil.choosePlatformAndDevice();

        context = OpenCL.clCreateContext(pair.platform, pair.device, null, null);
        queue = OpenCL.clCreateCommandQueue(context, pair.device, 0);
        program = OpenCL.clCreateProgramWithSource(context, IOUtil.readFileContent("kernel/kernel.cl"));
        OpenCL.clBuildProgram(program, pair.device, "", null);
        kernel = OpenCL.clCreateKernel(program, "max2");
        
        
        int[] vals = {5, 7, 10, 13 ,19 , 20, 6, 8, 100, 0, 0, 0, 0, 0, 0, 0}; //val = 40
        PointerBuffer gws_ValsCnt = new PointerBuffer(1);
        gws_ValsCnt.put(0, vals.length);
        IntBuffer valsBuff = BufferUtils.createIntBuffer(vals.length);
        valsBuff.put(vals);
        valsBuff.rewind();
        
        CLMem valsMem = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, valsBuff);
        
        if(vals.length > pair.device.getInfoInt(CL_DEVICE_MAX_WORK_GROUP_SIZE) * 2) {
        	System.out.println("Too many values! max. " + (pair.device.getInfoInt(CL_DEVICE_MAX_WORK_GROUP_SIZE) * 2) + "!!!");
        	return;
        }
        
        int log2n = (int) (Math.log(vals.length) / Math.log(2));
        
        clSetKernelArg(kernel, 0, valsMem);
        clSetKernelArg(kernel, 1, log2n);
        
        clEnqueueNDRangeKernel(queue, kernel, 1, null, gws_ValsCnt, gws_ValsCnt, null, null);
        
        CL10.clEnqueueReadBuffer(queue, valsMem, CL10.CL_FALSE, 0, valsBuff, null, null);
        System.out.println("Value: "+valsBuff.get(0));
        //do stuff here
        
        OpenCL.clReleaseKernel(kernel);
        OpenCL.clReleaseCommandQueue(queue);
        OpenCL.clReleaseProgram(program);
        OpenCL.clReleaseContext(context);
        
        CLUtil.destroyCL();
    }
}