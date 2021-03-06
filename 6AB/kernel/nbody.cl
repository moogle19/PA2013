float4 bodyBodyInteraction(float4 pi, float4 pj, float4 ai, float EPSILON_SQUARED)  
{  
    float3 r;  

    r.x = pj.x - pi.x;  
    r.y = pj.y - pi.y;  
    r.z = pj.z - pi.z;  

    float distSqr = r.x * r.x + r.y * r.y + r.z * r.z + EPSILON_SQUARED;  

    float invDist = rsqrt(distSqr);
    float invDistCube =  invDist * invDist * invDist;
    //mass = 1
    float m = 1;
    float s = m * invDistCube;  

    ai.x += r.x * s;  
    ai.y += r.y * s;  
    ai.z += r.z * s;  
    return ai;  
}

kernel void nBody_CalcNewV(
global float4* body_Pos,
global float4* body_V,
const float DELTA_T,
const float EPSILON_SQUARED)
{
    uint id = get_global_id(0);
    uint N = get_global_size(0);

    float4 myPos = body_Pos[id];
    
    float4 ac = (float4)(0,0,0,0);
    
    for(uint i = 0; i < N; ++i)
    {  
        float4 otherPos = body_Pos[i];
        ac = bodyBodyInteraction(myPos, otherPos, ac, EPSILON_SQUARED);
    }
    
    float4 v = body_V[id];
    v += ac * DELTA_T;
    body_V[id] = v;
}

kernel void nBody_CalcNewPos(
global float4* body_Pos, 
global float4* body_V,
const float DELTA_T)
{
    uint id = get_global_id(0);
    body_Pos[id] += body_V[id] * DELTA_T;
}

kernel void passPositionOn(
global float4* body_Pos,
global float4* curveVertex_Pos,
const uint VERTICES_PER_CURVE)
{
	uint id = get_global_id(0);
	uint vpc = VERTICES_PER_CURVE;
	
    for(int i = 0; i < vpc-1; i++) {
    	curveVertex_Pos[id*vpc+i] = curveVertex_Pos[id*vpc+i+1];
    }
    curveVertex_Pos[id*vpc-1] = body_Pos[id];
}

kernel void setTrailParticle(
global float4* trailParticle_Pos, 
global float*  trailParticle_S, 
global float4* trailParticle_Dir, 
global float4* curveVertex_Pos,   
const int TRAIL_PARTICLES_PER_CURVE,
const int VERTICES_PER_CURVE,   
const float dSC,
const float dSTP)
{
    uint id = get_global_id(0);
	uint tppc = TRAIL_PARTICLES_PER_CURVE;
    uint vpc = VERTICES_PER_CURVE;

    uint prevCV, nextCV;
    float tps, sPrevCV, sTPLoc;
    float4 prevCVpos, nextCVpos;

    for(int i = 0; i < tppc-1; i++)
    {
        tps = trailParticle_S[id*tppc+i];
        prevCV = floor(tps * (vpc - 1));
        nextCV = prevCV + 1;
        
        sPrevCV = prevCV * dSC;
        
        sTPLoc = (tps - sPrevCV) / dSC;

        prevCVpos = curveVertex_Pos[id*tppc+prevCV];
        nextCVpos = curveVertex_Pos[id*tppc+nextCV];

        trailParticle_Pos[id*tppc+i] = (prevCVpos * (1 - sTPLoc) + sTPLoc * nextCVpos);
     
        trailParticle_Dir[id*tppc+i] = nextCVpos - prevCVpos;
    }
}