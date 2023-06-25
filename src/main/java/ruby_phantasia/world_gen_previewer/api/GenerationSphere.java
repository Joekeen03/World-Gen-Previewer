package main.java.ruby_phantasia.world_gen_previewer.api;

import org.joml.Vector3fc;
import org.joml.Vector4fc;

public class GenerationSphere implements GenerationPrimitive {
    public final float radius;
    // FIXME Do these data types make sense?
    public final Vector3fc position;
    public final Vector4fc color;

    public GenerationSphere(float radius, Vector3fc position, Vector4fc color) {
        this.radius = radius;
        this.position = position;
        this.color = color;
    }

    @Override
    public <T> T accept(GenerationPrimitiveVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
