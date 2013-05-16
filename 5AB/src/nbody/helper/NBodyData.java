package nbody.helper;

import pa.util.math.MathUtil;


public class NBodyData 
{
    private static final int RAND_MAX = 0x7FFF;
    
    protected static class float3
    {
        public float x, y, z;
    }
    
    protected static float rand()
    {
        return MathUtil.nextFloat(RAND_MAX);
    }
    
    protected static float normalize(float3 vector)
    {
        float dist = (float)Math.sqrt(vector.x*vector.x + vector.y*vector.y + vector.z*vector.z);
        if (dist > 1e-6)
        {
            vector.x /= dist;
            vector.y /= dist;
            vector.z /= dist;
        }
        return dist;
    }

    protected static float dot(float3 v0, float3 v1)
    {
        return v0.x*v1.x+v0.y*v1.y+v0.z*v1.z;
    }

    protected static float3 cross(float3 v0, float3 v1)
    {
        float3 rt = new float3();
        rt.x = v0.y*v1.z-v0.z*v1.y;
        rt.y = v0.z*v1.x-v0.x*v1.z;
        rt.z = v0.x*v1.y-v0.y*v1.x; 
        return rt;
    }

    public static void createBodys(int numBodies, NBodyVisualizer vis, float[] pos, float[] velos)
    {
        float clusterScale = vis.m_currentParams.m_clusterScale;
        float velocityScale = vis.m_currentParams.m_velocityScale;

        float scale = clusterScale;
        float vscale = scale * velocityScale;
        float inner = 2.5f * scale;
        float outer = 4.0f * scale;

        int p = 0, v = 0;
        int i = 0;
        while (i < numBodies)
        {
            float x, y, z;
            x = rand() / (float) RAND_MAX * 2 - 1;
            y = rand() / (float) RAND_MAX * 2 - 1;
            z = rand() / (float) RAND_MAX * 2 - 1;

            float3 point = new float3();
            point.x = x; point.y = y; point.z = z;
            float len = normalize(point);
            if (len > 1)
                continue;

            pos[p++] =  point.x * (inner + (outer - inner) * rand() / (float) RAND_MAX);
            pos[p++] =  point.y * (inner + (outer - inner) * rand() / (float) RAND_MAX);
            pos[p++] =  point.z * (inner + (outer - inner) * rand() / (float) RAND_MAX);
            //pos[p++] = 1.0f;

            x = 0f * (rand() / (float) RAND_MAX * 2 - 1);
            y = 0f * (rand() / (float) RAND_MAX * 2 - 1);
            z = 1.0f;// * (rand() / (float) RAND_MAX * 2 - 1);
            float3 axis = new float3();
            axis.x = x;
            axis.y = y;
            axis.z = z;
            normalize(axis);

            if (1 - dot(point, axis) < 1e-6)
            {
                axis.x = point.y;
                axis.y = point.x;
                normalize(axis);
            }
            //if (point.y < 0) axis = scalevec(axis, -1);
            float3 vv = new float3();
            vv.x = (float)pos[3*i];
            vv.y = (float)pos[3*i+1];
            vv.z = (float)pos[3*i+2];
            vv = cross(vv, axis);
            velos[v++] = vv.x * vscale;
            velos[v++] = vv.y * vscale;
            velos[v++] = vv.z * vscale;
            //velos[v++] = 1.0f;

            i++;
        }
    }
}
