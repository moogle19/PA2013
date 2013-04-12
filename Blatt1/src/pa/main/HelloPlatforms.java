package pa.main;

import org.lwjgl.LWJGLException;
import org.lwjgl.opencl.CL;

import pa.cl.CLUtil;


public class HelloPlatforms 
{
    public static void main(String[] args) 
    {
        try 
        {
            CL.create();
            CLUtil.printPlatformInfos();
            CL.destroy();
        } catch (LWJGLException e) 
        {
            e.printStackTrace();
        }
    }
}
