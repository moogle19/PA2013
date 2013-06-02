package main;

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

public class Max3 
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
        kernel = OpenCL.clCreateKernel(program, "max3");
                
        int[] vals = new int[4096];
        
        for (int i = 0; i < vals.length; i++)
        {
            vals[i] = i;
        }
        vals[1240] = 31337;

        System.out.println();
        
        int newlength = vals.length;
        if(Integer.bitCount(newlength) > 1) {
        	newlength = Integer.highestOneBit(newlength) * 2;
        }
        int lwsize = newlength/64;
        
        System.out.println("Global work size: " + newlength);
        System.out.println("Local work size: " + lwsize);
        
        PointerBuffer gws = new PointerBuffer(1);
        gws.put(0, newlength);
        PointerBuffer lws = new PointerBuffer(1);
        lws.put(0, lwsize);
        IntBuffer valsBuff = BufferUtils.createIntBuffer(newlength);
        BufferUtils.zeroBuffer(valsBuff);
        valsBuff.put(vals);
        valsBuff.rewind();
                
        CLMem valsMem = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, valsBuff);
        
        clSetKernelArg(kernel, 0, valsMem);
        clSetKernelArg(kernel, 1, vals.length);
        kernel.setArgSize(2, newlength);

        clEnqueueNDRangeKernel(queue, kernel, 1, null, gws, lws, null, null);
        
        CL10.clEnqueueReadBuffer(queue, valsMem, CL10.CL_FALSE, 0, valsBuff, null, null);
        CL10.clFinish(queue);
        System.out.println("Maximum value: " + valsBuff.get(0));
        
        OpenCL.clReleaseKernel(kernel);
        OpenCL.clReleaseCommandQueue(queue);
        OpenCL.clReleaseProgram(program);
        OpenCL.clReleaseContext(context);
        
        CLUtil.destroyCL();
    }
}
