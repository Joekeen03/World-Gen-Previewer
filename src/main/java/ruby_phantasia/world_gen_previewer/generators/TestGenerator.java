package main.java.ruby_phantasia.world_gen_previewer.generators;

import main.java.ruby_phantasia.world_gen_previewer.api.GenerationCylinder_EndOrigin;
import main.java.ruby_phantasia.world_gen_previewer.api.GenerationPrimitive;
import main.java.ruby_phantasia.world_gen_previewer.api.GenerationSphere;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class TestGenerator implements Generator {
    @Override
    public GenerationPrimitive[] GeneratePrimitives() {
        GenerationPrimitive[] primitives = {
                new GenerationSphere(1.0f, new Vector3f(0.0f, 0.0f, 0.0f), new Vector4f(1.0f, 0.0f, 0.0f, 1.0f)),
                new GenerationCylinder_EndOrigin(1.0f, 3.0f, new Vector3f(2.0f, 0.0f, 0.0f), new Vector3f(0.0f, 1.0f, 0.0f).normalize(), new Vector4f(0.0f, 1.0f, 0.0f, 1.0f))
        };
        return primitives;
    }
}
