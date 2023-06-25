package main.java.ruby_phantasia.world_gen_previewer.jmeBackend;

import com.jme3.math.ColorRGBA;
import org.joml.Vector4fc;

public class JMEUtility {
    public static ColorRGBA ConvertVec4fcToColorRGBA(Vector4fc vector) {
        return new ColorRGBA(vector.x(), vector.y(), vector.z(), vector.w());
    }
}
