package main.java.ruby_phantasia.world_gen_previewer.primitives;

import it.unimi.dsi.fastutil.ints.IntImmutableList;
import it.unimi.dsi.fastutil.ints.IntLists;
import it.unimi.dsi.fastutil.objects.ObjectImmutableList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import main.java.ruby_phantasia.world_gen_previewer.helper.DefaultVectors;
import org.joml.*;

import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A cube with its rotational origin located at its center.
 */
public class Cube extends Primitive {
    public static final int CORNER_ARRAY_SIZE = 2;
    public static final int N_VERTICES = CORNER_ARRAY_SIZE*CORNER_ARRAY_SIZE*CORNER_ARRAY_SIZE;
    public final CubeMesh cubeMesh;

    public Cube (final Vector3fc position, final float size) {
        this(position, size, DefaultVectors.Z_POSITIVE, DefaultVectors.Y_POSITIVE, new Vector3f(1.0f));
    }

    public Cube(final Vector3fc position, final float size, final Vector3fc facingVector, final Vector3fc upVector,
                final Vector3fc color) {
        super(position, NewQuaternionFromTargetUpVectors(facingVector, upVector), color);

        Vector3f[][][] corners = new Vector3f[CORNER_ARRAY_SIZE][CORNER_ARRAY_SIZE][CORNER_ARRAY_SIZE];
        final float halfSize = 0.5f*size;
        corners[0][0][0] = new Vector3f(-halfSize, -halfSize, -halfSize);
        corners[0][0][1] = new Vector3f(-halfSize,-halfSize,halfSize);
        corners[0][1][0] = new Vector3f(-halfSize,halfSize,-halfSize);
        corners[0][1][1] = new Vector3f(-halfSize,halfSize,halfSize);
        corners[1][0][0] = new Vector3f(halfSize, -halfSize, -halfSize);
        corners[1][0][1] = new Vector3f(halfSize,-halfSize,halfSize);
        corners[1][1][0] = new Vector3f(halfSize,halfSize,-halfSize);
        corners[1][1][1] = new Vector3f(halfSize,halfSize,halfSize);
        cubeMesh = new CubeMesh(corners);
    }

    @Override
    public ObjectImmutableList<Vector3fc> GetVertices() {
        return new ObjectImmutableList<Vector3fc>(cubeMesh.vertices);
    }

    @Override
    public IntImmutableList GetIndices() {
        return new IntImmutableList(cubeMesh.indices);
    }

    public static class CubeMesh {
        private int getFlatIndex(int x, int y, int z) {
            return (x*CORNER_ARRAY_SIZE+y)*CORNER_ARRAY_SIZE+z;
        }

        private int[][] generateSideXIndices(boolean positiveXSide) {
            final int xIndex = positiveXSide ? 1 : 0;
            int[][] indices = new int[][] {
                    {getFlatIndex(xIndex, 0, 1), getFlatIndex(xIndex, 1, 0), getFlatIndex(xIndex, 0, 0)},
                    {getFlatIndex(xIndex, 1, 0), getFlatIndex(xIndex, 0, 1), getFlatIndex(xIndex, 1, 1)}
            };
            // Reverse orientation of triangles to face outwards
            if (positiveXSide) {
                SwapInPlace(indices[0], 0, 2);
                SwapInPlace(indices[1], 0, 2);
            }
            return indices;
        }

        private int[][] generateSideYIndices(boolean positiveYSide) {
            final int yIndex = positiveYSide ? 1 : 0;
            int[][] indices = new int[][] {
                    {getFlatIndex(0, yIndex, 1), getFlatIndex(1, yIndex, 0), getFlatIndex(0, yIndex, 0)},
                    {getFlatIndex(1, yIndex, 0), getFlatIndex(0, yIndex, 1), getFlatIndex(1, yIndex, 1)}
            };
            // Reverse orientation of triangles to face outwards
            if (!positiveYSide) {
                SwapInPlace(indices[0], 0, 2);
                SwapInPlace(indices[1], 0, 2);
            }
            return indices;
        }

        private int[][] generateSideZIndices(boolean positiveZSide) {
            final int zIndex = positiveZSide ? 1 : 0;
            int[][] indices = new int[][] {
                    {getFlatIndex(0, 1, zIndex), getFlatIndex(1, 0, zIndex), getFlatIndex(0, 0, zIndex)},
                    {getFlatIndex(1, 0, zIndex), getFlatIndex(0, 1, zIndex), getFlatIndex(1, 1, zIndex)}
            };
            // Reverse orientation of triangles to face outwards
            if (positiveZSide) {
                SwapInPlace(indices[0], 0, 2);
                SwapInPlace(indices[1], 0, 2);
            }
            return indices;
        }

        private void SwapInPlace(int[] array, int indexA, int indexB) {
            int temp = array[indexA];
            array[indexA] = array[indexB];
            array[indexB] = temp;
        }

        public final Vector3f[] vertices;
        public final int[] indices;
        CubeMesh(Vector3f[][][] corners) {
            vertices = new Vector3f[N_VERTICES];
            for (int x = 0; x < CORNER_ARRAY_SIZE; x++) {
                for (int y = 0; y < CORNER_ARRAY_SIZE; y++) {
                    for (int z = 0; z < CORNER_ARRAY_SIZE; z++) {
                        vertices[getFlatIndex(x, y, z)] = corners[x][y][z];
                    }
                }
            }
            int[][][] triangleIndices = {generateSideXIndices(false), generateSideYIndices(false), generateSideZIndices(false),
                    generateSideXIndices(true), generateSideYIndices(true), generateSideZIndices(true)};
            indices = Arrays.stream(triangleIndices).flatMap(Arrays::stream).flatMapToInt(Arrays::stream).toArray();
        }
    }
}
