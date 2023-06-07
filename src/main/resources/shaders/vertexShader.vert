#version 330

//uniform float vertexScale;
uniform mat4 gWorld;

layout (location=0) in vec3 Position;

out vec4 Color;

void main()
{
    gl_Position = gWorld*vec4(Position, 1.0);
    Color = vec4(clamp(2*Position, 0.0, 1.0), 1.0);
//    Color = vec4(1.0);
}