package main;

import org.lwjgl.LWJGLException;

import visualize.FrameWork;
import convo.Blur;
import convo.EdgeDetection;

public class Main {
    
    public static void main(String[] args)
    {
        FrameWork app;
        app = new EdgeDetection();
        //app = new Blur();
        try {
            app.create();
        } catch (LWJGLException e) {
            e.printStackTrace();
            return;
        }
        app.run();
    }
}
