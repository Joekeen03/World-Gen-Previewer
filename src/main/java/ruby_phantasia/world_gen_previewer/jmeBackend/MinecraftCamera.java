// FIXME The general structure of this was derived from FlyByCamera, but virtually none of its code is present here;
//  do I need to insert the license header from FlyByCamera?
/* Original license from FlyByCamera
// Just for safety:
 * Copyright (c) 2009-2012 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package main.java.ruby_phantasia.world_gen_previewer.jmeBackend;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.*;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

public class MinecraftCamera implements AnalogListener, ActionListener {

    private static final String MCCAM_ROTATEUP = "MCCAM_ROTATEUP";
    private static final String MCCAM_ROTATEDOWN = "MCCAM_ROTATEDOWN";
    private static final String MCCAM_ROTATELEFT = "MCCAM_ROTATELEFT";
    private static final String MCCAM_ROTATERIGHT = "MCCAM_ROTATERIGHT";

    private static final String MCCAM_MOVELEFT = "MCCAM_MOVELEFT";
    private static final String MCCAM_MOVERIGHT = "MCCAM_MOVERIGHT";
    private static final String MCCAM_MOVEUP = "MCCAM_MOVEUP";
    private static final String MCCAM_MOVEDOWN = "MCCAM_MOVEDOWN";
    private static final String MCCAM_MOVEFORWARD = "MCCAM_MOVEFORWARD";
    private static final String MCCAM_MOVEBACKWARD = "MCCAM_MOVEBACKWARD";

    private static final String[] mappings = {
    	MCCAM_ROTATEUP,
    	MCCAM_ROTATEDOWN,
    	MCCAM_ROTATELEFT,
    	MCCAM_ROTATERIGHT,

    	MCCAM_MOVELEFT,
    	MCCAM_MOVERIGHT,
    	MCCAM_MOVEUP,
    	MCCAM_MOVEDOWN,
    	MCCAM_MOVEFORWARD,
    	MCCAM_MOVEBACKWARD,
    };

    // Degrees change in corresponding angle per pixels of mouse movement.
    private static final float horizontalSensitivity = 1.0f/6.0f*200.0f;
    private static final float verticalSensitivity = horizontalSensitivity;

    private static final float forwardsSpeed = 0.02f;
    private static final float sidewaysSpeed = forwardsSpeed;
    private static final float verticalSpeed = forwardsSpeed;

    private static final float MAX_VERTICAL_ANGLE = 90.0f;
    private static final float MIN_VERTICAL_ANGLE = -90.0f;

    private static final Vector3f X_POS = new Vector3f(1.0f, 0.0f, 0.0f);
    private static final Vector3f Y_POS = new Vector3f(0.0f, 1.0f, 0.0f);
    private static final Vector3f Z_POS = new Vector3f(0.0f, 0.0f, 1.0f);

    private final Camera camera;
    private InputManager inputManager;

    private final Vector3f forward;
    private final Vector3f left;
    private final Vector3f position;
    private final Quaternion horizontalRotation;
    private final Quaternion verticalRotation;

    // Angle in degrees from +Z axis, counter-clockwise about +Y axis (looking down at xz plane along -Y).
    private float angleHorizontal;
    // Angle in degrees from horizontal plane (xz -plane), downwards (+angle below xz, -angle above xz)
    private float angleVertical;

    private int lastScreenWidth;
    private int lastScreenHeight;

    public MinecraftCamera(Camera camera) {
        this.camera = camera;
        forward = new Vector3f();
        left = new Vector3f();
        position = camera.getLocation().clone();
        horizontalRotation = new Quaternion();
        verticalRotation = new Quaternion();
        angleHorizontal = 0.0f;
        angleVertical = 0.0f;
        lastScreenHeight = camera.getHeight();
        lastScreenWidth = camera.getWidth();
        initAngle();
    } // MinecraftCamera

    private void initAngle() {
        Vector3f target = camera.getDirection();
        forward.set(target.x, 0.0f, target.z).normalizeLocal();
        angleHorizontal = (float)Math.toDegrees(Math.atan2(forward.x, forward.z));
        angleVertical = (float)Math.toDegrees(Math.asin(target.y));
        Update();
    } // initAngle

    public void HandleXMouseMovement(float deltaX) {
        // FIXME Kinda a hack
        // Mouse input spikes when window is resized, so drop it.
        if (camera.getWidth() != lastScreenWidth) {
            lastScreenWidth = camera.getWidth();
            return;
        }

        angleHorizontal -= (deltaX*horizontalSensitivity)%360.0f;
        Update();
    } // HandleXMouseMovement

    public void HandleYMouseMovement(float deltaY) {
        // FIXME Kinda a hack
        // Mouse input spikes when window is resized, so drop it.
        if (camera.getHeight() != lastScreenHeight) {
            lastScreenHeight = camera.getHeight();
            return;
        }

        // Don't think cursor movement can be so fast that the raw angle could reach (+/-)infinity
        float newVerticalAngleRaw = angleVertical+deltaY*verticalSensitivity;
        angleVertical = Math.max(MIN_VERTICAL_ANGLE, Math.min(MAX_VERTICAL_ANGLE, newVerticalAngleRaw));
        Update();
    } // HandleYMouseMovement

    private void Update() {
        // Compute new forward vector.
        forward.set(Z_POS);
        horizontalRotation.fromAngleAxis((float)Math.toRadians(angleHorizontal), Y_POS);
        horizontalRotation.multLocal(forward);

        // Compute total rotation
        left.set(Y_POS).crossLocal(forward).normalizeLocal();
        verticalRotation.fromAngleNormalAxis((float)Math.toRadians(angleVertical), left);
        Quaternion totalRotation = verticalRotation.mult(horizontalRotation);
        camera.setRotation(totalRotation);
    } // Update

    public void registerWithInput(InputManager inputManager) {
        this.inputManager = inputManager;

        // both mouse and button - rotation of cam
        inputManager.addMapping(MCCAM_ROTATELEFT, new MouseAxisTrigger(MouseInput.AXIS_X, true),
                new KeyTrigger(KeyInput.KEY_LEFT));

        inputManager.addMapping(MCCAM_ROTATERIGHT, new MouseAxisTrigger(MouseInput.AXIS_X, false),
                new KeyTrigger(KeyInput.KEY_RIGHT));

        inputManager.addMapping(MCCAM_ROTATEUP, new MouseAxisTrigger(MouseInput.AXIS_Y, false),
                new KeyTrigger(KeyInput.KEY_UP));

        inputManager.addMapping(MCCAM_ROTATEDOWN, new MouseAxisTrigger(MouseInput.AXIS_Y, true),
                new KeyTrigger(KeyInput.KEY_DOWN));

        // keyboard only WASD for movement and WZ for rise/lower height
        inputManager.addMapping(MCCAM_MOVELEFT, new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping(MCCAM_MOVERIGHT, new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping(MCCAM_MOVEFORWARD, new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping(MCCAM_MOVEBACKWARD, new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping(MCCAM_MOVEUP, new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping(MCCAM_MOVEDOWN, new KeyTrigger(KeyInput.KEY_LSHIFT));

        inputManager.addListener(this, mappings);
        inputManager.setCursorVisible(false);
    } // registerWithInput

    public void unregisterInput(){
        if (inputManager == null) {
            return;
        }

        for (String s : mappings) {
            if (inputManager.hasMapping(s)) {
                inputManager.deleteMapping( s );
            }
        }

        inputManager.removeListener(this);
    } // unregisterInput

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {

    } // onAction

    @Override
    public void onAnalog(String name, float value, float tpf) {
//        System.out.println("Received analog input: "+name+", "+value+", "+tpf+" -> "+value/tpf);
        switch (name) {
            // Not sure what "value" is for mouse movement - movement in pixels, scaled by some amount?
            //  Fraction of the screen?
            case MCCAM_ROTATEDOWN:
                HandleYMouseMovement(value);
                break;
            case MCCAM_ROTATEUP:
                HandleYMouseMovement(-value);
                break;
            case MCCAM_ROTATELEFT:
                HandleXMouseMovement(-value);
                break;
            case MCCAM_ROTATERIGHT:
                HandleXMouseMovement(value);
                break;
            case MCCAM_MOVELEFT:
                camera.setLocation(position.scaleAdd(sidewaysSpeed, left, position));
				break;
            case MCCAM_MOVERIGHT:
                camera.setLocation(position.scaleAdd(-sidewaysSpeed, left, position));
				break;
            case MCCAM_MOVEUP:
                camera.setLocation(position.scaleAdd(verticalSpeed, Y_POS, position));
				break;
            case MCCAM_MOVEDOWN:
                camera.setLocation(position.scaleAdd(-verticalSpeed, Y_POS, position));
				break;
            case MCCAM_MOVEFORWARD:
                camera.setLocation(position.scaleAdd(forwardsSpeed, forward, position));
				break;
            case MCCAM_MOVEBACKWARD:
                camera.setLocation(position.scaleAdd(-forwardsSpeed, forward, position));
				break;
        }
    } // onAnalog
}
