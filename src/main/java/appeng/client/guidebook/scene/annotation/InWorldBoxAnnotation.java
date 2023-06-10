package appeng.client.guidebook.scene.annotation;

import java.util.OptionalDouble;

import org.apache.commons.lang3.tuple.Pair;
import org.joml.Intersectionf;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import net.minecraft.core.BlockPos;

import appeng.client.guidebook.color.ColorValue;

public final class InWorldBoxAnnotation extends InWorldAnnotation {
    public static final float DEFAULT_THICKNESS = 0.5f / 16f;
    private final Vector3f minCorner;
    private final Vector3f maxCorner;
    private final ColorValue color;
    private final float thickness;

    public InWorldBoxAnnotation(Vector3f minCorner, Vector3f maxCorner, ColorValue color, float thickness) {
        this.minCorner = minCorner;
        this.maxCorner = maxCorner;
        this.color = color;
        this.thickness = thickness;
    }

    public InWorldBoxAnnotation(Vector3f minCorner, Vector3f maxCorner, ColorValue color) {
        this(minCorner, maxCorner, color, DEFAULT_THICKNESS);
    }

    public static InWorldBoxAnnotation forBlock(BlockPos pos, ColorValue color) {
        return new InWorldBoxAnnotation(
                new Vector3f(pos.getX(), pos.getY(), pos.getZ()),
                new Vector3f(pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1),
                color);
    }

    /**
     * Tests if the given ray intersects this highlighted box.
     */
    public OptionalDouble intersect(Vector3f rayOrigin, Vector3f rayDir) {
        var minExtruded = new Vector3f(minCorner).sub(thickness, thickness, thickness);
        var maxExtruded = new Vector3f(maxCorner).add(thickness, thickness, thickness);
        var intersection = new Vector2f();

        if (Intersectionf.intersectRayAab(
                rayOrigin,
                rayDir,
                minExtruded,
                maxExtruded,
                intersection)) {
            return OptionalDouble.of(intersection.x);
        }
        return OptionalDouble.empty();
    }

    /**
     * Computes the screen bounding box for this highlighted box, given a view matrix to transform the corners of this
     * box in to screen space.
     *
     * @return A pair of the min and max screen coordinates.
     */
    public Pair<Vector2f, Vector2f> getScreenBounds(Matrix4f viewMatrix) {
        var minScreen = new Vector3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        var maxScreen = new Vector3f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);

        var tmpPos = new Vector3f();
        for (var xCorner = 0; xCorner <= 1; xCorner++) {
            for (var yCorner = 0; yCorner <= 1; yCorner++) {
                for (var zCorner = 0; zCorner <= 1; zCorner++) {
                    var x = xCorner == 0 ? (minCorner.x - thickness) : (maxCorner.x + thickness);
                    var y = yCorner == 0 ? (minCorner.y - thickness) : (maxCorner.y + thickness);
                    var z = zCorner == 0 ? (minCorner.z - thickness) : (maxCorner.z + thickness);

                    viewMatrix.transformPosition(x, y, z, tmpPos);
                    minScreen.min(tmpPos);
                    maxScreen.max(tmpPos);
                }
            }
        }

        return Pair.of(new Vector2f(minScreen.x, minScreen.y), new Vector2f(maxScreen.x, maxScreen.y));
    }

    public Vector3f min() {
        return minCorner;
    }

    public Vector3f max() {
        return maxCorner;
    }

    public ColorValue color() {
        return color;
    }

    public float thickness() {
        return thickness;
    }
}
