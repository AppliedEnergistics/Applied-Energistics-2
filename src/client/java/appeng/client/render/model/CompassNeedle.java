/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2026, TeamAppliedEnergistics, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.client.render.model;

import org.jetbrains.annotations.Nullable;

import net.minecraft.util.Mth;

/**
 * Simulates a compass needle as a damped spring, so that it has inertia and overshoots its target.
 * <p>
 * The needle is simulated in world space, which means it holds its bearing while the compass case turns underneath it.
 * Damping is relative to the case, so turning the case drags the needle along and lets it swing back afterwards.
 */
final class CompassNeedle {
    /**
     * Fixed simulation step in seconds, which makes the behavior independent of the frame rate.
     */
    private static final float STEP = 1 / 120f;

    /**
     * The most simulation time in seconds that a single update will advance by.
     */
    private static final float MAX_FRAME_TIME = 0.1f;

    /**
     * If the needle hasn't been updated for this many seconds, it wasn't visible, and it restarts from rest.
     */
    private static final float MAX_UPDATE_GAP = 1f;

    /**
     * The spring's stiffness. This is the square of its angular frequency of 6 rad/s, or about one swing per second.
     */
    private static final float STIFFNESS = 36f;

    /**
     * Corresponds to a damping ratio of 0.3, which results in about 37% overshoot.
     */
    private static final float DAMPING = 3.6f;

    /**
     * How quickly the needle approaches the desired speed while spinning, in 1/s.
     */
    private static final float SPIN_RESPONSE = 3f;

    private float angle;
    private float velocity;
    private float lastCaseAngle;
    private float accumulator;
    private long lastNanos;
    private boolean started;

    /**
     * Advances the simulation up to the given time.
     *
     * @param nanos     The current time in nanoseconds. The simulation stands still as long as this doesn't change.
     * @param caseAngle The world space angle of the compass case in radians.
     * @param bearing   The world space angle in radians to point towards, or null to spin instead.
     * @param spinSpeed The speed in radians per second to spin at if there's no bearing.
     * @return The angle of the needle relative to the compass case in radians.
     */
    float update(long nanos, float caseAngle, @Nullable Float bearing, float spinSpeed) {
        var frameTime = (nanos - lastNanos) / 1e9f;
        if (!started || frameTime > MAX_UPDATE_GAP) {
            velocity = 0;
            accumulator = 0;
            started = true;
        } else {
            // This is the integral of the case's speed in the damping term, applied as an impulse,
            // since the case angle doesn't necessarily change on every frame
            velocity += DAMPING * wrap(caseAngle - lastCaseAngle);
            // The clock can go backwards a bit, since the client's game time gets corrected by the server
            accumulator += Mth.clamp(frameTime, 0, MAX_FRAME_TIME);
        }
        lastNanos = nanos;
        lastCaseAngle = caseAngle;

        for (; accumulator >= STEP; accumulator -= STEP) {
            var acceleration = bearing != null
                    ? -STIFFNESS * wrap(angle - bearing) - DAMPING * velocity
                    : SPIN_RESPONSE * (spinSpeed - velocity);
            velocity += acceleration * STEP;
            angle = wrap(angle + velocity * STEP);
        }
        return angle - caseAngle;
    }

    /**
     * Gets the world space angle of a direction on the horizontal plane.
     */
    static float getBearing(double dx, double dz) {
        return Mth.PI / 2 - (float) Math.atan2(dz, dx);
    }

    private static float wrap(float radians) {
        return Mth.positiveModulo(radians + Mth.PI, Mth.TWO_PI) - Mth.PI;
    }
}
