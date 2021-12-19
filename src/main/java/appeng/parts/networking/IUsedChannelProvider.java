/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.parts.networking;

import appeng.api.networking.IGridNode;
import appeng.api.parts.IPart;
import appeng.me.GridNode;

/**
 * Extended part that provides info about channel capacity and usage to probes like HWYLA and TheOneProbe.
 */
public interface IUsedChannelProvider extends IPart {

    /**
     * @return The number of channels carried on this cable. Purely for informational purposes.
     */
    default int getUsedChannelsInfo() {
        int howMany = 0;
        IGridNode node = this.getGridNode();
        if (node != null && node.isActive()) {
            for (var gc : node.getConnections()) {
                howMany = Math.max(gc.getUsedChannels(), howMany);
            }
        }
        return howMany;
    }

    /**
     * @return The number of channels that can be carried at most. Purely for informational purposes.
     */
    default int getMaxChannelsInfo() {
        var node = this.getGridNode();
        if (node instanceof GridNode gridNode) {
            return gridNode.getMaxChannels();
        }
        return 0;
    }

}
