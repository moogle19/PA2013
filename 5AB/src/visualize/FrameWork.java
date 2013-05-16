package visualize;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL31;
import org.lwjgl.util.vector.Vector3f;

import pa.cl.CLUtil;
import visualize.gl.Buffer.UniformBuffer;
import visualize.input.Input.InputAdapter;
import visualize.input.Input.InputHandler;
import visualize.util.Camera;
import visualize.util.Timer;

public abstract class FrameWork extends InputAdapter
{       
    public static class UniformBufferSlots
    {
        public static int CAMERA_BUFFER_SLOT = 0;
        public static int COLOR_BUFFER_SLOT  = 1;
        public static int MODEL_BUFFER_SLOT  = 2;
    }
    
    private static FrameWork m_sInstance;
    
    public static FrameWork instance()
    {
        return m_sInstance;
    }
    
    protected Camera m_camera;
    protected UniformBuffer m_cameraBuffer = new UniformBuffer(UniformBufferSlots.CAMERA_BUFFER_SLOT);
    protected UniformBuffer m_color = new UniformBuffer(UniformBufferSlots.COLOR_BUFFER_SLOT);
    protected InputHandler m_inputHandler;
    protected boolean m_done = false;
    protected boolean m_vsync;
    protected boolean m_enableCamera;
    protected int m_w;
    protected int m_h;
    protected boolean m_keys[];
    protected boolean m_mouseBtns[];
    protected Timer m_timer = new Timer();
    private String m_titel;
    
    private static final FloatBuffer CAMERA_BUFFER = BufferUtils.createFloatBuffer(32);
    public static final FloatBuffer MATRIX4X4_BUFFER = BufferUtils.createFloatBuffer(16);
    public static final FloatBuffer FLOAT4_BUFFER = BufferUtils.createFloatBuffer(4);
    
    public FrameWork(int w, int h, boolean enableCamera, boolean vsync)
    {
        this(w, h, enableCamera, false, "");
    }
    
    public FrameWork(int w, int h, boolean enableCamera, boolean vsync, String titel)
    {
        m_w = w;
        m_h = h;
        m_vsync = vsync;
        m_enableCamera = enableCamera;
        
        m_inputHandler = new InputAdapter();
        
        setInputHandler(this);
        
        m_timer.reset();
        
        m_titel = titel;
        
        m_sInstance = this;
    }
    
    public int getWidth()
    {
        return m_w;
    }
    
    public int getHeight()
    {
        return m_h;
    }
    
    public void setInputHandler(InputHandler handler)
    {
        m_inputHandler = handler;
    }
    
    public void uploadCameraBuffer()
    {   
        m_camera.getView().store(CAMERA_BUFFER);
        m_camera.getProjection().store(CAMERA_BUFFER);
        
        CAMERA_BUFFER.position(0);
        
        m_cameraBuffer.loadFloatData(CAMERA_BUFFER, GL15.GL_DYNAMIC_DRAW);
    }
    
    public void initGL()
    {
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        
        GL11.glEnable(GL31.GL_PRIMITIVE_RESTART);
        GL31.glPrimitiveRestartIndex(0xFFFFFFFF);
        
        m_keys = new boolean[Keyboard.getKeyCount()];
        m_mouseBtns = new boolean[Mouse.getButtonCount()];
        
        Display.setVSyncEnabled(m_vsync);
        
        m_cameraBuffer.create();
        
        m_color.create();
        
        FLOAT4_BUFFER.put(new float[]{1.0f, 1.0f, 1.0f, 1.0f});
        FLOAT4_BUFFER.position(0);

        m_color.loadFloatData(FLOAT4_BUFFER, GL15.GL_DYNAMIC_DRAW);
        
        uploadCameraBuffer();
        
        m_cameraBuffer.bindBufferBase();
        
        m_color.bindBufferBase();
        
        init();
    }
    
    public boolean create() throws LWJGLException
    {
        Display.setTitle(m_titel);
        Display.setDisplayMode(new DisplayMode(m_w, m_h));
        Display.create();
        Keyboard.create();
        Mouse.create();
        
        String infos[][] = new String[1][4]; 
        infos[0][0] = "Version: " + GL11.glGetString(GL11.GL_VERSION);
        infos[0][0] += ", Vendor: " + GL11.glGetString(GL11.GL_VENDOR);
        infos[0][1] = "Renderer: " + GL11.glGetString(GL11.GL_RENDERER);
        infos[0][2] = "Shading Language: " + GL11.glGetString(GL20.GL_SHADING_LANGUAGE_VERSION);
        infos[0][3] = "Driver Version: " + Display.getVersion();
        
        System.out.println(CLUtil.getFormattedInfoBox(infos, "+-OpenGL Info"));

        m_camera = new Camera(m_w, m_h);
        m_camera.lookAt(new Vector3f(0,0,-10), new Vector3f(0,0,0)); 
        //GLUtil.create();


        
        return true;
    }
    
    public abstract void init();
    
    public abstract void close();
    
    public abstract void render();
    
    public void run()
    {
        while(!(m_done || Display.isCloseRequested()))
        {
            updateInput();
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);            
            render();
            Display.update();
            m_timer.tick();
        }
        close();
        destroy();
    }
    
    public void destroy()
    {
        m_cameraBuffer.delete();
        m_color.delete();
        
        //GLUtil.destroy();
        Mouse.destroy();
        Keyboard.destroy();
        Display.destroy();
    }
    
    protected void updateInput()
    {
        Keyboard.poll();
        while(Keyboard.next()) 
        {
            if(Keyboard.isRepeatEvent()) 
            {
                m_inputHandler.processKeyRepeat(Keyboard.getEventKey());
            } else if(Keyboard.getEventKeyState()) 
            {
                m_inputHandler.processKeyPressed(Keyboard.getEventKey());
            } else 
            {
                m_inputHandler.processKeyUp(Keyboard.getEventKey());
            }
        }
        for(int i = 0; i < m_keys.length; ++i) 
        {
            if(Keyboard.isKeyDown(i)) 
            {
                m_inputHandler.processKeyDown(i);
            }
        }
        while(Mouse.next()) 
        {
            if(Mouse.getEventButton() != -1) 
            {
                m_mouseBtns[Mouse.getEventButton()] = Mouse.getEventButtonState();
                if(Mouse.getEventButtonState()) 
                {
                    m_inputHandler.processMouseButtonDown(Mouse.getX(), Mouse.getY(), Mouse.getEventButton());
                } else {
                    m_inputHandler.processMouseButtonUp(Mouse.getX(), Mouse.getY(), Mouse.getEventButton());
                } 
            } else if(Mouse.getEventDWheel() != 0) 
            {
                m_inputHandler.processMouseWheel(Mouse.getX(), Mouse.getY(), Mouse.getEventDWheel());
            } else 
            {
                for(int i = 0; i < m_mouseBtns.length; ++i) 
                {
                    if(m_mouseBtns[i]) 
                    {
                        int dx = Mouse.getDX();
                        int dy = Mouse.getDY();
                        m_inputHandler.processMouseDragged(Mouse.getX(), Mouse.getY(), dx, dy, i);
                    }
                    else
                    {
                        int dx = Mouse.getDX();
                        int dy = Mouse.getDY();
                        m_inputHandler.processMouseMoved(Mouse.getX(), Mouse.getY(), dx, dy);
                    }
                }
            }
        }
    }
    
    @Override
    public void processKeyDown(int key) 
    {
        if(!m_enableCamera)
        {
            return;
        }
        if(key == Keyboard.KEY_W || key == Keyboard.KEY_A || key == Keyboard.KEY_S || key == Keyboard.KEY_D || key == Keyboard.KEY_SPACE || key == Keyboard.KEY_C)
        {
            Vector3f move = new Vector3f();
            switch(key)
            {
            case Keyboard.KEY_W : move.z = 1; break;
            case Keyboard.KEY_S : move.z = -1; break;
            case Keyboard.KEY_A : move.x = -1; break;
            case Keyboard.KEY_D : move.x = 1; break;
            case Keyboard.KEY_C : move.y = -1; break;
            case Keyboard.KEY_SPACE : move.y = 1; break;
            }
            float scale = 0.5f * m_timer.getLastNanos() * 1e-7f;
            m_camera.move(new Vector3f(move.x * scale, move.y * scale, move.z * scale));
            uploadCameraBuffer();
        }
    }
    
    @Override
    public void processKeyPressed(int key)
    {
        if(key == Keyboard.KEY_ESCAPE)
        {
            m_done = true;
        }
    }
    
    @Override
    public void processMouseButtonDown(int x, int y, int button) 
    {
        if(button == 0) 
        {
            Mouse.setGrabbed(Mouse.getEventButtonState());
        }
    }

    @Override
    public void processMouseButtonUp(int x, int y, int button) 
    {
        if(button == 1)
        {
            Mouse.setGrabbed(Mouse.getEventButtonState());
        }
    }

    @Override
    public void processMouseMoved(int x, int y, int dx, int dy) 
    {
        if(m_enableCamera && Mouse.isGrabbed())
        {
            m_camera.rotate(2*dx * 1e-3f, -2*dy * 1e-3f);
            uploadCameraBuffer();
        }
    }
}
