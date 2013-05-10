/**
 * 
 */
package main;

import static cl.OpenCL.*;

import java.nio.IntBuffer;
import java.util.Random;

import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CLCommandQueue;
import org.lwjgl.opencl.CLContext;
import org.lwjgl.opencl.CLKernel;
import org.lwjgl.opencl.CLMem;
import org.lwjgl.opencl.CLProgram;

import cl.CLUtil;
import cl.CLUtil.PlatformDevicePair;

import util.BufferHelper;
import util.IOUtil;
import util.MathUtil;
import util.SizeOf;

/**
 * @author Valentin Bruder
 * @author Kevin Seidel
 * @date 10.05.2013
 *
 */
public class MatrixMultiplication
{

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        int a_m = 3, a_n = 4;
        int b_m = 4, b_n = 5;
        
        if (a_n != b_m)
            throw new RuntimeException("Wrong dimensions. Cannot multiply matrices.");
        
        int[] A_Java = new int[a_m * a_n];
        int[] B_Java = new int[b_m * b_n];
        int[] C_Java = new int[a_m * b_n];
        
        IntBuffer A_Host = BufferUtils.createIntBuffer(a_m * a_n);
        IntBuffer B_Host = BufferUtils.createIntBuffer(b_m * b_n);
        IntBuffer C_Host = BufferUtils.createIntBuffer(a_m * b_n);
        
        for (int i = 0; i < A_Java.length; i++)
        {
            A_Java[i] = MathUtil.nextInt() /100000000;
        }
        
        for (int i = 0; i < B_Java.length; i++)
        {
            B_Java[i] = MathUtil.nextInt() /100000000;
        }
        
        for (int i = 0; i < C_Java.length; i++)
        {
            C_Java[i] = 0;
        }

        A_Host.put(A_Java);
        A_Host.rewind();
        B_Host.put(B_Java);
        B_Host.rewind();
        C_Host.put(C_Java);
        C_Host.rewind();
        
        CLUtil.createCL();        
        CLUtil.printPlatformInfos();        
        PlatformDevicePair pair = CLUtil.choosePlatformAndDevice();        
        String source = IOUtil.readFileContent("programs/MatrixMul.cl");       
        CLContext context = clCreateContext(pair.platform, pair.device, null, null);        
        CLCommandQueue queue = clCreateCommandQueue(context, pair.device, 0);        
        CLProgram program = clCreateProgramWithSource(context, source);        
        clBuildProgram(program, pair.device, "", null);

        CLKernel kernel = clCreateKernel(program, "calcMatrixProduct");
        CLMem A = clCreateBuffer(context, CL_MEM_COPY_HOST_PTR | CL_MEM_READ_ONLY, A_Host);   
        CLMem B = clCreateBuffer(context, CL_MEM_COPY_HOST_PTR | CL_MEM_READ_ONLY, B_Host);
        CLMem C = clCreateBuffer(context, 0, C_Host.capacity() * SizeOf.INTEGER);

        int indexDim = 2;
        PointerBuffer gws = new PointerBuffer(indexDim);
        if (a_m >= b_n)
        {
            gws.put(0, a_m);
            gws.put(1, b_n);
            clSetKernelArg(kernel, 3, a_m);
        }
        else
        {
            gws.put(1, a_m);
            gws.put(0, b_n);
            clSetKernelArg(kernel, 3, b_n);
        }
            
        clSetKernelArg(kernel, 0, A);
        clSetKernelArg(kernel, 1, B);
        clSetKernelArg(kernel, 2, C);       
        clSetKernelArg(kernel, 4, a_n);
        clSetKernelArg(kernel, 5, b_n);

        clEnqueueNDRangeKernel(queue, kernel, indexDim, null, gws, null, null, null);
        
        clEnqueueReadBuffer(queue, C, CL_FALSE, 0, C_Host, null, null);       
        clFinish(queue);

        clReleaseCommandQueue(queue);
        clReleaseKernel(kernel);
        clReleaseProgram(program);
        clReleaseMemObject(A);
        clReleaseMemObject(B);
        clReleaseMemObject(C);
        clReleaseContext(context);
       
        CLUtil.destroyCL();
        
        BufferHelper.printBuffer(A_Host, a_n, "A: ");
        BufferHelper.printBuffer(B_Host, b_n, "B: ");
        BufferHelper.printBuffer(C_Host, b_n, "C: ");
    }

}
