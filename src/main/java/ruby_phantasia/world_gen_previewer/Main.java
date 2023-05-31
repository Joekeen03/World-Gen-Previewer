package main.java.ruby_phantasia.world_gen_previewer;

//import main.java.ruby_phantasia.world_gen_previewer.old.Vector3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.*;

import java.io.IOException;
import java.nio.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.Functions.SetCursorPosCallback;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * TODO:
 *  *Finish tutorial up through camera stuff, esp. moving camera
 *      -Basic camera transformations
 *      -Camera movement
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
    private final Path vertexShaderPath = shadersDirectory.resolve("vertexShader.vs");
    private final Path fragmentShaderPath = shadersDirectory.resolve("fragmentShader.fs");

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
    private int SetupVertexBuffer(Cube cube) {
        FloatBuffer vertexBuffer = memAllocFloat(cube.N_VERTICES*3);
        for (Vector3f vertex : cube.cubeMesh.vertices) {
            vertexBuffer.put(vertex.x);
            vertexBuffer.put(vertex.y);
            vertexBuffer.put(vertex.z);
        }
        vertexBuffer.flip();

        int cubeVBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, cubeVBO);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
        return cubeVBO;
    } // SetupVertexBuffer

    // Creates an index buffer holding the cube's cubeMesh indices; returns the buffer's ID.
    private int SetupIndexBuffer(Cube cube) {
        IntBuffer indexBuffer = memAllocInt(cube.cubeMesh.indices.length);
        int[] flippedIndices = new int[cube.cubeMesh.indices.length];
        for (int i = 0; i < cube.cubeMesh.indices.length; i++) {
            flippedIndices[i] = cube.cubeMesh.indices[cube.cubeMesh.indices.length-i-1];
        }
//        indexBuffer.put(cube.cubeMesh.indices);
        indexBuffer.put(flippedIndices);
        indexBuffer.flip();

        int cubeIBO = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, cubeIBO);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);
        return cubeIBO;
    } // SetupIndexBuffer

    private void Init() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW.");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
//        glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);

        window = glfwCreateWindow(INITIAL_SCREEN_WIDTH, INITIAL_SCREEN_HEIGHT, "Hello World.", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window.");
        }

//        glfwMaximizeWindow(window); // Doesn't update the drawing canvas w/in it - ends up in the lower left corner


        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            glfwGetWindowSize(window, pWidth, pHeight);

            GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            windowWidth = pWidth.get(0);
            windowHeight = pHeight.get(0);
            System.out.printf("Window dimensions (w,h): %d, %d", windowWidth, windowHeight);

//            glfwSetWindowPos(window, (vidMode.width()-pWidth.get(0))/2, (vidMode.height()-pHeight.get(0))/2);

            glfwMakeContextCurrent(window);

            glfwSwapInterval(1);

            glfwShowWindow(window);
        }
    } // Init

    private void Loop() {
        GL.createCapabilities();

//        glClearColor(0.5f, 0.0f, 0.0f, 0.0f);

        Cube cube = new Cube(new Vector3f(0, 0, 0.0f), 1.0f);

        int cubeVBO = SetupVertexBuffer(cube);
        int cubeIBO = SetupIndexBuffer(cube);

        // Set up shaders
        int shaderProgramID = CreateShaderProgram();

        float cubeScale = 0.0f;
//        Matrix4f transformationMatrix = new Matrix4f();
        Camera camera = new Camera(
                window,
                windowHeight,
                windowWidth,
                new Vector3f(1.0f, 1.0f, -3.0f),
                new Vector3f(0.3f, 0.0f, 1.0f),
                new Vector3f(0.0f, 1.0f, 0.0f));
        TransformPipeline pipeline = new TransformPipeline(windowWidth, windowHeight);
        Vector3f scale = new Vector3f(1.0f);
        Vector3f position = new Vector3f();
        Vector3f rotation = new Vector3f();

        int glTransformationMatrixLocation = glGetUniformLocation(shaderProgramID, "gWorld");
        if (glTransformationMatrixLocation == -1) {
            System.out.println("glTransformationMatrixLocation could not be determined. Quitting...");
            System.exit(-1);
        }

        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_INDEX_ARRAY);
        glEnable(GL_CULL_FACE);

        FloatBuffer matrixBuffer = memAllocFloat(4*4);

        glfwSetWindowSizeCallback(window, (resizedWindowID, newWidth, newHeight) -> {
            glViewport(0, 0, newWidth, newHeight);
            pipeline.SetScreenDimensions(newWidth, newHeight);
        });



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
            pipeline.SetScale(scale.set(scaleFactor, scaleFactor, scaleFactor));
            pipeline.SetRotation(rotation.set(cubeScale*20, cubeScale*20, cubeScale*20));
            pipeline.SetWorldPos(position.set(Math.sin(cubeScale*5)*0.5f, 0.0f, -3.0f));
            pipeline.SetCamera(camera);
            pipeline.GetTransformation().get(matrixBuffer);
            glUniformMatrix4fv(glTransformationMatrixLocation, false, matrixBuffer);

            glEnableVertexAttribArray(0);
            glBindBuffer(GL_ARRAY_BUFFER, cubeVBO);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, cubeIBO);
            glDrawElements(GL_TRIANGLES, cube.cubeMesh.indices.length, GL_UNSIGNED_INT, 0);
            glDisableVertexAttribArray(0);

            glfwSwapBuffers(window);
        }
    } // Loop

    public static void main(String[] args) {
        new Main().Run();
    } // main
}
