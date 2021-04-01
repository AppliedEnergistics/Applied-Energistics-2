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
import appeng.api.util.AEPartLocation;
import appeng.me.helpers.AENetworkProxy;
import appeng.tile.grid.AENetworkBlockEntity;

public class PhantomNodeBlockEntity extends AENetworkBlockEntity {

    private AENetworkProxy proxy = null;
    private boolean crashMode = false;

    public PhantomNodeBlockEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    @Override
    public IGridNode getGridNode(final AEPartLocation dir) {
        if (!this.crashMode) {
            return super.getGridNode(dir);
        }

        return this.proxy.getNode();
    }

    @Override
    public void onReady() {
        super.onReady();
        this.proxy = this.createProxy();
        this.proxy.onReady();
        this.crashMode = true;
    }

    void triggerCrashMode() {
        if (this.proxy != null) {
            this.crashMode = true;
            this.proxy.setValidSides(EnumSet.allOf(Direction.class));
        }
    }
}
