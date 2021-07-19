/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 AlgorithmX2
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package appeng.api.networking.events;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * An event that is posted whenever a spatial IO is active.
 */
public class GridSpatialEvent extends GridEvent {
    /**
     * The world in which the Spatial I/O tile entity triggering this transition is located.
     */
    public final World spatialIoWorld;
    /**
     * The block position at which the Spatial I/O tile entity triggering this transition is located.
     */
    public final BlockPos spatialIoPos;
    /**
     * The energy in AE units needed to perform this transition.
     */
    public final double spatialEnergyUsage;
    private boolean preventTransition;

    /**
     * @param SpatialIO   ( INSTANCE of the SpatialIO block )
     * @param EnergyUsage ( the amount of energy that the SpatialIO uses)
     */
    public GridSpatialEvent(World spatialIoWorld,
            BlockPos spatialIoPos,
            double EnergyUsage) {
        this.spatialIoWorld = spatialIoWorld;
        this.spatialIoPos = spatialIoPos;
        this.spatialEnergyUsage = EnergyUsage;
    }

    /**
     * Prevent the Spatial IO transition from happening.
     */
    public void preventTransition() {
        this.preventTransition = true;
    }

    /**
     * @return True if the transition into the spatial IO should not be allowed.
     */
    public boolean isTransitionPrevented() {
        return preventTransition;
    }

}
