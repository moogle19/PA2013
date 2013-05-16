import org.lwjgl.opencl.CL;
import static org.lwjgl.opencl.CL10.*;
import java.nio.IntBuffer;
import java.util.List;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CLCommandQueue;
import org.lwjgl.opencl.CLContext;
import org.lwjgl.opencl.CLDevice;
import org.lwjgl.opencl.CLKernel;
import org.lwjgl.opencl.CLMem;
import org.lwjgl.opencl.CLPlatform;
import org.lwjgl.opencl.CLProgram;

public class HelloOpenCL {	
   private static final int SIZE_OF_INT = 4; // Größe des Datentyps int in Bytes
  
   public static void main(String[] args) throws Exception {	
		
      CL.create();    // Initialisiere OpenCL	

      /* Vorlesung 2013-04-17, Folie 17       */
      List<CLPlatform> platforms = CLPlatform.getPlatforms();
      CLPlatform platform = platforms.get(0);
      
   
      /* Vorlesung 2013-04-17, Folie 19       */
      List<CLDevice> devices = platform.getDevices(CL_DEVICE_TYPE_GPU); // Modifiziert: (ALL statt CPU)
      CLDevice device = devices.get(0);   
      
      
      /* Vorlesung 2013-04-17, Folie  21      */
      CLContext context = CLContext.create(platform, devices, null);
      
      
      /* Vorlesung 2013-04-17, Folie  24 & 25 */    
      int tmp[] = new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
      IntBuffer hostA = BufferUtils.createIntBuffer(tmp.length);
      hostA.put(tmp);
      hostA.rewind();      
      CLMem a = clCreateBuffer(context, CL_MEM_COPY_HOST_PTR | CL_MEM_READ_ONLY, hostA, null);
      
      tmp = new int[] {10, 9, 8, 7, 6, 5, 4, 3, 2, 1};
      IntBuffer hostB = BufferUtils.createIntBuffer(tmp.length);
      hostB.put(tmp);
      hostB.rewind();  
      CLMem b = clCreateBuffer(context, CL_MEM_COPY_HOST_PTR | CL_MEM_READ_ONLY, hostB, null); // Analog...
      
      tmp = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
      IntBuffer cHost = BufferUtils.createIntBuffer(tmp.length);
      cHost.put(tmp);
      cHost.rewind();
      CLMem c = clCreateBuffer(context, 0, cHost.capacity() * SIZE_OF_INT, null);
      

      /* Vorlesung 2013-04-24, Folie  5        */ 
      String programSource = "kernel void vec_add(          \n" 
                           + " global int *a,             \n"
                           + " global int *b,             \n"
                           + " global int *c) {           \n"
                           + "    int i = get_global_id(0); \n"
                           + "    c[i] = a[i] + b[i];       \n" 
                           + "}                             \n";    
      CLProgram program = clCreateProgramWithSource(context, programSource, null); 
      
      
      /* Vorlesung 2013-04-24, Folie  7        */ 
      clBuildProgram(program, device, "", null); 
      
      
      /* Vorlesung 2013-04-24, Folie  9        */
      CLKernel kernel = clCreateKernel(program, "vec_add", null); 
      

      /* Vorlesung 2013-04-24, Folie  11       */
      clSetKernelArg(kernel, 0, a); 
      clSetKernelArg(kernel, 1, b); // Analog...
      clSetKernelArg(kernel, 2, c); 
      

      /* Vorlesung 2013-04-24, Folie  13       */
      CLCommandQueue queue = clCreateCommandQueue(context, device, 0, null);


      /* Vorlesung 2013-04-24, Folie  15       */    
      int indexDim = 1;
      PointerBuffer compCnt = new PointerBuffer(indexDim);
      compCnt.put(0, hostA.capacity());
      clEnqueueNDRangeKernel(queue, kernel, indexDim, null, compCnt, null, null, null);
      

      /* Vorlesung 2013-04-24, Folie  18       */ 
      clEnqueueReadBuffer(queue, c, CL_FALSE, 0, cHost, null, null);
      
      
      /* Vorlesung 2013-04-24, Folie  19       */
      clFinish(queue);
      

      /* Ausgabe des Ergebnisses               */
      for(int i=0; i < hostA.capacity(); i++){
    	  System.out.println(cHost.get(i)+" = "+hostA.get(i)+" + "+hostB.get(i));
      }
      
    
      /* Ressourcen, in umgekehrter Abhängigkeitsreihenfolge, freigeben */
      clReleaseCommandQueue(queue);
      clReleaseKernel(kernel);
      clReleaseProgram(program);
      clReleaseMemObject(a);
      clReleaseMemObject(b);
      clReleaseMemObject(c);
      clReleaseContext(context);
      
      CL.destroy();      
   }
}
