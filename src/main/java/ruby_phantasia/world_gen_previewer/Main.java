package main.java.ruby_phantasia.world_gen_previewer;

import it.unimi.dsi.fastutil.ints.IntImmutableList;
import it.unimi.dsi.fastutil.objects.ObjectImmutableList;
import main.java.ruby_phantasia.world_gen_previewer.helper.Constants;
import main.java.ruby_phantasia.world_gen_previewer.helper.DefaultVectors;
import main.java.ruby_phantasia.world_gen_previewer.primitives.Cone;
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
    private final Path PRIMITIVE_VERTEX_SHADER_PATH = shadersDirectory.resolve("vertexShader.vert");
    private final Path AXES_VERTEX_SHADER_PATH = shadersDirectory.resolve("AxesVertexShader.vert");
    private final Path FRAGMENT_SHADER_PATH = shadersDirectory.resolve("fragmentShader.frag");

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

    private int CreateShader(String shaderProgramText, int shaderType) {
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
        return shaderID;
    } // AttachShader

    private int CreateShaderProgram(int vertexShader, int fragmentShader) {
        int shaderProgramID = glCreateProgram();
        if (shaderProgramID == 0) {
            System.err.printf("Failed to create shader program, quitting...\n");
            System.exit(-1);
        }

        glAttachShader(shaderProgramID, vertexShader);
        glAttachShader(shaderProgramID, fragmentShader);

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
        glDetachShader(shaderProgramID, vertexShader);
        glDetachShader(shaderProgramID, fragmentShader);
        return shaderProgramID;
    } // CreateShaderProgram

    private ShaderProgram SetupShaderProgram(String shaderName, Path vertexShaderPath, Path fragmentShaderPath) {
        int vertexShader = CreateShader(String.join("\n", ReadFile(vertexShaderPath)), GL_VERTEX_SHADER);
        int fragmentShader = CreateShader(String.join("\n", ReadFile(fragmentShaderPath)), GL_FRAGMENT_SHADER);
        int shaderProgram = CreateShaderProgram(vertexShader, fragmentShader);
        int worldTransformLocation = glGetUniformLocation(shaderProgram, "gWorld");
        if (worldTransformLocation == -1) {
            System.out.println("glTransformationMatrixLocation could not be determined for "+shaderName+" shader program. Quitting...");
            System.exit(-1);
        }
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
        return new ShaderProgram(shaderProgram, worldTransformLocation, shaderName);
    }

    // Creates a vertex buffer holding the cube's cubeMesh vertices; returns the buffer's ID.
    private int SetupVertexBuffer(PooledVertices pooledVertices) {
        FloatBuffer vertexBuffer = memAllocFloat(pooledVertices.vertices.length*3);
        for (Vector3fc vertex : pooledVertices.vertices) {
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
    private int SetupIndexBuffer(PooledVertices pooledVertices) {
        // Where the indices for a given primitive start.

        IntBuffer indexBuffer = memAllocInt(pooledVertices.indices.length);
        indexBuffer.put(pooledVertices.indices);
        indexBuffer.flip();

        int cubeIBO = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, cubeIBO);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);
        return cubeIBO;
    } // SetupIndexBuffer

    private int SetupAxisVertexBuffer() {
        FloatBuffer vertexBuffer = memAllocFloat(3*3*2*2); // 3 axes, two points & 2 colors each, 3 values each
        Vector3fc red = DefaultVectors.X_POSITIVE;
        Vector3fc green = DefaultVectors.Y_POSITIVE;
        Vector3fc blue = DefaultVectors.Z_POSITIVE;
        Vector3fc[] vertices = {
                DefaultVectors.ZERO, red, DefaultVectors.X_POSITIVE.mul(5.0f, new Vector3f()), red,
                DefaultVectors.ZERO, green, DefaultVectors.Y_POSITIVE.mul(5.0f, new Vector3f()), green,
                DefaultVectors.ZERO, blue, DefaultVectors.Z_POSITIVE.mul(5.0f, new Vector3f()), blue,
        };
        for (Vector3fc vector :
                vertices) {
            vertexBuffer.put(vector.x());
            vertexBuffer.put(vector.y());
            vertexBuffer.put(vector.z());
        }
        vertexBuffer.flip();
        int coordVBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, coordVBO);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
        return coordVBO;
    }

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

            glfwMakeContextCurrent(window);

            glfwSwapInterval(1);

            glfwShowWindow(window);
        }
    } // Init

    private void Loop() {
        GL.createCapabilities();
        glViewport(0, 0, windowWidth, windowHeight);

        glClearColor(0.45f, 0.45f, 1.0f, 0.0f);

        Primitive[] primitives = {
                new Cube(new Vector3f(0.0f, 0.0f, 0.0f), 1.0f),
                new Cube(new Vector3f(0.0f, 2.0f, 0.0f), 1.0f),
                new Cone(new Vector3f(0.0f, 1.0f, 0.0f), new Vector3f(1.0f, 1.0f, 1.0f), 1.0f, 2.0f),
        };

        PooledVertices pooledVertices = PooledVertices.PoolVertices(primitives);

        int primitiveVBO = SetupVertexBuffer(pooledVertices);
        int primitiveIBO = SetupIndexBuffer(pooledVertices);
        int axesVBO = SetupAxisVertexBuffer();

        // Set up shaders
        ShaderProgram primitiveShaderProgram = SetupShaderProgram("Primitives", PRIMITIVE_VERTEX_SHADER_PATH, FRAGMENT_SHADER_PATH);
        ShaderProgram axesShaderProgram = SetupShaderProgram("Axes", AXES_VERTEX_SHADER_PATH, FRAGMENT_SHADER_PATH);

        float cubeScale = 0.0f;

        Camera camera = new Camera(
                window,
                windowHeight,
                windowWidth,
                new Vector3f(1.0f, 1.0f, -3.0f),
                new Vector3f(0.0f, 0.0f, 1.0f),
                new Vector3f(0.0f, 1.0f, 0.0f));
        TransformPipeline pipeline = new TransformPipeline(windowWidth, windowHeight);

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
            camera.Move();
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            cubeScale += 0.001f;
            float scaleFactor = (float) Math.sin(cubeScale);

//            primitives[0].SetScale(scaleFactor);
//            primitives[0].SetRotationXYZ(cubeScale*20, cubeScale*20, cubeScale*20);
//            primitives[0].SetPosition((float)Math.sin(cubeScale*5)*1.5f, 0.0f, 0.0f);

            glUseProgram(primitiveShaderProgram.ID);
            glEnableVertexAttribArray(0);

            glBindBuffer(GL_ARRAY_BUFFER, primitiveVBO);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, primitiveIBO);

            for (int index = 0; index < primitives.length; index++) {
                pipeline.SetScale(primitives[index].GetScale());
                pipeline.SetRotation(primitives[index].GetRotation());
                pipeline.SetWorldPos(primitives[index].GetPosition());
                pipeline.SetCamera(camera);
                pipeline.GetTransformation().get(matrixBuffer);

                // TODO Make this an input variable for the shader, instead of a uniform; far less draw calls.
                // TODO Add primitive color to the shader input.
                glUniformMatrix4fv(primitiveShaderProgram.worldTransformLocation, false, matrixBuffer);
                glDrawElements(GL_TRIANGLES, primitives[index].GetIndices().size(), GL_UNSIGNED_INT, pooledVertices.primitiveIndexStarts[index]*Constants.GL_FLOAT_SIZE);
            }
            glDisableVertexAttribArray(0);

            glUseProgram(axesShaderProgram.ID);
            glBindBuffer(GL_ARRAY_BUFFER, axesVBO);
            glEnableVertexAttribArray(0);
            glEnableVertexAttribArray(1);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 6*Constants.GL_FLOAT_SIZE, 0);
            glVertexAttribPointer(1, 3, GL_FLOAT, false, 6*Constants.GL_FLOAT_SIZE, 3*Constants.GL_FLOAT_SIZE);
            pipeline.ResetScale();
            pipeline.ResetWorldPos();
            pipeline.ResetRotation();
            pipeline.SetCamera(camera);
            pipeline.GetTransformation().get(matrixBuffer);

            // TODO Make this an input variable for the shader, instead of a uniform; far less draw calls.
            // TODO Add primitive color to the shader input.
            glUniformMatrix4fv(axesShaderProgram.worldTransformLocation, false, matrixBuffer);
            glDrawArrays(GL_LINES, 0, 3*2);

            glDisableVertexAttribArray(0);
            glDisableVertexAttribArray(1);

            glfwSwapBuffers(window);
        }
    } // Loop

    public static void main(String[] args) {
        new Main().Run();
    } // main

    private static class ShaderProgram {
        private final int ID;
        private final int worldTransformLocation; // Location of World-View-Projection matrix uniform location
        private final String name;

        public ShaderProgram(int ID, int worldTransformLocation, String name) {
            this.ID = ID;
            this.worldTransformLocation = worldTransformLocation;
            this.name = name;
        }
    }

    private static class PooledVertices {
        private final Vector3fc[] vertices;
        private final int[] indices;
        private final int[] primitiveIndexStarts;

        public PooledVertices(Vector3fc[] vertices, int[] indices, int[] primitiveIndexStarts) {
            this.vertices = vertices;
            this.indices = indices;
            this.primitiveIndexStarts = primitiveIndexStarts;
        }

        public static PooledVertices PoolVertices(Primitive[] primitives) {

            int[] vertexOffsets = new int[primitives.length];
            int[] vertexOffset = {0};
            int[] vertexOffsetIndex = {0};

            Vector3fc[] pooledVertices = Arrays.stream(primitives).flatMap(primitive -> {
                ObjectImmutableList<Vector3fc> primitiveVertices = primitive.GetVertices();
                int currOffset = vertexOffset[0];
                int currIndex = vertexOffsetIndex[0];
                vertexOffsets[currIndex] = currOffset;
                vertexOffsetIndex[0]++;
                vertexOffset[0] += primitiveVertices.size();
                return primitiveVertices.stream();
            }).toArray(length -> new Vector3fc[length]);

            // Where the indices for a given primitive start.
            int[] offsetIndex = {0};
            int[] nPrimitiveIndices = {0};
            int[] primitiveIndexStarts = new int[primitives.length];

            int[] pooledIndices = Arrays.stream(primitives).flatMapToInt(primitive -> {
                IntImmutableList primitiveIndices = primitive.GetIndices();
                // Capture the current offset, as the mapping stream doesn't seem to get processed
                //  until after offset is updated - when exactly? When toArray is executed?
                int currIndex = offsetIndex[0];
                int currVertexOffset = vertexOffsets[currIndex];
                int currNPrimitiveIndices = nPrimitiveIndices[0];

                primitiveIndexStarts[currIndex] = currNPrimitiveIndices;
                nPrimitiveIndices[0] += primitiveIndices.size();
                offsetIndex[0]++;

                return primitiveIndices.intStream().map(index -> index+currVertexOffset);
            }).toArray();
//            System.out.println(Arrays.toString(pooledIndices));
            return new PooledVertices(pooledVertices, pooledIndices, primitiveIndexStarts);
        }
    }
}
