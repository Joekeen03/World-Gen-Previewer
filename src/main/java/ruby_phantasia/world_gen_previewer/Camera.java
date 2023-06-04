package main.java.ruby_phantasia.world_gen_previewer;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import static org.lwjgl.glfw.GLFW.*;

public class Camera {
    private long windowID;
    private int windowHeight;
    private int windowWidth;

    private Vector3f position;
    private Vector3f target;
    private Vector3f forward;
    private Vector3f up;

    // Angle in degrees from +x axis, counter-clockwise about +Y axis (looking down at xz plane along -Y).
    float angleHorizontal;
    // Angle in degrees from horizontal plane (xz -plane), downwards (+angle below xz, -angle above xz)
    float angleVertical;

    private double lastMouseXPos;
    private double lastMouseYPos;

    private static final float forwardsSpeed = 0.02f;
    private static final float sidewaysSpeed = 0.02f;
    private static final float verticalSpeed = 0.02f;

    // Degrees change in corresponding angle per pixels of mouse movement.
    private static final float horizontalSensitivity = 1.0f/6.0f;
    private static final float verticalSensitivity = 1.0f/6.0f;

    private static final float MAX_VERTICAL_ANGLE = 90.0f;
    private static final float MIN_VERTICAL_ANGLE = -90.0f;

    public Camera(long windowID, int windowHeight, int windowWidth, Vector3f position, Vector3f target, Vector3f up) {
        this.windowID = windowID;
        this.windowHeight = windowHeight;
        this.windowWidth = windowWidth;
        this.position = position;
        this.forward = new Vector3f();
        this.target = target;
        this.up = up;
        Init();
    } // Camera(args)

    public void Init() {
        Vector3f horizontalTarget = new Vector3f(target.x, 0.0f, target.z).normalize();
        if (horizontalTarget.z >= 0.0f) {
            if (horizontalTarget.x >= 0.0f) {
                angleHorizontal = 360.0f - (float)Math.toDegrees(Math.asin(horizontalTarget.z));
            } else {
                angleHorizontal = 180.0f + (float)Math.toDegrees(Math.asin(horizontalTarget.z));
            }
        } else {
            if (horizontalTarget.x >= 0.0f) {
                angleHorizontal = (float)Math.toDegrees(Math.asin(-horizontalTarget.z));
            } else {
                angleHorizontal = 180.0f - (float)Math.toDegrees(Math.asin(-horizontalTarget.z));
            }
        }

        angleVertical = (float)Math.toDegrees(Math.asin(target.y));

        lastMouseXPos = windowWidth/2;
        lastMouseYPos = windowHeight/2;
        glfwSetCursorPos(windowID, lastMouseXPos, lastMouseYPos);

        glfwSetInputMode(windowID, GLFW_CURSOR, GLFW_CURSOR_DISABLED); // For eventual camera control.

        Update();
        /**
         * My control scheme:
         *  -MC style (mouse controls camera, cursor does not move and is invisible):
         *  -So, need to disable cursor (GLFW_CURSOR_DISABLED)
         *  -Keep a vertical angle and a horizontal angle; horizontal angle is angle from +X around the +Y axis,
         *      vertical angle is angle from the horizontal plane, like with the OpenGL tutorial's camera control
         *  -Really, only difference is how the mouse movement translates to camera motion
         *  -Also need to, when the 'cursor' moves, change angles according to how much it's moved
         *  -So we need to maintain the 'cursor's last position so we can calculate the difference, and update
         *      the last position after the difference has been calculated.
         *  -Then, just update the angles according to the difference.
         *  -Probably want Enable()/Disable() methods that turn camera control on/off?
         */
    } // Init

    public boolean HandleKeyPress(int key, int action) {
        boolean consumed = false;
        switch(key) {
            case GLFW_KEY_A: {
                Vector3f left = up.cross(target, new Vector3f()).normalize().mul(sidewaysSpeed);
                position.add(left);
                consumed = true;
                break;
            }
            case GLFW_KEY_D: {
                Vector3f right = target.cross(up, new Vector3f()).normalize().mul(sidewaysSpeed);
                position.add(right);
                consumed = true;
                break;
            }
            case GLFW_KEY_W: {
                position.fma(forwardsSpeed, forward.normalize());
                consumed = true;
                break;
            }
            case GLFW_KEY_S: {
                position.fma(-forwardsSpeed, forward.normalize());
                consumed = true;
                break;
            }
            case GLFW_KEY_LEFT_SHIFT: {
                position.add(0.0f, -verticalSpeed, 0.0f);
                consumed = true;
                break;
            }
            case GLFW_KEY_SPACE: {
                position.add(0.0f, verticalSpeed, 0.0f);
                consumed = true;
                break;
            }
        }
        return consumed;
    } // HandleKeyPress

    public boolean HandleMouseMovement(double newXPosition, double newYPosition) {
        boolean consumed = false;
        float deltaX = (float)(newXPosition-lastMouseXPos);
        float deltaY = (float)(newYPosition-lastMouseYPos);
        lastMouseXPos = newXPosition;
        lastMouseYPos = newYPosition;
        angleHorizontal -= (deltaX*horizontalSensitivity)%360.0f;
        // Don't think cursor movement can be so fast that the raw angle could reach (+/-)infinity
        float newVerticalAngleRaw = angleVertical+deltaY*verticalSensitivity;
        angleVertical = Math.max(MIN_VERTICAL_ANGLE, Math.min(MAX_VERTICAL_ANGLE, newVerticalAngleRaw));
        Update();
        consumed = true;
        return consumed;
    } // HandleMouseMovement

    private void Update() {
        /**
         * Might be faster/simpler to do:
         * Vector3f newTargetVector = new Vector3f(1.0f, 0.0f, 0.0f).rotateX(angleHorizontal).rotateY(angleVertical).normalize();
         * target = newTargetVector;
         * Vector3f yAxis = new Vector3f(0.0f, 1.0f, 0.0f);
         * Vector3f newRight = yAxis.cross(newTargetVector, new Vector3f()).normalize();
         * newTargetVector.cross(newRight, up).normalize();
         * Since one of the two axes we're rotating the new target vector about is the +Y axis, and the
         * other rotation axis is constrained to the xz plane, it should work?
         */
        Vector3fc verticalAxis = new Vector3f(0.0f, 1.0f, 0.0f);
        // Compute new forward vector.
        forward = new Vector3f(1.0f, 0.0f, 0.0f).rotateAxis((float)Math.toRadians(angleHorizontal), 0.0f, 1.0f, 0.0f);
        Vector3f right = verticalAxis.cross(forward, new Vector3f()).normalize();

        // Compute new target vector
        forward.rotateAxis((float)Math.toRadians(angleVertical), right.x, right.y, right.z, target);
        target.normalize();
        target.cross(right, up).normalize(); // Compute new up vector
    }

    public Vector3fc getPosition() {
        return position;
    } // getPosition

    public Vector3fc getTarget() {
        return target;
    } // getTarget

    public Vector3fc getUp() {
        return up;
    } // getUp
}
