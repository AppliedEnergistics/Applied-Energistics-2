/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

package appeng.me.cluster;

import java.util.Iterator;

import net.minecraft.util.math.BlockPos;

import appeng.api.networking.IGridHost;

public interface IAECluster {

    /**
     * The minimum x,y,z position still within the bounds of the cluster.
     */
    BlockPos getBoundsMin();

    /**
     * The maximum x,y,z position still within the bounds of the cluster.
     */
    BlockPos getBoundsMax();

    void updateStatus(boolean updateGrid);

    void destroy();

    /**
     * @return True if the cluster has been destroyed, but not yet removed from a
     *         tile entity. Usually true during destruction.
     */
    boolean isDestroyed();

    Iterator<? extends IGridHost> getTiles();
}
