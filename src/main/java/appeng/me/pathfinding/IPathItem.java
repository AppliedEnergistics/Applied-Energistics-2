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

public interface IPathItem {

    IPathItem getControllerRoute();

    void setControllerRoute(IPathItem fast, boolean zeroOut);

    /**
     * used to determine if the finder can continue.
     */
    boolean canSupportMoreChannels();

    /**
     * The maximum number of channels connections to this path item can carry.
     */
    int getMaxChannels();

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
     * channels are done, wrap it up.
     */
    void finalizeChannels();
}
