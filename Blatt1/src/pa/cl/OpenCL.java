package pa.cl;

import static org.lwjgl.opencl.CL10.CL_QUEUE_PROFILING_ENABLE;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.CL10GL;
import org.lwjgl.opencl.CLBuildProgramCallback;
import org.lwjgl.opencl.CLCommandQueue;
import org.lwjgl.opencl.CLContext;
import org.lwjgl.opencl.CLContextCallback;
import org.lwjgl.opencl.CLDevice;
import org.lwjgl.opencl.CLKernel;
import org.lwjgl.opencl.CLMem;
import org.lwjgl.opencl.CLPlatform;
import org.lwjgl.opencl.CLProgram;
import org.lwjgl.opengl.Drawable;
import org.lwjgl.opengl.GL11;

/**
 * @author Henning Wenke
 * @author Sascha Kolodzey
 */
public class OpenCL 
{
    private static final IntBuffer lastErrorCode = BufferUtils.createIntBuffer(1);
    public static final boolean checkError = true;
    
    static
    {
        lastErrorCode.put(0, CL10.CL_SUCCESS);
    }

    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clCreateBuffer.html">clCreateBuffer</a>
     */
    public static CLMem clCreateBuffer(CLContext context, long flags, long host_ptr) 
    {
        CLMem mem = CL10.clCreateBuffer(context, flags, host_ptr, lastErrorCode);
        checkError();
        return mem;
    }
    
    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clCreateBuffer.html">clCreateBuffer</a>
     */
    public static CLMem clCreateBuffer(CLContext context, long flags, FloatBuffer host_ptr) 
    {
        CLMem mem = CL10.clCreateBuffer(context, flags, host_ptr, lastErrorCode);
        checkError();
        return mem;
    }
    
    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clCreateFromGLBuffer.html">clCreateFromGLBuffer</a>
     */
    public static CLMem clCreateFromGLBuffer(CLContext context, long flags, int bufobj) 
    {
        CLMem mem = CL10GL.clCreateFromGLBuffer(context, flags, bufobj, lastErrorCode);
        checkError();
        return mem;
    }
    
    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clCreateFromGLTexture2D.html">clCreateFromGLTexture2D</a>
     */
    public static CLMem clCreateFromGLTexture2D(CLContext context, long flags, int target, int mimaplevel, int texobj) 
    {
        CLMem mem = CL10GL.clCreateFromGLTexture2D(context, CL10.CL_MEM_READ_WRITE, GL11.GL_TEXTURE_2D, 0, texobj, lastErrorCode);
        checkError();
        return mem;
    }
    
    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clCreateBuffer.html">clCreateBuffer</a>
     */
    public static CLMem clCreateBuffer(CLContext context, long flags, ByteBuffer host_ptr) 
    {
        CLMem mem = CL10.clCreateBuffer(context, flags, host_ptr, lastErrorCode);
        checkError();
        return mem;
    }
    
    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clCreateBuffer.html">clCreateBuffer</a>
     */
    public static CLMem clCreateBuffer(CLContext context, long flags, IntBuffer host_ptr) 
    {
        CLMem mem = CL10.clCreateBuffer(context, flags, host_ptr, lastErrorCode);
        checkError();
        return mem;
    }

    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clCreateKernel.html">clCreateKernel</a>
     */
    public static CLKernel clCreateKernel(CLProgram program, String name) 
    {
        CLKernel kernel = CL10.clCreateKernel(program, name, lastErrorCode);
        checkError();
        return kernel;
    }
    
    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clCreateProgramWithSource.html">clCreateProgramWithSource</a>
     */
    public static CLProgram clCreateProgramWithSource(CLContext context, String source) 
    {
        CLProgram program = CL10.clCreateProgramWithSource(context, source, lastErrorCode);
        checkError();
        return program;
    }
    
    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clCreateCommandQueue.html">clCreateCommandQueue</a>
     */
    public static CLCommandQueue clCreateCommandQueue(CLContext context, CLDevice device, boolean profiling) 
    {
        CLCommandQueue queue = CL10.clCreateCommandQueue(context, device, profiling ? CL_QUEUE_PROFILING_ENABLE : 0, lastErrorCode);
        checkError();
        return queue;
    }
    
    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clEnqueueAcquireGLObjects.html">clEnqueueAcquireGLObjects</a>
     */
    public static void clEnqueueAcquireGLObjects(CLCommandQueue command_queue, CLMem mem_object, PointerBuffer event_wait_list, PointerBuffer event) 
    {
        lastErrorCode.put(0, CL10GL.clEnqueueAcquireGLObjects(command_queue, mem_object, event_wait_list, event));
        checkError();
    }
    
    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clEnqueueReleaseGLObjects.html">clEnqueueReleaseGLObjects</a>
     */
    public static void clEnqueueReleaseGLObjects(CLCommandQueue command_queue, CLMem mem_object, PointerBuffer event_wait_list, PointerBuffer event) 
    {
        lastErrorCode.put(0, CL10GL.clEnqueueReleaseGLObjects(command_queue, mem_object, event_wait_list, event));
        checkError();
    }
    
    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clReleaseKernel.html">clReleaseKernel</a>
     */
    public static void clReleaseKernel(CLKernel kernel) 
    {
        lastErrorCode.put(0, CL10.clReleaseKernel(kernel));
        checkError();
    }
    
    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clReleaseProgram.html">clReleaseProgram</a>
     */
    public static void clReleaseProgram(CLProgram program) 
    {
        lastErrorCode.put(0, CL10.clReleaseProgram(program));
        checkError();
    }
    
    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clReleaseCommandQueue.html">clReleaseCommandQueue</a>
     */
    public static void clReleaseCommandQueue(CLCommandQueue queue) 
    {
        lastErrorCode.put(0, CL10.clReleaseCommandQueue(queue));
        checkError();
    }
    
    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clReleaseContext.html">clReleaseContext</a>
     */
    public static void clReleaseContext(CLContext context) 
    {
        lastErrorCode.put(0, CL10.clReleaseContext(context));
        checkError();
    }
    
    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clReleaseMemObject.html">clReleaseMemObject</a>
     */
    public static void clReleaseMemObject(CLMem mem) 
    {
        CL10.clReleaseMemObject(mem);
    }

    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clEnqueueNDRangeKernel.html">clEnqueueNDRangeKernel</a>
     */
    public static void clEnqueueNDRangeKernel
    (
            CLCommandQueue command_queue, 
            CLKernel kernel, 
            int work_dim, 
            PointerBuffer global_work_offset, 
            PointerBuffer global_work_size, 
            PointerBuffer local_work_size, 
            PointerBuffer event_wait_list, 
            PointerBuffer event) 
    {
        lastErrorCode.put(0, 
                CL10.clEnqueueNDRangeKernel(
                        command_queue, 
                        kernel, 
                        work_dim, 
                        global_work_offset, 
                        global_work_size, 
                        local_work_size, 
                        event_wait_list, 
                        event));
        checkError();
    }
    
    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clFinish.html">clFinish</a>
     */
    public static void clFinish(CLCommandQueue command_queue) 
    {
        lastErrorCode.put(0, CL10.clFinish(command_queue));
        checkError();
    }
    
    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clFlush.html">clFlush</a>
     */
    public static void clFlush(CLCommandQueue command_queue) 
    {
        lastErrorCode.put(0, CL10.clFlush(command_queue));
        checkError();
    }
  
    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clBuildProgram.html">clBuildProgram</a>
     */
    public static void clBuildProgram(CLProgram program, CLDevice device, CharSequence options, CLBuildProgramCallback pfn_notify) 
    {
        lastErrorCode.put(0, CL10.clBuildProgram(program, device, "", null));
        CLUtil.checkProgram(program, device);
    }
    
    /**
     * @see http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clCreateContext.html
     */
    public static CLContext clCreateContext(CLPlatform platform, List<CLDevice> devices, CLContextCallback pfn_notify, Drawable share_drawable) throws LWJGLException 
    {
        CLContext context = CLContext.create(platform, devices, null, share_drawable, lastErrorCode);
        checkError();
        return context;
    }
    
    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clCreateCommandQueue.html">clCreateCommandQueue</a>
     */
    public static CLCommandQueue clCreateCommandQueue(CLContext context, CLDevice device, long properties) 
    {
        CLCommandQueue queue = CL10.clCreateCommandQueue(context, device, 0, lastErrorCode);
        checkError();
        return queue;
    }
    
    public static void checkError() 
    {
        checkError(lastErrorCode.get(0));
    }
    
    public static void checkError(int errorCode) 
    {
        if(checkError && errorCode != CL10.CL_SUCCESS) 
        {
            throw new RuntimeException(getErrorLog(errorCode));
        }
    }
    
    private static String getErrorLog(int errorCode) 
    {
        switch(errorCode) 
        {
        case 0xFFFFFFFF : return "CL_ERROR: CL_DEVICE_NOT_FOUND";
        case 0xFFFFFFFE : return "CL_ERROR: CL_DEVICE_NOT_AVAILABLE";
        case 0xFFFFFFFD : return "CL_ERROR: CL_COMPILER_NOT_AVAILABLE";
        case 0xFFFFFFFC : return "CL_ERROR: CL_MEM_OBJECT_ALLOCATION_FAILURE";
        case 0xFFFFFFFB : return "CL_ERROR: CL_OUT_OF_RESOURCES";
        case 0xFFFFFFFA : return "CL_ERROR: CL_OUT_OF_HOST_MEMORY";
        case 0xFFFFFFF9 : return "CL_ERROR: CL_PROFILING_INFO_NOT_AVAILABLE";
        case 0xFFFFFFF8 : return "CL_ERROR: CL_MEM_COPY_OVERLAP";
        case 0xFFFFFFF7 : return "CL_ERROR: CL_IMAGE_FORMAT_MISMATCH";
        case 0xFFFFFFF6 : return "CL_ERROR: CL_IMAGE_FORMAT_NOT_SUPPORTED";
        case 0xFFFFFFF5 : return "CL_ERROR: CL_BUILD_PROGRAM_FAILURE";
        case 0xFFFFFFF4 : return "CL_ERROR: CL_MAP_FAILURE";
        case 0xFFFFFFE2 : return "CL_ERROR: CL_INVALID_VALUE";
        case 0xFFFFFFE1 : return "CL_ERROR: CL_INVALID_DEVICE_TYPE";
        case 0xFFFFFFE0 : return "CL_ERROR: CL_INVALID_PLATFORM";
        case 0xFFFFFFDF : return "CL_ERROR: CL_INVALID_DEVICE";
        case 0xFFFFFFDE : return "CL_ERROR: CL_INVALID_CONTEXT";
        case 0xFFFFFFDD : return "CL_ERROR: CL_INVALID_QUEUE_PROPERTIES";
        case 0xFFFFFFDC : return "CL_ERROR: CL_INVALID_COMMAND_QUEUE";
        case 0xFFFFFFDB : return "CL_ERROR: CL_INVALID_HOST_PTR";
        case 0xFFFFFFDA : return "CL_ERROR: CL_INVALID_MEM_OBJECT";
        case 0xFFFFFFD9 : return "CL_ERROR: CL_INVALID_IMAGE_FORMAT_DESCRIPTOR";
        case 0xFFFFFFD8 : return "CL_ERROR: CL_INVALID_IMAGE_SIZE";
        case 0xFFFFFFD7 : return "CL_ERROR: CL_INVALID_SAMPLER";
        case 0xFFFFFFD6 : return "CL_ERROR: CL_INVALID_BINARY";
        case 0xFFFFFFD5 : return "CL_ERROR: CL_INVALID_BUILD_OPTIONS";
        case 0xFFFFFFD4 : return "CL_ERROR: CL_INVALID_PROGRAM";
        case 0xFFFFFFD3 : return "CL_ERROR: CL_INVALID_PROGRAM_EXECUTABLE";
        case 0xFFFFFFD2 : return "CL_ERROR: CL_INVALID_KERNEL_NAME";
        case 0xFFFFFFD1 : return "CL_ERROR: CL_INVALID_KERNEL_DEFINITION";
        case 0xFFFFFFD0 : return "CL_ERROR: CL_INVALID_KERNEL";
        case 0xFFFFFFCF : return "CL_ERROR: CL_INVALID_ARG_INDEX";
        case 0xFFFFFFCE : return "CL_ERROR: CL_INVALID_ARG_VALUE";
        case 0xFFFFFFCD : return "CL_ERROR: CL_INVALID_ARG_SIZE";
        case 0xFFFFFFCC : return "CL_ERROR: CL_INVALID_KERNEL_ARGS";
        case 0xFFFFFFCB : return "CL_ERROR: CL_INVALID_WORK_DIMENSION";
        case 0xFFFFFFCA : return "CL_ERROR: CL_INVALID_WORK_GROUP_SIZE";
        case 0xFFFFFFC9 : return "CL_ERROR: CL_INVALID_WORK_ITEM_SIZE";
        case 0xFFFFFFC8 : return "CL_ERROR: CL_INVALID_GLOBAL_OFFSET";
        case 0xFFFFFFC7 : return "CL_ERROR: CL_INVALID_EVENT_WAIT_LIST";
        case 0xFFFFFFC6 : return "CL_ERROR: CL_INVALID_EVENT";
        case 0xFFFFFFC5 : return "CL_ERROR: CL_INVALID_OPERATION";
        case 0xFFFFFFC4 : return "CL_ERROR: CL_INVALID_GL_OBJECT";
        case 0xFFFFFFC3 : return "CL_ERROR: CL_INVALID_BUFFER_SIZE";
        case 0xFFFFFFC2 : return "CL_ERROR: CL_INVALID_MIP_LEVEL";
        case 0xFFFFFFC1 : return "CL_ERROR: CL_INVALID_GLOBAL_WORK_SIZE";
        default : return "CL_ERROR: CL_UNKNOWN_ERROR";
        }
    }
}