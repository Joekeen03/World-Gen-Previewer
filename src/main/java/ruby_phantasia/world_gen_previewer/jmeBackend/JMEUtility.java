package main.java.ruby_phantasia.world_gen_previewer.jmeBackend;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;
import org.joml.Vector4fc;

public class JMEUtility {
    public static ColorRGBA ConvertVec4fcToColorRGBA(Vector4fc vector) {
        return new ColorRGBA(vector.x(), vector.y(), vector.z(), vector.w());
    }

    public static ColorRGBA ConvertVec3fcAndAlphaToColorRGBA(Vector3fc vector, float alpha) {
        return new ColorRGBA(vector.x(), vector.y(), vector.z(), alpha);
    }

    public static Quaternion ConvertJOMLQuaternionToJMEQuaternion(Quaternionfc quaternion) {
        return new Quaternion(quaternion.x(), quaternion.y(), quaternion.z(), quaternion.w());
    }
}
