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

package appeng.tile.grid;

import javax.annotation.Nullable;

import appeng.core.Api;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.networking.IManagedGridNode;
import appeng.hooks.ticking.TickHandler;
import appeng.me.helpers.IGridConnectedTileEntity;
import appeng.me.helpers.TileEntityNodeListener;
import appeng.tile.AEBaseInvTileEntity;

public abstract class AENetworkInvTileEntity extends AEBaseInvTileEntity
        implements IInWorldGridNodeHost, IGridConnectedTileEntity {

    private final IManagedGridNode mainNode = createMainNode()
            .setVisualRepresentation(this.getItemFromTile())
            .setInWorldNode(true)
            .setTagName("proxy");

    public AENetworkInvTileEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    protected IManagedGridNode createMainNode() {
        return Api.instance().grid().createManagedNode(this, TileEntityNodeListener.INSTANCE);
    }

    @Override
    public void read(BlockState blockState, final CompoundNBT data) {
        super.read(blockState, data);
        this.getMainNode().loadFromNBT(data);
    }

    @Override
    public CompoundNBT write(final CompoundNBT data) {
        super.write(data);
        this.getMainNode().saveToNBT(data);
        return data;
    }

    public final IManagedGridNode getMainNode() {
        return this.mainNode;
    }

    @Nullable
    public IGridNode getGridNode() {
        return getMainNode().getNode();
    }

    @Override
    public IGridNode getGridNode(final Direction dir) {
        var node = this.getMainNode().getNode();

        // Check if the proxy exposes the node on this side
        if (node != null && node.isExposedOnSide(dir)) {
            return node;
        }

        return null;
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        this.getMainNode().destroy();
    }

    @Override
    public void onReady() {
        super.onReady();
        this.getMainNode().create(world, pos);
    }

    @Override
    public void remove() {
        super.remove();
        // if the cached block state is not current, the following method may call
        // markForUpdate
        // and cause the block state to be changed back to non-air
        updateContainingBlockInfo();
        this.getMainNode().destroy();
    }

    @Override
    public void validate() {
        super.validate();
        TickHandler.instance().addInit(this); // Required for onReady to be called
    }

}
