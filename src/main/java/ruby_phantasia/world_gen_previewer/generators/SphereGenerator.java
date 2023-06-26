package main.java.ruby_phantasia.world_gen_previewer.generators;

import main.java.ruby_phantasia.world_gen_previewer.api.GenerationPrimitive;
import main.java.ruby_phantasia.world_gen_previewer.api.GenerationSphere;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import java.util.Random;

public class SphereGenerator implements Generator {
    private static final float PLUS_MINUS_RANGE = 4.0f;
    private static final float ABSOLUTE_RANGE = PLUS_MINUS_RANGE*2;

    private final long seed;
    private final int nSpheres;

    public SphereGenerator(long seed, int nSpheres) {
        this.seed = seed;
        this.nSpheres = nSpheres;
    }

    public GenerationPrimitive[] GeneratePrimitives() {
        Random rng = new Random(seed);
        GenerationPrimitive[] spheres = new GenerationPrimitive[nSpheres];
        for (int i = 0; i < nSpheres; i++) {
            float radius = rng.nextFloat()*0.2f+0.2f;
            Vector3fc position = new Vector3f(rng.nextFloat()*ABSOLUTE_RANGE-PLUS_MINUS_RANGE, rng.nextFloat()*ABSOLUTE_RANGE-PLUS_MINUS_RANGE, rng.nextFloat()*ABSOLUTE_RANGE-PLUS_MINUS_RANGE);
            Vector4fc color = new Vector4f(rng.nextFloat(), rng.nextFloat(), rng.nextFloat(), rng.nextFloat());
            GenerationSphere sphere = new GenerationSphere(radius, position, color);
            spheres[i] = sphere;
        }
        return spheres;
    }
}
