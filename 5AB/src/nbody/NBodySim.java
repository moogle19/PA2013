package nbody;



import static pa.cl.OpenCL.*;

import java.nio.FloatBuffer;

import nbody.helper.NBodyData;
import nbody.helper.NBodyVisualizer;

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

import pa.cl.CLUtil;
import pa.cl.CLUtil.PlatformDevicePair;
import pa.util.IOUtil;

public class NBodySim 
{
    private NBodyVisualizer vis;
    private PlatformDevicePair pair;
    private CLKernel kernel, kernel2;
    private CLProgram program;
    private CLCommandQueue queue;
    private CLContext context;
    private PointerBuffer globalWorkSize = new PointerBuffer(1);
    private int numBodys = 15360;
    private long time;
    private boolean toggle = false;
    private CLMem V, positions[];

    public void init()
    {	
        vis = new NBodyVisualizer(1024, 768);
        try 
        {
            vis.create();
        } catch (LWJGLException e) 
        {
            throw new RuntimeException(e.getMessage());
        }
        
        CLUtil.createCL();
        pair = CLUtil.choosePlatformAndDevice();
        context = clCreateContext(pair.platform, pair.device, null, Display.getDrawable());
        queue = clCreateCommandQueue(context, pair.device, 0);
        program = clCreateProgramWithSource(context, IOUtil.readFileContent("kernel/nbody.cl"));
        clBuildProgram(program, pair.device, "", null);
        kernel = clCreateKernel(program, "nbody");
        kernel2 = clCreateKernel(program, "nbody");
        vis.initGL();
        
        globalWorkSize.put(0, numBodys);
        
        //The Visualizer needs to know the kernel and queue 
        //because he has to use some OpenCL API calls to let 
        //opengl and opencl communicate
        vis.setKernelAndQueue(kernel, queue);
        
        float p[] = new float[numBodys * 3];
        float v[] = new float[numBodys * 3];
        
        //creates initial positions and velocities, and stores them in p / v
        NBodyData.createBodys(numBodys, vis, p, v);
        
        //creates two opengl buffers and returns
        //an opencl view on these
        //these objects can be used to modify positions in a kernel
        //we use two to be doublebuffered
        positions = vis.createPositions(p, context);
        
        FloatBuffer V_Host = BufferUtils.createFloatBuffer(v.length);
        V_Host.put(v);
        V_Host.rewind();
        V = CL10.clCreateBuffer(context, CL_MEM_COPY_HOST_PTR, V_Host, null);
        
        //TODO: what value for epsilon?
        float eps = 1.0f;
        
        CL10.clSetKernelArg(kernel, 0, positions[0]);
        CL10.clSetKernelArg(kernel, 1, positions[1]);
        CL10.clSetKernelArg(kernel, 2, V);
        clSetKernelArg(kernel, 4, eps);
        
        CL10.clSetKernelArg(kernel2, 1, positions[0]);
        CL10.clSetKernelArg(kernel2, 0, positions[1]);
        CL10.clSetKernelArg(kernel2, 2, V);
        clSetKernelArg(kernel2, 4, eps);
    }
    
    public void run()
    {
        init();
        time = System.currentTimeMillis();
        while(!vis.isDone())
        {            
            //simulate here
            //TODO: switch pos0 and pos1
            if (toggle)
            {
                clSetKernelArg(kernel2, 3, (float)(System.currentTimeMillis() - time)/10000);
                clEnqueueNDRangeKernel(queue, kernel2, 1, null, globalWorkSize, null, null, null);            
            }
            else
            {
                clSetKernelArg(kernel, 3, (float)(System.currentTimeMillis() - time)/10000);
            	clEnqueueNDRangeKernel(queue, kernel, 1, null, globalWorkSize, null, null, null);
            }
            
            toggle = !toggle;
            	
            clFinish(queue);
            time = System.currentTimeMillis();
            //render here
            vis.visualize();
            
        }
        close();
    }
    
    public void close()
    {
        //clean up here
        //TODO: clean up
        if(positions != null)
        {
            clReleaseMemObject(V);
            positions = null;
        }
        
        if(V != null)
        {
            clReleaseMemObject(V);
            V = null;
        }
        
        if(kernel2 != null)
        {
            clReleaseKernel(kernel2);
            kernel2 = null;
        }
        
        if(kernel != null)
        {
            clReleaseKernel(kernel);
            kernel = null;
        }
        
        if(program != null)
        {
            clReleaseProgram(program);
            program = null;
        }
        
        if(queue != null)
        {
            clReleaseCommandQueue(queue);
            queue = null;
        }
        
        if(context != null)
        {
            clReleaseContext(context);
            context = null;
        }
        
        CLUtil.destroyCL();
        
        vis.close();
    }
}
