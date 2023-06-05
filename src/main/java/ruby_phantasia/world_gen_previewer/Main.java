package main.java.ruby_phantasia.world_gen_previewer;

//import main.java.ruby_phantasia.world_gen_previewer.old.Vector3f;
import main.java.ruby_phantasia.world_gen_previewer.primitives.Cube;
import main.java.ruby_phantasia.world_gen_previewer.primitives.Primitive;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.*;

import java.io.IOException;
import java.nio.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.IntStream;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * TODO:
 *  *Figure out voxel render engine:
 *      -voxel representation, storage
 *      -
 *  *Figure out resizing window.
 */

public class Main {
    private long window;
    private int windowWidth;
    private int windowHeight;

    private static final int INITIAL_SCREEN_WIDTH = 700;
    private static final int INITIAL_SCREEN_HEIGHT = 700;

    private final Path mainDirectory = Paths.get("src/main/");
    private final Path shadersDirectory = mainDirectory.resolve("resources/shaders/");
    private final Path vertexShaderPath = shadersDirectory.resolve("vertexShader.vert");
    private final Path fragmentShaderPath = shadersDirectory.resolve("fragmentShader.frag");

    public void Run() {
        System.out.println("Hello World");

        Init();
        Loop();

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        glfwTerminate();
        glfwSetErrorCallback(null).free();
    } // Run

    private String ReadFile(Path filePath) {
        try {
            return new String(Files.readAllBytes(filePath));
        } catch (IOException e) {
            System.out.printf("Error reading file '%s'\nError: ", filePath.toAbsolutePath());
            e.printStackTrace();
            System.exit(-1);
            return "";
        }
    } // ReadFile

    private int AttachShader(int shaderProgramID, String shaderProgramText, int shaderType) {
        int shaderID = glCreateShader(shaderType);
        if (shaderID == 0) {
            System.err.printf("Error creating shader type %d, quitting.\n", shaderType);
            System.exit(-1);
        }
        glShaderSource(shaderID, String.join("", shaderProgramText));
        glCompileShader(shaderID);
        int[] success = {0};
        glGetShaderiv(shaderID, GL_COMPILE_STATUS, success);
        if (success[0]==0) {
            String output = glGetShaderInfoLog(shaderID);
            System.err.printf("Error compiling shader type %d: %s\nQuitting...\n", GL_VERTEX_SHADER, output);
            System.exit(-1);
        }
        glAttachShader(shaderProgramID, shaderID);
        return shaderID;
    } // AttachShader

    private int CreateShaderProgram() {
        int shaderProgramID = glCreateProgram();
        if (shaderProgramID == 0) {
            System.err.printf("Failed to create shader program, quitting...\n");
            System.exit(-1);
        }

        int vertexShaderID = AttachShader(shaderProgramID, String.join("\n", ReadFile(vertexShaderPath)), GL_VERTEX_SHADER);
        int fragmentShaderID = AttachShader(shaderProgramID, String.join("\n", ReadFile(fragmentShaderPath)), GL_FRAGMENT_SHADER);

        int[] success = {0};
        glLinkProgram(shaderProgramID);
        glGetProgramiv(shaderProgramID, GL_LINK_STATUS, success);
        if (success[0] == 0) {
            String error = glGetProgramInfoLog(shaderProgramID);
            System.err.printf("Error linking shader program: %s\nQuitting...", error);
            System.exit(-1);
        }

        glValidateProgram(shaderProgramID);
        glGetProgramiv(shaderProgramID, GL_VALIDATE_STATUS, success);
        if (success[0] == 0) {
            String error = glGetProgramInfoLog(shaderProgramID);
            System.err.printf("Error validating shader program: %s", error);
            System.exit(-1);
        }

        glUseProgram(shaderProgramID);
        glDetachShader(shaderProgramID, vertexShaderID);
        glDetachShader(shaderProgramID, fragmentShaderID);
        glDeleteShader(vertexShaderID);
        glDeleteShader(fragmentShaderID);
        return shaderProgramID;
    } // CreateShaderProgram

    // Creates a vertex buffer holding the cube's cubeMesh vertices; returns the buffer's ID.
    private int SetupVertexBuffer(Primitive[] primitives) {
        Vector3fc[] vertices = Arrays.stream(primitives).flatMap((primitive) -> Arrays.stream(primitive.GetVertices())).toArray((length) -> new Vector3fc[length]);
        FloatBuffer vertexBuffer = memAllocFloat(vertices.length*3);
        for (Vector3fc vertex : vertices) {
//            vertex.get(vertexBuffer);
            vertexBuffer.put(vertex.x());
            vertexBuffer.put(vertex.y());
            vertexBuffer.put(vertex.z());
        }
        vertexBuffer.flip();

        int cubeVBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, cubeVBO);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
        return cubeVBO;
    } // SetupVertexBuffer

    // Creates an index buffer holding the cube's cubeMesh indices; returns the buffer's ID.
    private BufferInfo SetupIndexBuffer(Primitive[] primitives) {
        // Where the indices for a given primitive start.
        int[] offsets = new int[primitives.length];
        int[] offset = {0};
        int[] offsetIndex = {0};

        int[] indices = Arrays.stream(primitives).flatMapToInt(primitive -> {
            int[] primitiveIndices = primitive.GetIndices();
            // Capture the current offset, as the mapping stream doesn't seem to get processed
            //  until after offset is updated - when exactly? When toArray is executed?
            int currOffset = offset[0];
            int currIndex = offsetIndex[0];
            IntStream stream = Arrays.stream(primitiveIndices).map(index -> index+currOffset);
            offsets[currIndex] = currOffset;
            offsetIndex[0]++;
            offset[0] += primitiveIndices.length;
            return stream;
        }).toArray();

        IntBuffer indexBuffer = memAllocInt(indices.length);
        indexBuffer.put(indices);
        indexBuffer.flip();

        int cubeIBO = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, cubeIBO);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);
        return new BufferInfo(cubeIBO, offsets);
    } // SetupIndexBuffer

    private void Init() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW.");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);

        window = glfwCreateWindow(INITIAL_SCREEN_WIDTH, INITIAL_SCREEN_HEIGHT, "Hello World", NULL, NULL);

        // Because using the window creation hints to maximize the screen and prevent resizing
        //  yields a window with a little gap above the taskbar.
        glfwMaximizeWindow(window);
        glfwSetWindowAttrib(window, GLFW_RESIZABLE, GLFW_FALSE);

        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window.");
        }

        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            glfwGetWindowSize(window, pWidth, pHeight);

            GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            windowWidth = pWidth.get(0);
            windowHeight = pHeight.get(0);
            System.out.printf("Window dimensions (w,h): %d, %d\n", windowWidth, windowHeight);


//            glfwSetWindowPos(window, (vidMode.width()-pWidth.get(0))/2, (vidMode.height()-pHeight.get(0))/2);

            glfwMakeContextCurrent(window);

            glfwSwapInterval(1);

            glfwShowWindow(window);
        }
    } // Init

    private void Loop() {
        GL.createCapabilities();
        glViewport(0, 0, windowWidth, windowHeight);

//        glClearColor(0.5f, 0.0f, 0.0f, 0.0f);

        Primitive[] primitives = {
                new Cube(new Vector3f(0, 0, 0.0f), 1.0f),
                new Cube(new Vector3f(0, 2.0f, 0.0f), 1.0f),
                new Cube(new Vector3f(0, 1.0f, 0.0f), 1.0f)
        };

        int primitiveVBO = SetupVertexBuffer(primitives);
        BufferInfo primitiveIBO = SetupIndexBuffer(primitives);

        // Set up shaders
        int shaderProgramID = CreateShaderProgram();

        float cubeScale = 0.0f;

        Camera camera = new Camera(
                window,
                windowHeight,
                windowWidth,
                new Vector3f(1.0f, 1.0f, -3.0f),
                new Vector3f(0.0f, 0.0f, 1.0f),
                new Vector3f(0.0f, 1.0f, 0.0f));
        TransformPipeline pipeline = new TransformPipeline(windowWidth, windowHeight);

        int glTransformationMatrixLocation = glGetUniformLocation(shaderProgramID, "gWorld");
        if (glTransformationMatrixLocation == -1) {
            System.out.println("glTransformationMatrixLocation could not be determined. Quitting...");
            System.exit(-1);
        }

        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_INDEX_ARRAY);
        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);

        FloatBuffer matrixBuffer = memAllocFloat(4*4);


        glfwSetWindowSizeCallback(window, (resizedWindowID, newWidth, newHeight) -> {
            glViewport(0, 0, newWidth, newHeight);
            pipeline.SetScreenDimensions(newWidth, newHeight);
        });

        // Each primitive's index in the transformation matrix array; mostly to make the 1:1 association
        //  explicit.
        int[] transformMatrixIndices = new int[primitives.length];
        for (int i = 0; i < transformMatrixIndices.length; i++) {
            transformMatrixIndices[i] = i;
        }

        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true);
            } else {
                camera.HandleKeyPress(key, action);
            }
        });

        glfwSetCursorPosCallback(window, (window, newXPos, newYPos) -> camera.HandleMouseMovement(newXPos, newYPos));

        while (!glfwWindowShouldClose(window)) {

            glfwPollEvents();
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            cubeScale += 0.001f;
            float scaleFactor = (float) Math.sin(cubeScale);

            primitives[0].SetScale(scaleFactor);
            primitives[0].SetRotationXYZ(cubeScale*20, cubeScale*20, cubeScale*20);
            primitives[0].SetPosition((float)Math.sin(cubeScale*5)*1.5f, 0.0f, 0.0f);

            glEnableVertexAttribArray(0);
            glBindBuffer(GL_ARRAY_BUFFER, primitiveVBO);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, primitiveIBO.GetBufferID());

            for (int index = 0; index < primitives.length; index++) {
                pipeline.SetScale(primitives[index].GetScale());
                pipeline.SetRotation(primitives[index].GetRotation());
                pipeline.SetWorldPos(primitives[index].GetPosition());
                pipeline.SetCamera(camera);
                pipeline.GetTransformation().get(matrixBuffer);

                // TODO Make this an input variable for the shader, instead of a uniform; far less draw calls.
                // TODO Add primitive color to the shader input.
                glUniformMatrix4fv(glTransformationMatrixLocation, false, matrixBuffer);

                glDrawElements(GL_TRIANGLES, primitives[index].GetIndices().length, GL_UNSIGNED_INT, 0);
            }
            glDisableVertexAttribArray(0);

            glfwSwapBuffers(window);
        }
    } // Loop

    public static void main(String[] args) {
        new Main().Run();
    } // main

    public static class BufferInfo {
        private final int bufferID;
        private final int[] offset;

        public BufferInfo(int bufferID, int[] offset) {
            this.bufferID = bufferID;
            this.offset = offset;
        }

        public int GetBufferID() {
            return bufferID;
        }

        public int GetOffset(int index) {
            return offset[index];
        }

        @Override
        public String toString() {
            return "BufferInfo{" +
                    "bufferID=" + bufferID +
                    ", offset=" + Arrays.toString(offset) +
                    '}';
        }
    }
}
