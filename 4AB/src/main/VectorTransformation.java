package main;

import static cl.OpenCL.*;

import java.nio.IntBuffer;

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
import util.SizeOf;

/**
 *  Beispiel für Matrix-Vektor Multiplikation, analog zur Vorlesung. Berechnet das das Produkt einmal mit Java und dann mit OpenCL
 */
public class VectorTransformation 
{    
	// Liefert 1D Row-Major-Order-Index des Elements ij einer
	//  Matrix mit n Spalten
    public static int getIndexRowMO(int i, int j, int n)
    {
        return i * n + j;
    }   
    
    // Berechnet und schreibt Komponente i des m x 1 Vektors c, mit c = A * b.
    // Dabei ist A eine m x n Matrix und b ein n x 1 Vektor
    public static void calcMatVecProductComponent(int[] A, int[] b, int[] c, final int m, int i)
    {
        int sum = 0;
        for(int k = 0; k < m; ++k)
        {
            sum += A[getIndexRowMO(i, k, m)] * b[k];
        }
        c[i] = sum;
    } 

    public static void main(String[] args) 
    {
        int m = 3, n = 2;       
        int[] A_Java = new int[m * n], b_Java = new int[n], c_Java = new int[m];  
        IntBuffer A_Host = BufferUtils.createIntBuffer(m * n);
        IntBuffer b_Host = BufferUtils.createIntBuffer(n);
        IntBuffer c_Host = BufferUtils.createIntBuffer(m);
        
        A_Java[0] = 1; A_Java[1] = 2;
        A_Java[2] = 3; A_Java[3] = 4;
        A_Java[4] = 1; A_Java[5] = 0;       

        b_Java[0] = 2;
        b_Java[1] = 5;
        
        c_Java[0] = 0;
        c_Java[0] = 0;

        A_Host.put(A_Java);
        A_Host.rewind();
        b_Host.put(b_Java);
        b_Host.rewind();
        c_Host.put(c_Java);
        c_Host.rewind();

        /*
         * Berechnung mit java
         */
        for(int i = 0; i < m; ++i)
        {
            calcMatVecProductComponent(A_Java, b_Java, c_Java, n, i);
        }   
        System.out.println("Berechnung des Produkts mit Java");
        BufferHelper.printArray(A_Java, n);
        System.out.println("\n" + "*" + "\n");
        BufferHelper.printArray(b_Java, 1);
        System.out.println("\n" + "=" + "\n");
        BufferHelper.printArray(c_Java, 1);

        System.out.println();
        
        /*
         * Berechnung mit OpenCL
         */
        System.out.println("Berechnung des Produkts mit OpenCL");
        CLUtil.createCL();
        
        CLUtil.printPlatformInfos();
        
        PlatformDevicePair pair = CLUtil.choosePlatformAndDevice();
        
        String source = IOUtil.readFileContent("programs/VectorTransformation.cl");
        
        CLContext context = clCreateContext(pair.platform, pair.device, null, null);
        
        CLCommandQueue queue = clCreateCommandQueue(context, pair.device, 0);
        
        CLProgram program = clCreateProgramWithSource(context, source);
        
        clBuildProgram(program, pair.device, "", null);

        CLKernel kernel = clCreateKernel(program, "calcMatVecProductComponent");
        CLMem A = clCreateBuffer(context, CL_MEM_COPY_HOST_PTR | CL_MEM_READ_ONLY, A_Host);   
        CLMem b = clCreateBuffer(context, CL_MEM_COPY_HOST_PTR | CL_MEM_READ_ONLY, b_Host);
        CLMem c = clCreateBuffer(context, 0, c_Host.capacity() * SizeOf.INTEGER);        
        
        int indexDim = 1;
        PointerBuffer gws = new PointerBuffer(indexDim);
        gws.put(0, m);
        
        clSetKernelArg(kernel, 0, A);
        clSetKernelArg(kernel, 1, b);
        clSetKernelArg(kernel, 2, c);
        clSetKernelArg(kernel, 3, n);

        clEnqueueNDRangeKernel(queue, kernel, indexDim, null, gws, null, null, null);
        
        clEnqueueReadBuffer(queue, c, CL_FALSE, 0, c_Host, null, null);       
        clFinish(queue);

        clReleaseCommandQueue(queue);
        clReleaseKernel(kernel);
        clReleaseProgram(program);
        clReleaseMemObject(A);
        clReleaseMemObject(b);
        clReleaseMemObject(c);
        clReleaseContext(context);
       
        CLUtil.destroyCL();
    }
}
