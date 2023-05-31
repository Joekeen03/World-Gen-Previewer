package main.java.ruby_phantasia.world_gen_previewer;

import org.joml.Vector3f;

import java.util.Arrays;

public class Cube {
    public static final int CORNER_ARRAY_SIZE = 2;
    public static final int N_VERTICES = CORNER_ARRAY_SIZE*CORNER_ARRAY_SIZE*CORNER_ARRAY_SIZE;
    // FIXME Should maybe use Vector3fc here
    public final Vector3f position; // Position of cube's center
    public final Vector3f[][][] corners; // Locations of each of cube's corners. Indices are x, y, z.
    public final Vector3f[][] mesh;
    public final CubeMesh cubeMesh;
    public Cube (final Vector3f position, final float size) {
        this.position = position;
        corners = new Vector3f[CORNER_ARRAY_SIZE][CORNER_ARRAY_SIZE][CORNER_ARRAY_SIZE];
        final float halfSize = 0.5f*size;
        corners[0][0][0] = new Vector3f(position).add(-halfSize, -halfSize, -halfSize);
        corners[0][0][1] = new Vector3f(position).add(-halfSize,-halfSize,halfSize);
        corners[0][1][0] = new Vector3f(position).add(-halfSize,halfSize,-halfSize);
        corners[0][1][1] = new Vector3f(position).add(-halfSize,halfSize,halfSize);
        corners[1][0][0] = new Vector3f(position).add(halfSize, -halfSize, -halfSize);
        corners[1][0][1] = new Vector3f(position).add(halfSize,-halfSize,halfSize);
        corners[1][1][0] = new Vector3f(position).add(halfSize,halfSize,-halfSize);
        corners[1][1][1] = new Vector3f(position).add(halfSize,halfSize,halfSize);
        Vector3f[][][] sides = {generateSideX(0), generateSideY(0), generateSideZ(0),
                                generateSideX(1), generateSideY(1), generateSideZ(1)};
        Vector3f[][] tempMesh = new Vector3f[6*2][3];
        for (int i = 0; i < 6; i++) {
            tempMesh[2*i] = sides[i][0];
            tempMesh[2*i+1] = sides[i][1];
        }
        mesh = tempMesh;
        cubeMesh = new CubeMesh(corners);
    }

    private Vector3f[][] generateSideX(int xIndex) {
        return new Vector3f[][] {{corners[xIndex][0][0], corners[xIndex][1][0], corners[xIndex][0][1]},
                {corners[xIndex][1][1], corners[xIndex][0][1], corners[xIndex][1][0]}};
    }

    private Vector3f[][] generateSideY(int yIndex) {
        return new Vector3f[][] {{corners[0][yIndex][0], corners[1][yIndex][0], corners[0][yIndex][1]},
                {corners[1][yIndex][1], corners[0][yIndex][1], corners[1][yIndex][0]}};
    }

    private Vector3f[][] generateSideZ(int zIndex) {
        return new Vector3f[][] {{corners[0][0][zIndex], corners[1][0][zIndex], corners[0][1][zIndex]},
                                 {corners[1][1][zIndex], corners[0][1][zIndex], corners[1][0][zIndex]}};
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

        final Vector3f[] vertices;
        final int[] indices;
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
//            int[][][] triangleIndices = {generateSideZIndices(true)};
            indices = Arrays.stream(triangleIndices).flatMap(Arrays::stream).flatMapToInt(Arrays::stream).toArray();
        }
    }
}
