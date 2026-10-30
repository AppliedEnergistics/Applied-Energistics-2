package appeng.client.render.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CompassNeedleTest {
    private static final long SECOND = 1_000_000_000L;
    private static final float DELTA = 0.01f;

    private final CompassNeedle needle = new CompassNeedle();
    private long nanos;

    /**
     * Runs the simulation at the given frame rate and returns the last angle.
     */
    private float run(float seconds, int fps, float caseAngle, @Nullable Float bearing, float spinSpeed) {
        var result = 0f;
        var frames = Math.round(seconds * fps);
        for (var i = 0; i < frames; i++) {
            nanos += SECOND / fps;
            result = needle.update(nanos, caseAngle, bearing, spinSpeed);
        }
        return result;
    }

    @Test
    void testSettlesOnBearing() {
        needle.update(nanos, 0, 1f, 0);
        assertEquals(1f, run(10, 60, 0, 1f, 0), DELTA);
    }

    @Test
    void testOvershootsBearing() {
        needle.update(nanos, 0, 1f, 0);

        var highest = 0f;
        for (var i = 0; i < 180; i++) {
            highest = Math.max(highest, run(1 / 60f, 60, 0, 1f, 0));
        }

        assertEquals(1.37f, highest, 0.05f);
    }

    @Test
    void testTakesShortestPathAcrossWrapAround() {
        needle.update(nanos, 0, 3f, 0);
        run(10, 60, 0, 3f, 0);

        for (var i = 0; i < 300; i++) {
            var angle = run(1 / 60f, 60, 0, -3f, 0);
            assertTrue(Math.abs(angle) > 2.5f, "needle went the long way around: " + angle);
        }
    }

    @Test
    void testIsIndependentOfFrameRate() {
        var other = new CompassNeedleTest();
        needle.update(nanos, 0, 2f, 0);
        other.needle.update(other.nanos, 0, 2f, 0);

        for (var i = 0; i < 12; i++) {
            assertEquals(run(0.25f, 60, 0, 2f, 0), other.run(0.25f, 144, 0, 2f, 0), DELTA);
        }
    }

    @Test
    void testHoldsWorldBearingWhileCaseTurns() {
        needle.update(nanos, 0, 1f, 0);
        run(10, 60, 0, 1f, 0);

        assertEquals(1f - 2f, run(10, 60, 2f, 1f, 0), DELTA);
    }

    @Test
    void testTurningCaseDragsNeedleAlong() {
        needle.update(nanos, 0, 0f, 0);
        run(1, 60, 0, 0f, 0);

        var relativeAngle = run(0.1f, 60, 1f, 0f, 0);

        // Without drag, the needle would remain at world angle 0, which is -1 relative to the case
        assertTrue(relativeAngle > -0.9f, "needle was not dragged: " + relativeAngle);
    }

    @Test
    void testSpinsAtRequestedSpeed() {
        needle.update(nanos, 0, null, 2f);
        run(10, 60, 0, null, 2f);

        var before = run(0.1f, 60, 0, null, 2f);
        var after = run(0.1f, 60, 0, null, 2f);

        assertEquals(0.2f, after - before, DELTA);
    }

    @Test
    void testStandsStillWhileClockIsStopped() {
        needle.update(nanos, 0, 1f, 0);
        var angle = run(0.2f, 60, 0, 1f, 0);

        for (var i = 0; i < 60; i++) {
            assertEquals(angle, needle.update(nanos, 0, 1f, 0));
        }

        // It continues with its previous velocity
        assertTrue(run(1 / 60f, 60, 0, 1f, 0) > angle);
    }

    @Test
    void testToleratesClockGoingBackwards() {
        needle.update(nanos, 0, 1f, 0);
        var angle = run(0.2f, 60, 0, 1f, 0);

        nanos -= SECOND / 10;

        assertEquals(angle, needle.update(nanos, 0, 1f, 0));
    }

    @Test
    void testRestartsFromRestAfterUpdateGap() {
        needle.update(nanos, 0, 1f, 0);
        var angle = run(0.2f, 60, 0, 1f, 0);

        nanos += 10 * SECOND;

        assertEquals(angle, needle.update(nanos, 3f, 1f, 0) + 3f, DELTA);
    }

    /**
     * Rendered relative to the owner, the bearing has to match the angle between the look vector and the direction.
     */
    @ParameterizedTest
    @CsvSource({
            "1, 0, 0",
            "0, 1, 0",
            "-3, 2, 40",
            "2, -7, 135",
            "-1, -1, -100"
    })
    void testBearingMatchesAngleToLookVector(double dx, double dz, float yawDegrees) {
        var yaw = (float) Math.toRadians(yawDegrees);
        var expected = new Vector2f((float) dx, (float) dz).angle(new Vector2f((float) -Math.sin(yaw),
                (float) Math.cos(yaw)));

        var actual = CompassNeedle.getBearing(dx, dz) + yaw;

        assertEquals(0, Math.sin((actual - expected) / 2), 0.001);
    }
}
