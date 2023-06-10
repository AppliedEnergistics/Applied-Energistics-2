package appeng.client.guidebook.scene.annotation;

import java.util.OptionalDouble;

import org.apache.commons.lang3.tuple.Pair;
import org.joml.Intersectionf;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import appeng.client.guidebook.color.ColorValue;

public final class InWorldLineAnnotation extends InWorldAnnotation {
    public static final float DEFAULT_THICKNESS = 0.5f / 16f;
    private final Vector3f from;
    private final Vector3f to;
    private final ColorValue color;
    private final float thickness;

    public InWorldLineAnnotation(Vector3f from, Vector3f to, ColorValue color, float thickness) {
        this.from = from;
        this.to = to;
        this.color = color;
        this.thickness = thickness;
    }

    public InWorldLineAnnotation(Vector3f from, Vector3f to, ColorValue color) {
        this(from, to, color, DEFAULT_THICKNESS);
    }

    /**
     * Tests if the given ray intersects this highlighted box.
     */
    public OptionalDouble intersect(Vector3f rayOrigin, Vector3f rayDir) {
        // Convert the ray to a line segment
        var rayTo = new Vector3f(rayOrigin).add(rayDir);

        var resultA = new Vector3f();
        var resultB = new Vector3f();
        var distance = Intersectionf.findClosestPointsLineSegments(
                from.x, from.y, from.z,
                to.x, to.y, to.z,

                rayOrigin.x, rayOrigin.y, rayOrigin.z,
                rayTo.x, rayTo.y, rayTo.z,
                resultA, resultB);

        if (distance > thickness * thickness / 4) {
            return OptionalDouble.empty();
        }

        var distanceFromOrigin = resultB.sub(rayOrigin).lengthSquared() / rayDir.lengthSquared();
        return OptionalDouble.of(distanceFromOrigin);
    }

    /**
     * Computes the screen bounding box for this line, given a view matrix to transform the start and end of the line
     * into screen space. Since lines are extruded, we compute the bounds around the 8 corners around both end-points.
     *
     * @return A pair of the min and max screen coordinates.
     */
    public Pair<Vector2f, Vector2f> getScreenBounds(Matrix4f viewMatrix) {
        var minScreen = new Vector3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        var maxScreen = new Vector3f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);

        var tmpPos = new Vector3f();
        for (var i = 0; i <= 1; i++) {
            var p = i == 0 ? from : to;
            for (var xCorner = 0; xCorner <= 1; xCorner++) {
                for (var yCorner = 0; yCorner <= 1; yCorner++) {
                    for (var zCorner = 0; zCorner <= 1; zCorner++) {
                        var x = xCorner == 0 ? (p.x - thickness / 2) : (p.x + thickness / 2);
                        var y = yCorner == 0 ? (p.y - thickness / 2) : (p.y + thickness / 2);
                        var z = zCorner == 0 ? (p.z - thickness / 2) : (p.z + thickness / 2);

                        viewMatrix.transformPosition(x, y, z, tmpPos);
                        minScreen.min(tmpPos);
                        maxScreen.max(tmpPos);
                    }
                }
            }
        }

        return Pair.of(new Vector2f(minScreen.x, minScreen.y), new Vector2f(maxScreen.x, maxScreen.y));
    }

    public Vector3f min() {
        return from;
    }

    public Vector3f max() {
        return to;
    }

    public ColorValue color() {
        return color;
    }

    public float thickness() {
        return thickness;
    }
}
