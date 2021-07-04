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

import appeng.api.networking.IInWorldGridNodeHost;
import appeng.block.IOwnerAwareTile;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;

import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalBlockPos;
import appeng.me.helpers.ManagedGridNode;
import appeng.tile.AEBaseTileEntity;
import net.minecraft.util.Direction;

import javax.annotation.Nullable;

public class AENetworkTileEntity extends AEBaseTileEntity implements IActionHost, IInWorldGridNodeHost, IOwnerAwareTile {

    private final ManagedGridNode gridProxy = new ManagedGridNode(this, "proxy")
            .setVisualRepresentation(getItemFromTile());

    public AENetworkTileEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    @Override
    public void read(BlockState blockState, final CompoundNBT data) {
        super.read(blockState, data);
        this.getProxy().readFromNBT(data);
    }

    @Override
    public CompoundNBT write(final CompoundNBT data) {
        super.write(data);
        this.getProxy().writeToNBT(data);
        return data;
    }

    protected final ManagedGridNode getProxy() {
        return this.gridProxy;
    }

    @Nullable
    public IGridNode getGridNode() {
        return getProxy().getNode();
    }

    @Override
    public IGridNode getGridNode(final Direction dir) {
        var node = this.getProxy().getNode();

        // Check if the proxy exposes the node on this side
        if (node != null && node.isExposedOnSide(dir)) {
            return node;
        }

        return null;
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.SMART;
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        this.getProxy().onChunkUnloaded();
    }

    @Override
    public void onReady() {
        super.onReady();
        this.getProxy().onReady();
    }

    @Override
    public void remove() {
        super.remove();
        this.getProxy().remove();
    }

    @Override
    public void validate() {
        super.validate();
        this.getProxy().validate();
    }

    @Override
    public DimensionalBlockPos getLocation() {
        return new DimensionalBlockPos(this);
    }

    @Override
    public IGridNode getActionableNode() {
        return this.getProxy().getNode();
    }

    @Override
    public void setOwner(PlayerEntity owner) {
        getProxy().setOwner(owner);
    }
}
