#version 150

#include "shader/ShaderGlobals.glslh"

in vec2 fs_in_tc;
in vec3 fs_in_normal;
out vec4 fs_out_color;

const vec3 lightDir = vec3(0,-1,0);

void main()
{
    vec2 tx = 2 * fs_in_tc - 1;
    if(tx.x * tx.x + tx.y * tx.y > 1)
    {
        discard;
    }
    fs_out_color = g_color; // * (0.6f + 0.5f * dot(normalize(fs_in_normal), normalize(-lightDir)));
}