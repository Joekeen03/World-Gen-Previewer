package main.java.ruby_phantasia.world_gen_previewer.lwjglBackend;

import main.java.ruby_phantasia.world_gen_previewer.helper.Constants;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;

/**
 * Single vertex with a color
 */
public class Vertex {
    public static final int POSITION_OFFSET_BYTES = 0;
    public static final int COLOR_OFFSET_BYTES = Constants.GL_FLOAT_SIZE*Constants.VECTOR3F_N_FLOATS;
    public static final int N_FLOATS = Constants.VECTOR3F_N_FLOATS+Constants.VECTOR4F_N_FLOATS;
    public static final int N_FLOATS_BYTES = Constants.GL_FLOAT_SIZE* N_FLOATS;

    public final Vector3fc position;
    public final Vector4fc color; // RGBA color; R(x), G(y), B(z), A(w)

    public Vertex(Vector3fc position, Vector4fc color) {
        this.position = new Vector3f(position);
        this.color = new Vector4f(color);
    }
}
