package main.java.ruby_phantasia.world_gen_previewer;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class TransformPipeline {
    public TransformPipeline(int screenWidth, int screenHeight) {
        transformation = new Matrix4f();
        scale = new Vector3f(1.0f);
        rotation = new Vector3f();
        position = new Vector3f();
        perspective = new PerspectiveInformation(screenWidth, screenHeight, (float)Math.toRadians(60.0f), 0.0f, 100.0f);
        cameraPosition = new Vector3f();
        cameraUp = new Vector3f(0.0f, 1.0f, 0.0f);
        cameraTarget = new Vector3f(0.0f, 0.0f, 1.0f);
    }

    public void SetScale(Vector3fc newScale) {
        scale.set(newScale);
    }

    public void SetWorldPos(Vector3fc newPosition) {
        position.set(newPosition);
    }

    public void SetRotation(Vector3fc newRotation) {
        rotation.set(newRotation);
    }

    public void SetCamera(Camera camera) {
        cameraPosition.set(camera.getPosition());
        cameraUp.set(camera.getUp());
        cameraTarget.set(camera.getTarget());
    }

    public void SetScreenDimensions(int screenWidth, int screenHeight) {
        perspective.setScreenDimensions(screenWidth, screenHeight);
    }

    public Matrix4fc GetTransformation() {
        Matrix4f cameraRotation = new Matrix4f();
        Vector3f N = new Vector3f(cameraTarget).normalize();
        Vector3f U = new Vector3f(cameraUp).cross(cameraTarget).normalize();
        Vector3f V = new Vector3f(N).cross(U);
//        System.out.println("Vectors: U"+U+"V"+V+"N"+N);
        cameraRotation
                .m00(U.x).m10(U.y).m20(U.z)
                .m01(V.x).m11(V.y).m21(V.z)
                .m02(N.x).m12(N.y).m22(N.z);
//        cameraRotation
//                .m00(U.x).m01(U.y).m02(U.z)
//                .m10(V.x).m11(V.y).m12(V.z)
//                .m20(N.x).m21(N.y).m22(N.z);
        return transformation
                .setPerspective(perspective.FOVAngleY, perspective.aspectRatio, perspective.zNear, perspective.zFar)
                .lookAt(cameraPosition, cameraPosition.add(cameraTarget, new Vector3f()), cameraUp)
//                .rotateXYZ(0.0f, (float)Math.PI, 0.0f).mul(cameraRotation)
//                .translate(new Vector3f(cameraPosition).mul(-1))
                .translate(position).rotateXYZ(rotation).scale(scale);
    }

    private Matrix4f transformation;
    private Vector3f scale;
    private Vector3f position;
    private Vector3f rotation;
    private PerspectiveInformation perspective;
    private Vector3f cameraPosition;
    private Vector3f cameraUp; // Camera's rotation.
    private Vector3f cameraTarget;

    public static class PerspectiveInformation {
        int screenWidth;
        int screenHeight;
        float aspectRatio;
        float minFOVAngle;
        float FOVAngleY;
        float zNear;
        float zFar;

        public PerspectiveInformation(int screenWidth, int screenHeight, float minFOVAngle, float zNear, float zFar) {
            this.set(screenWidth, screenHeight, minFOVAngle, zNear, zFar);
        }

        public void set(int screenWidth, int screenHeight, float minFOVAngle, float zNear, float zFar) {
            this.minFOVAngle = minFOVAngle;
            this.zNear = zNear;
            this.zFar = zFar;
            this.setScreenDimensions(screenWidth, screenHeight);
        }

        public void setScreenDimensions(int screenWidth, int screenHeight) {
            this.screenWidth = screenWidth;
            this.screenHeight = screenHeight;
            this.aspectRatio = ((float)screenWidth)/((float)screenHeight);
            // Ensure neither horizontal nor vertical FOVs drop below minimum FOV
            if (this.aspectRatio < 1.0f) { // Window is portrait shaped, horizontal (X-axis?) FOV is smaller, == minFOV
                // Matrix4f's perspective methods expect the vertical (Y-axis) FOV as an argument.
                // So we need to compute the vertical FOV (unknown) from the horizontal FOV (known).
                this.FOVAngleY = (float) Math.atan(Math.tan(this.minFOVAngle)/this.aspectRatio)+0.5f;
                // FIXME Not perfect - as aspect ratio gets very small, horizontal FOV seems to shrink a bit,
                //  even though it should be fixed. That is, for a given scene, the left & right sides start getting
            } else {
                this.FOVAngleY = this.minFOVAngle;
            }
        }
    }
}
