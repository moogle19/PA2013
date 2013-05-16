package visualize.gl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;

public class Texture 
{
    public static class TextureDescription
    {
        public int internalFormat;
        public int format;
        public int target;
        public int type = GL11.GL_FLOAT;
        public int width;
        public int height;
        public int depth;
        public boolean genMipMap = false;
    }
    
    private int m_id;
    private int m_unit;
    private TextureDescription m_desc;

    public Texture(int unit)
    {
        m_id = -1;
        m_unit = unit;
    }
    
    public void bind()
    {
        GL11.glBindTexture(m_desc.target, m_id);
    }
    
    public void activate()
    {
        bind();
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + m_unit);
    }
    
    public void create(TextureDescription desc)
    {
        delete();
        m_id = GL11.glGenTextures();
        m_desc = desc;
    }
    
    public void loadFloatData(FloatBuffer data)
    {
        bind();
        switch(m_desc.target)
        {
        case GL11.GL_TEXTURE_2D:
        {
            GL11.glTexImage2D(m_desc.target, 0, m_desc.internalFormat, m_desc.width, m_desc.height, 0, m_desc.format, m_desc.type, data);
            GL30.glGenerateMipmap(m_desc.target);   
        } break;
        default : throw new IllegalArgumentException();
        }
    }
    
    public int getId()
    {
        return m_id;
    }
    
    public void delete()
    {
        if(m_id != -1)
        {
            GL11.glDeleteTextures(m_id);
        }
    }
    
    public static Texture create2DTexture(int format, int internalFormat, int w, int h, int unit, FloatBuffer data)
    {
        TextureDescription desc = new TextureDescription();
        desc.target = GL11.GL_TEXTURE_2D;
        desc.type = GL11.GL_FLOAT;
        desc.format = format;
        desc.internalFormat = internalFormat;
        desc.width = w;
        desc.height = h;
        Texture t = new Texture(unit);
        t.create(desc);
        t.loadFloatData(data);
        return t;
    }
    
    public static Texture createRGBA16F2DTexture(int w, int h, int unit, FloatBuffer data)
    {
        return create2DTexture(GL11.GL_RGBA, GL30.GL_RGBA16F, w, h, unit, data);
    }
    
    public static Texture createFromFile(String path, int unit)
    {
        File file = new File(path);
        
        BufferedImage image;
        
        try 
        {
            image = ImageIO.read(file);
        } catch (IOException e) 
        {
            throw new IllegalArgumentException(e.getMessage());
        }
        
        int w = image.getWidth();
        int h = image.getHeight();
        
        int color[] = new int[image.getColorModel().getComponentSize().length];
        FloatBuffer data = BufferUtils.createFloatBuffer(w * h * color.length);

        for(int y = 0; y < h; ++y) 
        {
            for(int x = 0; x < w; ++x) 
            {
                image.getRaster().getPixel(x, y, color);
                for(int i = 0; i < color.length; ++i)
                {
                    data.put(color[i] / 255.0f);
                }
            }
        }
        
        data.position(0);
        
        int format = 0;
        int internalFormat = 0;
        switch(image.getColorModel().getComponentSize().length) 
        {
            case 1: internalFormat = GL30.GL_R8; format = GL11.GL_RED; break;
            case 2: internalFormat = GL30.GL_RG8; format = GL30.GL_RG; break;
            case 3: internalFormat = GL11.GL_RGB8; format = GL11.GL_RGB; break;
            case 4: internalFormat = GL11.GL_RGBA8; format = GL11.GL_RGBA; break;
        }
        
        return create2DTexture(format, internalFormat, w, h, unit, data);
    }
}
