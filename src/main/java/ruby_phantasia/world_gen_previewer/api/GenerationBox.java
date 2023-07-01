package main.java.ruby_phantasia.world_gen_previewer.api;

import org.joml.Vector3fc;

/**
 * Box w/ its local origin (center of rotation) at its center.
 */
public class GenerationBox extends GenerationPrimitive {

    public final Vector3fc position;
    public final Vector3fc dimensions; // Dimensions of cube along x, y, and z axes
    public final Vector3fc color;

    public GenerationBox(Vector3fc position, Vector3fc dimensions, Vector3fc color) {
        this(position, dimensions, color, 1.0f);
    }

    public GenerationBox(Vector3fc position, Vector3fc dimensions, Vector3fc color, float alpha) {
        super(alpha);
        this.position = position;
        this.dimensions = dimensions;
        this.color = color;
    }

    @Override
    public <T> T accept(GenerationPrimitiveVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
