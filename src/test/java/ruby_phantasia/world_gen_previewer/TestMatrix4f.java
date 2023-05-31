package ruby_phantasia.world_gen_previewer;

import main.java.ruby_phantasia.world_gen_previewer.old.Matrix4f;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class TestMatrix4f {
    final float[][] matrixArrayZero = new float[4][4];
    final float[][] matrixArrayIdentity = new float[][] {{1,0,0,0},{0,1,0,0},{0,0,1,0},{0,0,0,1}};
    final float[][] matrixArrayA = new float[][] {{1, 4, 3, 2}, {6, 7, 5, 8}, {9, 10, 12, 11}, {13, 14, 15, -16}};
    final float[][] matrixArrayB = new float[][] {{5, 3, -3, 3}, {1, 1, 55, 2}, {7, -3, -5, 2}, {-3, 0, -8, 31}};

    public void assertArrayNotDeeplyEquals(Object[] expected, Object actual[]) {
        assertFalse(Arrays.equals(expected, actual));
    }

    public void assertArrayNotEquals(float[] unexpected, float[] actual) {
        assertFalse(Arrays.equals(unexpected, actual));
    }

    @Test
    void TestDefaultConstructor() {
        assertArrayEquals( matrixArrayZero, new Matrix4f().matrix);
    }

    @Test
    void TestEnumConstructor() {
        final Matrix4f zeroSpecified = new Matrix4f(Matrix4f.MatrixInitializationType.ZERO);
        assertArrayEquals( matrixArrayZero, zeroSpecified.matrix);
        final Matrix4f identitySpecified = new Matrix4f(Matrix4f.MatrixInitializationType.IDENTITY);
        final Matrix4f identitySpecified2 = new Matrix4f(Matrix4f.MatrixInitializationType.IDENTITY);
        assertArrayEquals( matrixArrayIdentity, identitySpecified.matrix);
        assertArrayEquals( identitySpecified.matrix, identitySpecified2.matrix);
        assertArrayNotDeeplyEquals(new float[][]{{1, 0, 0, 1}, {0, 1, 0, 0}, {0, 0, 1, 0}, {0, 0, 0, 1}}, identitySpecified.matrix);
    }

    @Test
    void TestArrayConstructor() {

        assertArrayEquals( matrixArrayZero, new Matrix4f(matrixArrayZero).matrix);
        final Matrix4f matrixA = new Matrix4f(matrixArrayA);
        assertArrayEquals( matrixArrayA, matrixA.matrix);
        assertArrayNotDeeplyEquals( matrixArrayZero, matrixA.matrix);
    }

    @Test
    void TestFlattenMatrix() {
        final Matrix4f zero = new Matrix4f();
        final Matrix4f identity = new Matrix4f(Matrix4f.MatrixInitializationType.IDENTITY);
        final Matrix4f matrixA = new Matrix4f(matrixArrayA);
        final Matrix4f matrixB = new Matrix4f(matrixArrayB);

        float[] flattenedZero = new float[4*4];
        float[] flattenedIdentity = new float[] {1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1};
        float[] flattenedArrayA = new float[] {1, 4, 3, 2, 6, 7, 5, 8, 9, 10, 12, 11, 13, 14, 15, -16};
        float[] flattenedArrayB = new float[] {5, 3, -3, 3, 1, 1, 55, 2, 7, -3, -5, 2, -3, 0, -8, 31};

        assertArrayEquals(flattenedZero, zero.FlattenMatrix());
        assertArrayNotEquals( flattenedIdentity, zero.FlattenMatrix());
        assertArrayEquals( flattenedIdentity, identity.FlattenMatrix());
        assertArrayEquals( flattenedArrayA, matrixA.FlattenMatrix());
        assertArrayNotEquals( flattenedZero, matrixA.FlattenMatrix());
        matrixA.matrix[0][3] = 4; // Change should be reflected
        assertArrayNotEquals( flattenedArrayA, matrixA.FlattenMatrix());
        assertArrayEquals( flattenedArrayB, matrixB.FlattenMatrix());
    }

    @Test
    void TestMultiplyMatrixZero() {
        Matrix4f zero = new Matrix4f();
        Matrix4f identity = new Matrix4f(Matrix4f.MatrixInitializationType.IDENTITY);
        Matrix4f matrixA = new Matrix4f(matrixArrayA);
        assertArrayNotDeeplyEquals(matrixA.matrix, zero.matrix);
        assertArrayEquals( zero.matrix, zero.Times(matrixA).matrix);
        assertArrayEquals( zero.matrix, matrixA.Times(zero).matrix);
        assertArrayEquals( zero.matrix, zero.Times(identity).matrix);
        assertArrayEquals( zero.matrix, identity.Times(zero).matrix);
    }

    @Test
    void TestMultiplyMatrixIdentity() {
        Matrix4f identity = new Matrix4f(Matrix4f.MatrixInitializationType.IDENTITY);
        Matrix4f identity2 = new Matrix4f(Matrix4f.MatrixInitializationType.IDENTITY);
        Matrix4f matrixA = new Matrix4f(matrixArrayA);
        Matrix4f matrixB = new Matrix4f(matrixArrayB);
        assertArrayEquals( identity2.matrix, identity2.Times(identity).matrix);
        assertArrayEquals( identity2.matrix, identity.Times(identity2).matrix);
        assertArrayEquals( matrixA.matrix, identity.Times(matrixA).matrix);
        assertArrayEquals( matrixA.matrix, matrixA.Times(identity).matrix);
        assertArrayEquals( matrixB.matrix, matrixB.Times(identity).matrix);
        assertArrayNotDeeplyEquals(identity.matrix, matrixB.Times(identity).matrix);
    }

    @Test
    void TestMultiplyMatrixGeneral() {
        Matrix4f matrixA = new Matrix4f(matrixArrayA);
        Matrix4f matrixB = new Matrix4f(matrixArrayB);
        final float[][] matrixArrayATimesB = new float[][] {{24, -2, 186, 79}, {48, 10, 278, 290}, {106, 1, 375, 412}, {232, 8, 784, -399}};
        assertArrayEquals(matrixArrayATimesB, matrixA.Times(matrixB).matrix);
    }
}
