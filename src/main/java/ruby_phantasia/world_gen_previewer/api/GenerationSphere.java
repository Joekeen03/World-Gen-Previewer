package main.java.ruby_phantasia.world_gen_previewer.api;

import org.joml.Vector3fc;
import org.joml.Vector4fc;

public class GenerationSphere extends GenerationPrimitive {
    public final float radius;
    // FIXME Do these data types make sense?
    public final Vector3fc position;
    public final Vector3fc color;


    public GenerationSphere(float radius, Vector3fc position, Vector3fc color) {
        this(radius, position, color, 1.0f);
    }

    public GenerationSphere(float radius, Vector3fc position, Vector3fc color, float alpha) {
        super(alpha);
        this.radius = radius;
        this.position = position;
        this.color = color;
    }

    @Override
    public <T> T accept(GenerationPrimitiveVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
