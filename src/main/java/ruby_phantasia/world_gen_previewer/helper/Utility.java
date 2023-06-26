package main.java.ruby_phantasia.world_gen_previewer.helper;

import org.joml.Quaternionf;
import org.joml.Vector3fc;

public class Utility {
    /**
     * Precondition: localZ & localY are normalized, and are not approximately equal (distanceSquared >= EPSILON_SQUARED)
     * @param localZ Input direction to look at.
     * @param localY Input "up" direction.
     * @return
     */
    public static Quaternionf NewQuaternionFromTargetUpVectors(Vector3fc localZ, Vector3fc localY) {
        /**
         * Negate the localZ vector, as the lookAlong method applies a "camera rotation", instead of an object
         *  rotation, which uses the negative of the provided direction vector. Not really sure
         *  what exactly what they mean by a camera rotation.
         *  FIXME need to better understand this.
         */
        return new Quaternionf().lookAlong(-localZ.x(), -localZ.y(), -localZ.z(),
                                localY.x(), localY.y(), localY.z());
//        return new Quaternionf().lookAlong(localZ.negate(new Vector3f()), localY);
    }

    /**
     * Returns a new quaternion representing a rotation from the +y axis to the target vector.
     * @param target
     * @return
     */
    public static Quaternionf NewQuaternionFromTargetDirection(Vector3fc target) {
        return new Quaternionf().rotationTo(DefaultVectors.Y_POSITIVE, target);
    }
}
