package main.java.ruby_phantasia.world_gen_previewer.primitives;

import org.joml.*;

import java.util.Arrays;
import java.util.stream.IntStream;

public class Cube implements Primitive {
    public static final int CORNER_ARRAY_SIZE = 2;
    public static final int N_VERTICES = CORNER_ARRAY_SIZE*CORNER_ARRAY_SIZE*CORNER_ARRAY_SIZE;
    public final CubeMesh cubeMesh;

    public final Vector3f position; // Position of cube's center
    private final Quaternionf rotation;
    private final Vector3f scale;
    private final Vector3f color;

    public Cube (final Vector3fc position, final float size) {
        this(position, size, new Vector3f(0.0f, 0.0f, 1.0f), new Vector3f(0.0f, 1.0f, 0.0f), new Vector3f(1.0f));
    }

    public Cube(final Vector3fc position, final float size, final Vector3fc facingVector, final Vector3fc upVector,
                final Vector3fc color) {
        this.position = new Vector3f(position);
        Vector3fc rightVector = facingVector.cross(upVector, new Vector3f());
        Vector3f upVectorRecalc = rightVector.cross(facingVector, new Vector3f());
        this.rotation = new Quaternionf().setFromUnnormalized(new Matrix3f(rightVector, upVectorRecalc, facingVector));
        this.scale = new Vector3f(1.0f);
        this.color = new Vector3f(color);

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
    public void SetScale(Vector3fc scale) {
        this.scale.set(scale);
    }

    @Override
    public void SetScale(float scale) {
        this.scale.set(scale);
    }

    @Override
    public void SetPosition(Vector3fc position) {
        this.position.set(position);
    }

    @Override
    public void SetPosition(float x, float y, float z) {
        this.position.set(x, y, z);
    }

    @Override
    public void SetRotationXYZ(Vector3fc rotations) {
        this.rotation.rotationXYZ(rotations.x(), rotations.y(), rotations.z());
    }

    @Override
    public void SetRotationXYZ(float rotationX, float rotationY, float rotationZ) {
        this.rotation.rotationXYZ(rotationX, rotationY, rotationZ);
    }

    @Override
    public Vector3fc GetPosition() {
        return position;
    }

    @Override
    public Vector3fc[] GetVertices() {
        return cubeMesh.vertices;
    }

    @Override
    public int[] GetIndices() {
        return cubeMesh.indices;
    }

    @Override
    public Vector3fc GetColor() {
        return color;
    }

    @Override
    public Quaternionfc GetRotation() {
        return rotation;
    }

    @Override
    public Vector3fc GetScale() {
        return scale;
    }

    public static class CubeMesh {
        private int getFlatIndex(int x, int y, int z) {
            return (x*CORNER_ARRAY_SIZE+y)*CORNER_ARRAY_SIZE+z;
        }

        private int[][] generateSideXIndices(boolean positiveXSide) {
            final int xIndex = positiveXSide ? 1 : 0;
            int[][] indices = new int[][] {
                    {getFlatIndex(xIndex, 0, 0), getFlatIndex(xIndex, 1, 0), getFlatIndex(xIndex, 0, 1)},
                    {getFlatIndex(xIndex, 1, 1), getFlatIndex(xIndex, 0, 1), getFlatIndex(xIndex, 1, 0)}
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
            int[][] indices = new int[][] {{getFlatIndex(0, yIndex, 0), getFlatIndex(1, yIndex, 0), getFlatIndex(0, yIndex, 1)},
                    {getFlatIndex(1, yIndex, 1), getFlatIndex(0, yIndex, 1), getFlatIndex(1, yIndex, 0)}};
            // Reverse orientation of triangles to face outwards
            if (!positiveYSide) {
                SwapInPlace(indices[0], 0, 2);
                SwapInPlace(indices[1], 0, 2);
            }
            return indices;
        }

        private int[][] generateSideZIndices(boolean positiveZSide) {
            final int zIndex = positiveZSide ? 1 : 0;
            int[][] indices = new int[][] {{getFlatIndex(0, 0, zIndex), getFlatIndex(1, 0, zIndex), getFlatIndex(0, 1, zIndex)},
                    {getFlatIndex(1, 1, zIndex), getFlatIndex(0, 1, zIndex), getFlatIndex(1, 0, zIndex)}};
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
            // TODO Reverse indices computed in generateSide...methods, so we don't have to reverse them here.
            int[] reversedIndices = Arrays.stream(triangleIndices).flatMap(Arrays::stream).flatMapToInt(Arrays::stream).toArray();
            indices = IntStream.range(0, reversedIndices.length).map(index -> reversedIndices[reversedIndices.length-index-1]).toArray();
        }
    }
}
