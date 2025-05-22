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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.blockentity.grid.AENetworkBlockEntity;
import appeng.me.helpers.BlockEntityNodeListener;

public class PhantomNodeBlockEntity extends AENetworkBlockEntity {

    private IManagedGridNode proxy = null;
    private boolean crashMode = false;

    public PhantomNodeBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    @Override
    public IGridNode getGridNode(Direction dir) {
        if (!this.crashMode) {
            return super.getGridNode(dir);
        }

        return this.proxy.getNode();
    }

    @Override
    public void onReady() {
        super.onReady();
        this.proxy = GridHelper.createManagedNode(this, BlockEntityNodeListener.INSTANCE)
                .setInWorldNode(true)
                .setVisualRepresentation(getItemFromBlockEntity());
        this.proxy.create(level, worldPosition);
        this.crashMode = true;
    }

    void triggerCrashMode() {
        if (this.proxy != null) {
            this.crashMode = true;
            this.proxy.setExposedOnSides(EnumSet.allOf(Direction.class));
        }
    }
}
