package main.java.ruby_phantasia.world_gen_previewer.jmeBackend;

import com.jme3.app.SimpleApplication;
import com.jme3.input.FlyByCamera;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import main.java.ruby_phantasia.world_gen_previewer.api.GenerationPrimitive;

public class JMEBackend extends SimpleApplication {
    private final GenerationPrimitive[] primitives;


    public JMEBackend(GenerationPrimitive[] primitives) {
        super();
        this.primitives = primitives;
    }

    @Override
    public void simpleInitApp() {
        PrimitiveToGeometryConvertor convertor = new PrimitiveToGeometryConvertor(assetManager);
        for (GenerationPrimitive primitive: primitives) {
            rootNode.attachChild(primitive.accept(convertor));
        }
        FlyByCamera cam;

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
