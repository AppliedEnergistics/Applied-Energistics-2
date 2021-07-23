/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.debug;

import java.util.EnumSet;

import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.core.Api;
import appeng.me.helpers.TileEntityNodeListener;
import appeng.tile.grid.AENetworkTileEntity;

public class PhantomNodeTileEntity extends AENetworkTileEntity {

    private IManagedGridNode proxy = null;
    private boolean crashMode = false;

    public PhantomNodeTileEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    @Override
    public IGridNode getGridNode(final Direction dir) {
        if (!this.crashMode) {
            return super.getGridNode(dir);
        }

        return this.proxy.getNode();
    }

    @Override
    public void onReady() {
        super.onReady();
        this.proxy = Api.instance().grid().createManagedNode(this, TileEntityNodeListener.INSTANCE)
                .setInWorldNode(true)
                .setVisualRepresentation(getItemFromTile());
        this.proxy.create(world, pos);
        this.crashMode = true;
    }

    void triggerCrashMode() {
        if (this.proxy != null) {
            this.crashMode = true;
            this.proxy.setExposedOnSides(EnumSet.allOf(Direction.class));
        }
    }
}
