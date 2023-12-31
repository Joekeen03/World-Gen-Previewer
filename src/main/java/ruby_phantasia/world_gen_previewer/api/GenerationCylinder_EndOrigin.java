package main.java.ruby_phantasia.world_gen_previewer.api;

import main.java.ruby_phantasia.world_gen_previewer.helper.DefaultVectors;
import main.java.ruby_phantasia.world_gen_previewer.helper.Utility;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;
import org.joml.Vector4fc;

/**
 * A cylinder with its local origin (centerpoint of rotation) at the center of one end, not centered between its ends.
 * Specifically, if not rotated at all, the cylinder extends from the local origin along the +z axis.
 */
public class GenerationCylinder_EndOrigin extends GenerationPrimitive {
    public final float radius;
    public final float length;
    public final Vector3fc endPosition;
    public final Quaternionfc rotation;
    public final Vector3fc color;


    public GenerationCylinder_EndOrigin(float radius, float length, Vector3fc endPosition, Vector3fc target, Vector3fc color) {
        this(radius, length, endPosition, target, color, 1.0f);
    }

    public GenerationCylinder_EndOrigin(float radius, float length, Vector3fc endPosition, Vector3fc target, Vector3fc color, float alpha) {
        super(alpha);
        this.radius = radius;
        this.length = length;
        this.endPosition = endPosition;
        this.rotation = Utility.NewQuaternionFromTargetDirection(target);
        this.color = color;
    }

    @Override
    public <T> T accept(GenerationPrimitiveVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
