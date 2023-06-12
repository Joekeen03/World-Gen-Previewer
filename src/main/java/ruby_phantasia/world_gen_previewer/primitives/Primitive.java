package main.java.ruby_phantasia.world_gen_previewer.primitives;

import it.unimi.dsi.fastutil.ints.IntImmutableList;
import it.unimi.dsi.fastutil.objects.ObjectImmutableList;
import main.java.ruby_phantasia.world_gen_previewer.lwjglBackend.Vertex;
import main.java.ruby_phantasia.world_gen_previewer.helper.DefaultVectors;
import org.joml.*;

public abstract class Primitive {
    protected final Vector3f scale;
    protected final Vector3f position;
    protected final Quaternionf rotation;

    private static final float EPSILON = 0.00000001f;
    private static final float EPSILON_SQUARED = EPSILON*EPSILON;

    public Primitive(Vector3fc position, Quaternionfc rotation) {
        this.scale = new Vector3f(1.0f);
        this.position = new Vector3f(position);
        this.rotation = new Quaternionf(rotation);
    }

    public void SetScale(Vector3fc scale) {
        this.scale.set(scale);
    }
    public void SetScale(float scale) {
        this.scale.set(scale);
    }

    public void SetPosition(Vector3fc position) {
        this.position.set(position);
    }

    public void SetPosition(float x, float y, float z) {
        this.position.set(x, y, z);
    }

    public void SetRotationXYZ(Vector3fc rotations) {
        this.rotation.rotationXYZ(rotations.x(), rotations.y(), rotations.z());
    }

    public void SetRotationXYZ(float rotationX, float rotationY, float rotationZ) {
        this.rotation.rotationXYZ(rotationX, rotationY, rotationZ);
    }

    public Vector3fc GetPosition() {
        return position;
    }

    public Quaternionfc GetRotation() {
        return rotation;
    }

    public Vector3fc GetScale() {
        return scale;
    }

    public abstract ObjectImmutableList<Vertex> GetVertices();
    public abstract IntImmutableList GetIndices();

    /**
     * Precondition: localZ & localY are normalized, and are not approximately equal (distanceSquared >= EPSILON_SQUARED)
     * @param localZ Input direction to look at.
     * @param localY Input "up" direction.
     * @return
     */
    public static Quaternionf NewQuaternionFromTargetUpVectors(Vector3fc localZ, Vector3fc localY) {
        /**
         * Negate the localZ vector, as the lookAlong method applies a "camera rotation", instead of an object
         *  rotation, which uses the negative of the provided direction vector. Not really sure
         *  what exactly what they mean by a camera rotation.
         *  FIXME need to better understand this.
         */
        return new Quaternionf().lookAlong(-localZ.x(), -localZ.y(), -localZ.z(),
                                localY.x(), localY.y(), localY.z());
//        return new Quaternionf().lookAlong(localZ.negate(new Vector3f()), localY);
    }

    /**
     * Returns a new quaternion representing a rotation from the +y axis to the target vector.
     * @param target
     * @return
     */
    public static Quaternionf NewQuaternionFromTargetDirection(Vector3fc target) {
        return new Quaternionf().rotationTo(DefaultVectors.Y_POSITIVE, target);
    }
}
