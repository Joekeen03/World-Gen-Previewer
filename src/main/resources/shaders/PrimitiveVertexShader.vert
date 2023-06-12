#version 330

//uniform float vertexScale;
uniform mat4 gWorld;

layout (location=0) in vec3 Position;
layout (location=1) in vec4 Color_0;

out vec4 Color;

void main()
{
    gl_Position = gWorld*vec4(Position, 1.0);
    Color = vec4(Color_0);
}