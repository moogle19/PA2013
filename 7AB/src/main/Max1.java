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

public class Max1 
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
        kernel = OpenCL.clCreateKernel(program, "max1");
        
        int[] vals = {5, 7, 1, 3 ,9, 2, 6, 18, 2, 3, -10};
        
        System.out.print("numbers: ");
        for (int i = 0; i < vals.length; i++)
        {
            System.out.print(vals[i] + " ");
        }
        System.out.println();
        
        int newlength = vals.length;
        //do stuff here
        if(Integer.bitCount(newlength) > 1) {
        	newlength = Integer.highestOneBit(newlength) * 2;
        }
        int stride = 1;
        int threadCnt = newlength/2;
        PointerBuffer gws_ValsCnt = new PointerBuffer(1);
        gws_ValsCnt.put(0, newlength/2);
        IntBuffer valsBuff = BufferUtils.createIntBuffer(newlength);
        BufferUtils.zeroBuffer(valsBuff);
        valsBuff.put(vals);
        valsBuff.rewind();
        
        CLMem valsMem = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, valsBuff);
        
        clSetKernelArg(kernel, 0, valsMem);
        clSetKernelArg(kernel, 1, stride);
        clSetKernelArg(kernel, 2, newlength);
        
        int log2n = (int) (Math.log(newlength) / Math.log(2));
        
        System.out.println((log2n+1) + " kernel calls");
        
        for(int i = 0; i < log2n; i++) {
            clEnqueueNDRangeKernel(queue, kernel, 1, null, gws_ValsCnt, null, null, null);
            clSetKernelArg(kernel, 1, stride*=2);
            clSetKernelArg(kernel, 2, threadCnt /= 2);
            gws_ValsCnt.put(0, gws_ValsCnt.get(0)/2);
        }
        
        CL10.clEnqueueReadBuffer(queue, valsMem, CL10.CL_FALSE, 0, valsBuff, null, null);
        CL10.clFinish(queue);
        System.out.println("Value: " + valsBuff.get(0));
        
        OpenCL.clReleaseKernel(kernel);
        OpenCL.clReleaseCommandQueue(queue);
        OpenCL.clReleaseProgram(program);
        OpenCL.clReleaseContext(context);
        
        CLUtil.destroyCL();
    }
}
