package main.java.ruby_phantasia.world_gen_previewer.old;

import java.util.Arrays;

public class Matrix4f {

    public static final int SIZE = 4;
    public final float[][] matrix;
    public Matrix4f(float[][] matrix) {
        this.matrix = matrix;
    }
    public Matrix4f() {
        this(MatrixInitializationType.ZERO);
    }
    public Matrix4f(MatrixInitializationType initialType) {
        this.matrix = new float[SIZE][SIZE];
        switch (initialType) {
            case ZERO:
                break;
            case IDENTITY:
                for (int i = 0; i < SIZE; i++) {
                    this.matrix[i][i] = 1;
                }
                break;
        }
    }

    // Converts matrix to a 1D representation, row-by-row.
    //  Meaning, the first four elements are the first matrix row (matrix[0][0...3]), the next
    //  four are the second matrix row, and so on.
    public float[] FlattenMatrix() {
        float[] flattenedMatrix = new float[SIZE*SIZE];
        for (int row = 0; row < SIZE; row++) {
            for (int column = 0; column < SIZE; column++) {
                flattenedMatrix[row*4+column]=matrix[row][column];
            }
        }
        return flattenedMatrix;
    }

    // Returns this matrix times the other matrix; phrased mathematically, return = this*other.
    public Matrix4f Times(Matrix4f other) {
        float[][] product = new float[SIZE][SIZE];
        for (int productRow = 0; productRow < SIZE; productRow++) {
            for (int productColumn = 0; productColumn < SIZE; productColumn++) {
                float elementProduct = 0.0f;
                for (int multiplyIndex= 0; multiplyIndex < SIZE; multiplyIndex++) {
                    elementProduct += matrix[productRow][multiplyIndex]*other.matrix[multiplyIndex][productColumn];
                }
                product[productRow][productColumn] = elementProduct;
            }
        }
        return new Matrix4f(product);
    }

    // Todo:
    //  -Transpose
    //  -Add
    //  -Subtract

    public enum MatrixInitializationType {
        ZERO,
        IDENTITY
    }

    @Override
    public String toString() {
        return "Matrix4f{"
                + "matrix=["
                    + Arrays.toString(matrix[0]) + ','
                    + Arrays.toString(matrix[1]) + ','
                    + Arrays.toString(matrix[2]) + ','
                    + Arrays.toString(matrix[3])
                + "]}";
    }

}
