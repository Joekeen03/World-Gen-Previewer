package main.java.ruby_phantasia.world_gen_previewer.primitives;

import org.joml.*;

public interface Primitive {
    void SetScale(Vector3fc scale);
    void SetScale(float scale);
    void SetRotationXYZ(Vector3fc rotations);
    void SetRotationXYZ(float rotationX, float rotationY, float rotationZ);
    void SetPosition(Vector3fc position);
    void SetPosition(float x, float y, float z);

    Vector3fc[] GetVertices();
    int[] GetIndices();
    Vector3fc GetColor();
    Vector3fc GetPosition();
    Quaternionfc GetRotation();
    Vector3fc GetScale();
}
