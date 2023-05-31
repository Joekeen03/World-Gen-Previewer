package main.java.ruby_phantasia.world_gen_previewer.old;

import java.util.Objects;

public class Vector3f {
    public final float x;
    public final float y;
    public final float z;
    public Vector3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3f add(float dx, float dy, float dz) {
        return new Vector3f(x+dx, y+dy, z+dz);
    }

    public Vector3f add(Vector3f operand) {
        return add(operand.x, operand.y, operand.z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vector3f vector3f = (Vector3f) o;
        return Float.compare(vector3f.x, x) == 0 && Float.compare(vector3f.y, y) == 0 && Float.compare(vector3f.z, z) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return "Vector3f{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
