package visualize.gl;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL33;

import pa.util.SizeOf;
import visualize.gl.Buffer.IndexBuffer;
import visualize.gl.Buffer.VertexBuffer;

public class GeometryFactory 
{    
    public static class VertexPointer
    {
        protected int m_index;
        protected int m_offset;
        protected int m_elementCount;
        protected boolean m_isInstanced;
        
        public VertexPointer(int index, int elementCount, int offset)
        {
            this(index, elementCount, offset, false);
        }
        
        public VertexPointer(int index, int elementCount, int offset, boolean isInstanced)
        {
            m_index = index;
            m_offset = offset;
            m_elementCount = elementCount;
            m_isInstanced = isInstanced;
        }
    }
    
    public static interface GeometryCreator
    {
        public Geometry CreateGeometry();
    }
    
    public static interface Drawer
    {
        void Draw(int topo, int elements, int instances, int offset);
    }
    
    public static class ElementDrawer implements Drawer
    {
        public void Draw(int topo, int elements, int instances, int offset)
        {
            GL11.glDrawElements(topo, elements, GL11.GL_UNSIGNED_INT, offset);
        }
    }
    
    public static class ArrayDrawer implements Drawer
    {
        public void Draw(int topo, int elements, int instances, int offset)
        {
            GL11.glDrawArrays(topo, offset, elements);
        }
    }
    
    public static class ElementInstancedDrawer implements Drawer
    {
        public void Draw(int topo, int elements, int instances, int offset)
        {
            GL31.glDrawElementsInstanced(topo, elements, GL11.GL_UNSIGNED_INT, offset, instances);
        }
    }
    
    public static class ArrayInstancedDrawer implements Drawer
    {
        public void Draw(int topo, int elements, int instances, int offset)
        {
            GL31.glDrawArraysInstanced(topo, offset, elements, instances);
        }
    }

    public static class Geometry
    {
        protected static final Drawer g_ELEMENT_DRAWER = new ElementDrawer();
        protected static final Drawer g_ELEMENT_INSTANCED_DRAWER = new ElementInstancedDrawer();
        protected static final Drawer g_ARRAY_DRAWER = new ArrayDrawer();
        protected static final Drawer g_ARRAY_INSTANCED_DRAWER = new ArrayInstancedDrawer();
        
        protected IndexBuffer m_indexBuffer;
        protected VertexBuffer m_vertexBuffer;
        protected List<VertexBuffer> m_instanceBuffers = new ArrayList<VertexBuffer>();
        protected int m_array;
        protected Drawer m_drawer;
        
        private int m_instances = 0;
        
        private int m_elements = 0;
        
        private int m_topology;
        
        public Geometry()
        {
            m_indexBuffer = null;
            m_vertexBuffer = null;
            m_array = -1;
        }
        
        public void create(float vertices[], int vertexBufferUsage, int indices[], int indexBufferUsage, int topology, VertexPointer... pointers)
        {
            if(m_array == -1)
            {
                m_array = GL30.glGenVertexArrays();
            }
            
            m_topology = topology;
            
            FloatBuffer verts = BufferUtils.createFloatBuffer(vertices.length);
            verts.put(vertices);
            verts.position(0);
            
            /*
            if(instances != null)
            {
                if(m_instanceBuffer == null)
                {
                    m_instanceBuffer = new VertexBuffer();
                }
                
                FloatBuffer _b = BufferUtils.createFloatBuffer(instances.length);
                _b.put(instances);
                _b.flip();
                m_instanceBuffer.create();
                m_instanceBuffer.loadFloatData(_b, vertexBufferUsage);
                m_drawer = g_INSTANCED_DRAWER;
            } */
            
            GL30.glBindVertexArray(m_array);
            
            if(m_vertexBuffer == null)
            {
                m_vertexBuffer = new VertexBuffer();   
            }
            m_vertexBuffer.create();
            m_vertexBuffer.loadFloatData(verts, vertexBufferUsage);
            
            if(indices != null)
            {
                IntBuffer inds = BufferUtils.createIntBuffer(indices.length);
                inds.put(indices);
                inds.position(0);
                
                if(m_indexBuffer == null)
                {
                    m_indexBuffer = new IndexBuffer();   
                }
                m_indexBuffer.m_elements = indices.length;
                m_indexBuffer.create();
                m_indexBuffer.loadIntData(inds, indexBufferUsage);
                m_elements = m_indexBuffer.m_elements;
                
                inds = null;
            }
            
            int stride = 0;
            for(int i = 0; i < pointers.length; ++i)
            {
                stride += pointers[i].m_elementCount;
            }
            
            stride *= SizeOf.FLOAT;

            for(int i = 0; i < pointers.length; ++i)
            {
                VertexPointer pointer = pointers[i];
                GL20.glEnableVertexAttribArray(pointer.m_index);
                GL20.glVertexAttribPointer(pointer.m_index, pointer.m_elementCount, GL11.GL_FLOAT, false, stride, pointer.m_offset);
            }
            
            /*if(instances != null)
            {
                VertexPointer pointer = new VertexPointer(3, instanceElementSize, 0);
                stride = pointer.m_elementCount * SizeOf.FLOAT;
                m_instanceBuffer.bind();
                GL20.glEnableVertexAttribArray(pointer.m_index);
                GL33.glVertexAttribDivisor(pointer.m_index, 1);
                GL20.glVertexAttribPointer(pointer.m_index, pointer.m_elementCount, GL11.GL_FLOAT, false, stride, pointer.m_offset);
                
                m_instances = m_instanceBuffer.m_size / pointer.m_elementCount;
            } */

            verts = null;

            GL30.glBindVertexArray(0);
            
            if(m_indexBuffer != null)
            {
                m_drawer = g_ELEMENT_DRAWER;
            }
            else
            {
                m_drawer = g_ARRAY_DRAWER;
                m_elements = vertices.length / (stride / SizeOf.FLOAT);
            }
        }
        
        public void addInstanceBuffer(float instances[], int instanceElementSize, int position, int usage)
        {
            VertexBuffer instanceBuffer = new VertexBuffer();
            
            FloatBuffer _b = BufferUtils.createFloatBuffer(instances.length);
            _b.put(instances);
            _b.flip();
            instanceBuffer.create();
            instanceBuffer.loadFloatData(_b, usage);
            
            GL30.glBindVertexArray(m_array);
            VertexPointer pointer = new VertexPointer(position, instanceElementSize, 0);
            int stride = pointer.m_elementCount * SizeOf.FLOAT;
            instanceBuffer.bind();
            GL20.glEnableVertexAttribArray(pointer.m_index);
            GL33.glVertexAttribDivisor(pointer.m_index, 1);
            GL20.glVertexAttribPointer(pointer.m_index, pointer.m_elementCount, GL11.GL_FLOAT, false, stride, pointer.m_offset);
            
            m_instances = instanceBuffer.m_size / pointer.m_elementCount;
            GL30.glBindVertexArray(0);
            
            m_instanceBuffers.add(instanceBuffer);
            
            if(m_indexBuffer != null)
            {
                m_drawer = g_ELEMENT_INSTANCED_DRAWER;
            }
            else
            {
                m_drawer = g_ARRAY_INSTANCED_DRAWER;
            }
        }
        
        public void bind()
        {
            GL30.glBindVertexArray(m_array);
        }
        
        public void draw()
        {
            bind();
            m_drawer.Draw(m_topology, m_elements, m_instances, 0);
        }
        
        public void draw(int offset)
        {
            bind();
            m_drawer.Draw(m_topology, m_elements, m_instances, offset);
        }
        
        public VertexBuffer getVertexBuffer()
        {
            return m_vertexBuffer;
        }
        
        public VertexBuffer getInstanceBuffer(int pos)
        {
            return m_instanceBuffers.get(pos);
        }
        
        public void delete()
        {
           if(m_vertexBuffer != null)
           {
               m_vertexBuffer.delete();
           }
           
           if(m_indexBuffer != null)
           {
               m_indexBuffer.delete();
           }
           
           for(VertexBuffer buffer : m_instanceBuffers)
           {
               buffer.delete();
           }
           
           if(m_array != -1)
           {
               GL30.glDeleteVertexArrays(m_array);
           }
        }
    }
    
    public static Geometry createScreenQuad()
    {
        Geometry geo = new Geometry();
        
        float vertices[] = 
            {
                -1, -1, 0, 0, 1,
                +1, -1, 0, 1, 1,
                -1, +1, 0, 0, 0,
                +1, +1, 0, 1, 0,
            };
        
        int indices[] = {0,1,2,3};

        geo.create(vertices, GL15.GL_STATIC_DRAW, indices, GL15.GL_STATIC_DRAW, GL11.GL_TRIANGLE_STRIP, new VertexPointer(0, 3, 0), new VertexPointer(1, 2, 12));

        return geo;
    }
    
    public static Geometry createDynamicScreenQuad()
    {
        Geometry geo = new Geometry();
        
        float vertices[] = 
            {
                -1, -1, 0, 0, 1,
                +1, -1, 0, 1, 1,
                -1, +1, 0, 0, 0,
                +1, +1, 0, 1, 0,
            };
        
        int indices[] = {0,1,2,3};

        geo.create(vertices, GL15.GL_DYNAMIC_DRAW, indices, GL15.GL_STATIC_DRAW, GL11.GL_TRIANGLE_STRIP, new VertexPointer(0, 3, 0), new VertexPointer(1, 2, 12));

        return geo;
    }
    public static Geometry createCube()
    {
        return createCube(null, 0);
    }
    
    public static Geometry createParticles(float[] instances, float size, int stride)
    {
        Geometry geo = new Geometry();
        size /= 2.0f;
        
        float vertices[] = 
            {
                -size, -size, 0, 0, 1, 0, 0, 1,
                +size, -size, 0, 0, 1, 0, 1, 1,
                -size, +size, 0, 0, 1, 0, 0, 0,
                +size, +size, 0, 0, 1, 0, 1, 0,
            };
        
        int indices[] = {0,1,2,3};

        geo.create(vertices, GL15.GL_STATIC_DRAW, indices, GL15.GL_STATIC_DRAW, GL11.GL_TRIANGLE_STRIP, 
                new VertexPointer(0, 3, 0), new VertexPointer(1, 3, 12), new VertexPointer(2, 2, 24));
        
        geo.addInstanceBuffer(instances, stride, 3, GL15.GL_DYNAMIC_DRAW);
        return geo; 
    }
    
    public static Geometry createCube(float[] instances, int instanceElementSize)
    {
        Geometry geo = new Geometry();

        float vertices[] = 
        {
            //vorne
            -1, -1, -1, /**/0, 0, -1, /**/ 0, 1,
            -1, +1, -1, /**/0, 0, -1, /**/ 0, 0,
            +1, +1, -1, /**/0, 0, -1, /**/ 1, 0,
            +1, -1, -1, /**/0, 0, -1, /**/ 1, 1,

            //hinten
            -1, -1, +1, /**/0, 0, +1, /**/ 1, 0,
            -1, +1, +1, /**/0, 0, +1, /**/ 1, 1,
            +1, +1, +1, /**/0, 0, +1, /**/ 0, 1,
            +1, -1, +1, /**/0, 0, +1, /**/ 0, 0,

            //links
            -1, -1, -1, /**/-1, 0, 0, /**/ 1, 0, //8
            -1, -1, +1, /**/-1, 0, 0, /**/ 0, 0, //9
            -1, +1, -1, /**/-1, 0, 0, /**/ 1, 1, //10
            -1, +1, +1, /**/-1, 0, 0, /**/ 0, 1, //11

            //rechts
            +1, -1, -1, /**/+1, 0, 0, /**/ 0, 0, //12
            +1, -1, +1, /**/+1, 0, 0, /**/ 1, 0, //13
            +1, +1, -1, /**/+1, 0, 0, /**/ 0, 1, //14
            +1, +1, +1, /**/+1, 0, 0, /**/ 1, 1, //15

            //oben
            +1, +1, +1, /**/0, +1, 0, /**/ 1, 1, //16
            +1, +1, -1, /**/0, +1, 0, /**/ 1, 0, //17
            -1, +1, +1, /**/0, +1, 0, /**/ 0, 1, //18
            -1, +1, -1, /**/0, +1, 0, /**/ 0, 0, //19

            //unten
            +1, -1, +1, /**/0, -1, 0, /**/ 0, 1, //20
            +1, -1, -1, /**/0, -1, 0, /**/ 0, 0, //21
            -1, -1, +1, /**/0, -1, 0, /**/ 1, 1, //22
            -1, -1, -1, /**/0, -1, 0, /**/ 1, 0, //23
        };

        int indices[] = 
        {
            3,2,0,1,-1,
            5,6,4,7,-1,
            8,10,9,11,-1,
            13,15,12,14,-1,
            17,16,19,18,-1,
            20,21,22,23,-1
        };
        
        geo.create(vertices, GL15.GL_STATIC_DRAW, indices, GL15.GL_STATIC_DRAW, GL11.GL_TRIANGLE_STRIP, 
                new VertexPointer(0, 3, 0), new VertexPointer(1, 3, 12), new VertexPointer(2, 2, 24));
        geo.addInstanceBuffer(instances, 3, 3, GL15.GL_DYNAMIC_DRAW);
        return geo;
    }
}
