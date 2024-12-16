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

package appeng.me.pathfinding;

import appeng.api.networking.GridFlags;
import org.jetbrains.annotations.Nullable;

public interface IPathItem {

    @Nullable
    IPathItem getControllerRoute();

    /**
     * Sets route to controller.
     * TODO: why do we even bother in ad hoc case?
     * @param fast {@code null} when performing ad hoc update
     */
    void setControllerRoute(@Nullable IPathItem fast);

    /**
     * used to determine if the finder can continue.
     */
    boolean canSupportMoreChannels();

    /**
     * The maximum number of channels connections to this path item can carry.
     */
    int getMaxChannels();

    // TODO: cleanup, it's kinda shit
    int getUsedChannelCount();

    /**
     * find possible choices for other pathing.
     */
    Iterable<IPathItem> getPossibleOptions();

    /**
     * add one to the channel count, this is mostly for cables.
     */
    void incrementChannelCount(int usedChannels);

    /**
     * Tests if this path item has the specific grid flag set.
     */
    boolean hasFlag(GridFlags flag);

    /**
     * Final propagation pass.
     */
    void aggregateChildChannels();

    /**
     * channels are done, wrap it up.
     */
    void finalizeChannels();
}
