package main.java.ruby_phantasia.world_gen_previewer.jmeBackend;

import com.jme3.app.DebugKeysAppState;
import com.jme3.app.FlyCamAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.app.state.ConstantVerifierState;
import com.jme3.audio.AudioListenerState;
import com.jme3.input.FlyByCamera;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import main.java.ruby_phantasia.world_gen_previewer.api.GenerationPrimitive;

public class JMEBackend extends SimpleApplication {
    private final GenerationPrimitive[] primitives;
    private MinecraftCamera camera;

    public JMEBackend(GenerationPrimitive[] primitives) {
        super();
        stateManager.attach(new MinecraftCameraAppState());
        this.primitives = primitives;
    }

    @Override
    public void simpleInitApp() {
        stateManager.detach(stateManager.getState(FlyCamAppState.class));
        MinecraftCameraAppState cameraAppState = new MinecraftCameraAppState();
        cameraAppState.setCamera(new MinecraftCamera(cam));
        stateManager.attach(cameraAppState);
//        new MinecraftCamera(cam).registerWithInput(inputManager);
        PrimitiveToGeometryConvertor convertor = new PrimitiveToGeometryConvertor(assetManager);
        for (GenerationPrimitive primitive: primitives) {
            Geometry geometry = primitive.accept(convertor);
            if (primitive.alpha < 1.0f) {
                geometry.getMaterial().getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
                if (primitive.alpha > 0.0f) {
                    geometry.setQueueBucket(RenderQueue.Bucket.Translucent);
                } else { // Alpha == 0.0f; could just drop this primitive?
                    geometry.setQueueBucket(RenderQueue.Bucket.Transparent);
                }
            }
            rootNode.attachChild(geometry);
        }

        rootNode.addLight(new AmbientLight(new ColorRGBA(0.2f, 0.2f, 0.2f, 1.0f)));
        rootNode.addLight(new DirectionalLight(new Vector3f(-0.1f, 0.0f, -0.1f).normalizeLocal(), new ColorRGBA(0.2f, 0.2f, 0.2f, 1.0f)));
    }

    @Override
    public void simpleUpdate(float tpf) {
        
    }

    @Override
    public void simpleRender(RenderManager rm) {

    }
}
