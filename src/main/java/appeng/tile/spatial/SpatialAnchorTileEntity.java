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

package appeng.tile.spatial;

import net.minecraft.tileentity.TileEntityType;

import appeng.api.networking.GridFlags;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.statistics.MENetworkChunkEvent.MENetworkChunkAdded;
import appeng.api.networking.events.statistics.MENetworkChunkEvent.MENetworkChunkRemoved;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.tile.grid.AENetworkTileEntity;

public class SpatialAnchorTileEntity extends AENetworkTileEntity {

    public SpatialAnchorTileEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
        this.getProxy().setFlags(GridFlags.REQUIRE_CHANNEL);
    }

    @Override
    public AECableType getCableConnectionType(final AEPartLocation dir) {
        return AECableType.SMART;
    }

    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this);
    }

    @MENetworkEventSubscribe
    public void chunkAdded(final MENetworkChunkAdded changed) {
    }

    @MENetworkEventSubscribe
    public void chunkRemoved(final MENetworkChunkRemoved changed) {
    }
}
