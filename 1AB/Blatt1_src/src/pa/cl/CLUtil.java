package pa.cl;

import static org.lwjgl.opencl.CL10.CL_DEVICE_TYPE_CPU;
import static org.lwjgl.opencl.CL10.CL_DEVICE_TYPE_GPU;
import static org.lwjgl.opencl.CL10.CL_PLATFORM_NAME;
import static org.lwjgl.opencl.CL10.CL_PLATFORM_PROFILE;
import static org.lwjgl.opencl.CL10.CL_PLATFORM_VENDOR;
import static org.lwjgl.opencl.CL10.CL_PLATFORM_VERSION;

import java.nio.ByteBuffer;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.CLDevice;
import org.lwjgl.opencl.CLPlatform;
import org.lwjgl.opencl.CLProgram;

public class CLUtil 
{
    public static String[] getPlatformInfo(CLPlatform platform) 
    {
        String[] lines = new String[3];
        
        lines[0] = String.format("Version: %s", platform.getInfoString(CL_PLATFORM_VERSION)) 
                 + String.format(", Name: %s", platform.getInfoString(CL_PLATFORM_NAME));
        lines[1] = String.format(String.format("Vendor: %s", platform.getInfoString(CL_PLATFORM_VENDOR)) 
                 + String.format(", Profile: %s", platform.getInfoString(CL_PLATFORM_PROFILE)));
        
        List<CLDevice> d = platform.getDevices(CL_DEVICE_TYPE_GPU);
        String s = String.format("GPU Devices: %s", d != null ? d.size() : 0);
        d = platform.getDevices(CL_DEVICE_TYPE_CPU);
        lines[2] = s+String.format(", CPU Devices: %s", d != null ? d.size() : 0);
        return lines;
    }
    
    public static void printPlatformInfos()
    {
        int longestString = 0;
        String[][] lines;
        int platformcount = CLPlatform.getPlatforms().size();
        
        if(platformcount != 0) 
        {
            lines = new String[platformcount][3];
            for(int i = 0; i < platformcount; ++i) 
            {
                lines[i] = getPlatformInfo(CLPlatform.getPlatforms().get(i));
            }
        } else 
        {
            lines = new String[1][1];
            lines[0][0] = "####No OpenCL Platform found####";
        }
        
        for(int i = 0; i < lines.length; ++i) 
        {
            String[] plfs = lines[i];
            for(String line : plfs) 
            {
                int l = line.length();
                longestString = l > longestString ? l : longestString;
            }
        }
        
        String info = "+-OpenCL Platform(s)";
        for(int i = 0; i < longestString-17; ++i) 
        {
            info += "-";
        }
        info += "+\n";
        for(int i = 0; i < lines.length; ++i) 
        {
            String[] plfs = lines[i];
            if(i > 0) 
            {
                info += "|";
                for(int k = 0; k < longestString+2; ++k) 
                {
                    info += "*";
                } 
                info += "|\n";
            }
            for(String line : plfs) 
            {
                info += "| ";
                info += line;
                for(int j = 0; j < (1+longestString - line.length()); ++j) 
                {
                    info += " ";
                }
                info += "|\n";
            }
        }
        info += "+";
        for(int i = 0; i < longestString+2; ++i) 
        {
            info += "-";
        }
        info += "+";
        
        System.out.println(info);
    }
    
    public static void checkProgram(CLProgram program, CLDevice device) 
    {
        PointerBuffer buffer = BufferUtils.createPointerBuffer(1);
        CL10.clGetProgramBuildInfo(program, device, CL10.CL_PROGRAM_BUILD_LOG, null, buffer);
        if(buffer.get(0) > 2) 
        {
            ByteBuffer log = BufferUtils.createByteBuffer((int)buffer.get(0));
            CL10.clGetProgramBuildInfo(program, device, CL10.CL_PROGRAM_BUILD_LOG, log, buffer);
            byte bytes[] = new byte[log.capacity()];
            log.get(bytes);
            System.out.println(String.format("CL Compiler Error/Warning:\n %s", new String(bytes)));
        }
    }
}
