package main.java.ruby_phantasia.world_gen_previewer.primitives;

import it.unimi.dsi.fastutil.ints.IntImmutableList;
import it.unimi.dsi.fastutil.objects.ObjectImmutableList;
import main.java.ruby_phantasia.world_gen_previewer.helper.DefaultVectors;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * A cone; rotational origin is located at the center of its base.
 */
public class Cone extends Primitive {
    public final float radius;
    public final float length;
    private final Vector3fc[] vertices;
    private final int[] indices;

    private static final int N_INDICES_PER_PERIMETER_VERTEX = 2*3; // Two faces per perimeter vertex, and 3 vertices per face.
    private static final int BASE_CENTER_VERTEX_INDEX = 0;
    private static final int TIP_VERTEX_INDEX = 1;
    private static final int PERIMETER_VERTICES_START_INDEX = 2;

    // Default # vertices per unit circumference; so a radius=1 circle would have 1*2*pi*DEFAULT... vertices.
    private static final int DEFAULT_PERIMETER_RESOLUTION = 5;

    public Cone(Vector3fc position, Vector3fc target, float radius, float length) {
        this(position, target, radius, length,new Vector3f(1.0f));
    }

    public Cone(Vector3fc position, Vector3fc target, float radius, float length, Vector3fc color) {
        this(position, target, radius, length, (int)Math.ceil(2*Math.PI*radius*DEFAULT_PERIMETER_RESOLUTION), color);
    }

    /**
     *
     * @param radius Base radius
     * @param length Length
     * @param nPerimeterVertices number of vertices used for the base's perimeter. I.e. 3 -> 3 vertices
     *                           describing the edge (effectively a triangular prism?)
     */
    public Cone(Vector3fc position, Vector3fc target, float radius, float length, int nPerimeterVertices, Vector3fc color) {
        super(position, NewQuaternionFromTargetUpVectors(target, DefaultVectors.Y_AXIS), color);
        this.radius = radius;
        this.length = length;

        Vector3fc baseCenterVertex = new Vector3f(0.0f, 0.0f, 0.0f);
        Vector3fc tipVertex = new Vector3f(0.0f, length, 0.0f);
        // Maybe add ability to offset the perimeter vertices'
        vertices = new Vector3fc[nPerimeterVertices+2];
        vertices[BASE_CENTER_VERTEX_INDEX] = baseCenterVertex;
        vertices[TIP_VERTEX_INDEX] = tipVertex;
        for (int vertexIndex = 0; vertexIndex < nPerimeterVertices; vertexIndex++) {
            float angle = (float)(Math.PI*2/(double)nPerimeterVertices*(double)vertexIndex);
            vertices[PERIMETER_VERTICES_START_INDEX+vertexIndex] = new Vector3f(radius, 0.0f, 0.0f).rotateY(angle);
        }
        indices = new int[nPerimeterVertices*N_INDICES_PER_PERIMETER_VERTEX];
        for (int vertexIndex = 0; vertexIndex < nPerimeterVertices; vertexIndex++) {
            int index = vertexIndex*6;
            indices[index+2] = BASE_CENTER_VERTEX_INDEX;
            indices[index+1] = PERIMETER_VERTICES_START_INDEX+vertexIndex;
            indices[index] = PERIMETER_VERTICES_START_INDEX+(1+vertexIndex)%nPerimeterVertices;

            indices[index+3] = TIP_VERTEX_INDEX;
            indices[index+4] = PERIMETER_VERTICES_START_INDEX+vertexIndex;
            indices[index+5] = PERIMETER_VERTICES_START_INDEX+(1+vertexIndex)%nPerimeterVertices;
        }
    }

    @Override
    public ObjectImmutableList<Vector3fc> GetVertices() {
        return new ObjectImmutableList<Vector3fc>(vertices);
    }

    @Override
    public IntImmutableList GetIndices() {
        return new IntImmutableList(indices);
    }
}
