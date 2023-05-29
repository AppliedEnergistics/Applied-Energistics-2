package appeng.client.guidebook.scene.annotation;

import java.util.OptionalDouble;

import org.apache.commons.lang3.tuple.Pair;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

/**
 * A {@link SceneAnnotation} that renders in-world.
 */
public abstract class InWorldAnnotation extends SceneAnnotation {

    /**
     * Test intersection between a 3D ray and this in-world annotations shape.
     *
     * @return Absent if no intersection exists, otherwise a number between 0 and 1 indicating the distance from the ray
     *         origin (along the ray) that the intersection closest to the origin occurred.
     */
    public abstract OptionalDouble intersect(Vector3f rayOrigin, Vector3f rayDir);

    public abstract Pair<Vector2f, Vector2f> getScreenBounds(Matrix4f viewMatrix);
}
