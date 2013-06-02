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
import pa.cl.OpenCL;
import pa.cl.CLUtil.PlatformDevicePair;
import pa.util.IOUtil;

import static pa.cl.OpenCL.*;

import pa.util.BufferHelper;
import pa.util.math.MathUtil;
import pa.util.SizeOf;

public class MatMulRed 
{
        private static CLKernel kernelMatProducts, kernelSumRed;
    
        /**
         * @param args
         */
        public static void main(String[] args)
        {
            int a_z = 4, a_s = 4;
            int b_z = 4, b_s = 4;
            
            if (a_s != b_z)
                throw new RuntimeException("Wrong dimensions. Cannot multiply matrices.");
            
            int[] A_Java = new int[a_z * a_s];
            int[] B_Java = new int[b_z * b_s];
            int[] C_Java = new int[a_z * b_s];
            
            IntBuffer A_Host = BufferUtils.createIntBuffer(a_z * a_s);
            IntBuffer B_Host = BufferUtils.createIntBuffer(b_z * b_s);
            IntBuffer C_Host = BufferUtils.createIntBuffer(a_z * b_s * a_s);
            
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
            String source = IOUtil.readFileContent("kernel/kernel.cl");       
            CLContext context = clCreateContext(pair.platform, pair.device, null, null);        
            CLCommandQueue queue = clCreateCommandQueue(context, pair.device, 0);        
            CLProgram program = clCreateProgramWithSource(context, source);        
            clBuildProgram(program, pair.device, "", null);

            kernelMatProducts = clCreateKernel(program, "calcMatProducts");
            kernelSumRed = clCreateKernel(program, "sumMat");
            CLMem A = clCreateBuffer(context, CL_MEM_COPY_HOST_PTR | CL_MEM_READ_ONLY, A_Host);   
            CLMem B = clCreateBuffer(context, CL_MEM_COPY_HOST_PTR | CL_MEM_READ_ONLY, B_Host);
            CLMem C = clCreateBuffer(context, 0, C_Host.capacity() * SizeOf.INTEGER);

            int indexDim = 2;
            PointerBuffer gws = new PointerBuffer(indexDim);
            
            gws.put(0, a_z);
            gws.put(1, b_s);
                
            clSetKernelArg(kernelMatProducts, 0, A);
            clSetKernelArg(kernelMatProducts, 1, B);
            clSetKernelArg(kernelMatProducts, 2, C);
            clSetKernelArg(kernelMatProducts, 3, a_z);
            clSetKernelArg(kernelMatProducts, 4, a_s);
            clSetKernelArg(kernelMatProducts, 5, b_s);

            clEnqueueNDRangeKernel(queue, kernelMatProducts, indexDim, null, gws, null, null, null);
            
            clEnqueueReadBuffer(queue, C, CL_FALSE, 0, C_Host, null, null);       
            clFinish(queue);

            BufferHelper.printBuffer(A_Host, a_s, "A: ");
            BufferHelper.printBuffer(B_Host, b_s, "B: ");
            BufferHelper.printBuffer(C_Host, a_z * b_s * a_s, "C: ");
            C_Host.rewind();
            
            
            int newlength = a_s;
            if(Integer.bitCount(newlength) > 1) {
                newlength = Integer.highestOneBit(newlength) * 2;
            }
            int lwsize = newlength/2;
            
            System.out.println("Global work size: " + newlength);
            System.out.println("Local work size: " + lwsize);
            
            PointerBuffer gws2 = new PointerBuffer(1);
            gws2.put(0, newlength);
            PointerBuffer lws = new PointerBuffer(1);
            lws.put(0, lwsize);
            IntBuffer valsBuff = BufferUtils.createIntBuffer(a_z * b_s * a_s);
            IntBuffer resBuff = BufferUtils.createIntBuffer(a_z*b_s);            
            BufferUtils.zeroBuffer(valsBuff);
            valsBuff.put(C_Host);
            valsBuff.rewind();
            
            CLMem valsMem   = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, valsBuff);
            CLMem resultMem = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, resBuff);

            clSetKernelArg(kernelSumRed, 0, valsMem);
            clSetKernelArg(kernelSumRed, 1, resultMem);
            clSetKernelArg(kernelSumRed, 2, newlength);
            kernelSumRed.setArgSize(3, newlength);

            for (int i = 0; i < (a_z*b_s); i++)
            {
                clSetKernelArg(kernelSumRed, 4, i);
                clEnqueueNDRangeKernel(queue, kernelSumRed, 1, null, gws2, lws, null, null);   
            }
            
            CL10.clEnqueueReadBuffer(queue, resultMem, CL10.CL_FALSE, 0, resBuff, null, null);
            CL10.clFinish(queue);
            
            resBuff.rewind();
            BufferHelper.printBuffer(resBuff, b_s, "RESULT: ");
            
            OpenCL.clReleaseKernel(kernelSumRed);
            OpenCL.clReleaseCommandQueue(queue);
            OpenCL.clReleaseProgram(program);
            OpenCL.clReleaseContext(context);
            
            clReleaseCommandQueue(queue);
            clReleaseKernel(kernelMatProducts);
            clReleaseProgram(program);
            clReleaseMemObject(A);
            clReleaseMemObject(B);
            clReleaseMemObject(C);
            clReleaseContext(context);
            
            CLUtil.destroyCL();
            
        }
}
