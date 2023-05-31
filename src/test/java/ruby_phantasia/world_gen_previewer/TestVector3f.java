package ruby_phantasia.world_gen_previewer;

import main.java.ruby_phantasia.world_gen_previewer.old.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class TestVector3f {

    private final Vector3f a = new Vector3f(1, 1, 1);
    private final Vector3f a1 = new Vector3f(1, 1, 1);
    private final Vector3f b = new Vector3f(3, 1, 2);
    private final Vector3f zero = new Vector3f(0, 0, 0);

    @Test
    void equality() {
        assertEquals(a, a);
        assertEquals(a, a1);
        assertNotEquals(a, b);
    }

    @Test
    void addition() {
        assertEquals(a, a.add(0, 0, 0));
        assertEquals(a, a.add(zero));
        assertNotEquals(a, a.add(0, 0, 1));
        assertNotEquals(a, a.add(0, 1, 0));
        assertNotEquals(a, a.add(1, 0, 0));
        assertNotEquals(a, a.add(1, 1, 0));
        assertNotEquals(a, a.add(b));
        assertEquals(new Vector3f(4, 2, 3), a.add(b));
        assertEquals(new Vector3f(4, 2, 3), b.add(a));
        assertEquals(new Vector3f(4, 6, 8), b.add(1,5,6));
    }
}
