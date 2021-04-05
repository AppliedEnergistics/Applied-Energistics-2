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

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;

import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.tile.powersink.AEBasePoweredTileEntity;

public abstract class AENetworkPowerTileEntity extends AEBasePoweredTileEntity implements IActionHost, IGridProxyable {

    private final AENetworkProxy gridProxy = new AENetworkProxy(this, "proxy", this.getItemFromTile(this), true);

    public AENetworkPowerTileEntity(TileEntityType<?> tileEntityTypeIn) {
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

    @Override
    public AENetworkProxy getProxy() {
        return this.gridProxy;
    }

    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this);
    }

    @Override
    public void gridChanged() {

    }

    @Override
    public IGridNode getGridNode(final AEPartLocation dir) {
        return this.getProxy().getNode();
    }

    @Override
    public AECableType getCableConnectionType(final AEPartLocation dir) {
        return AECableType.SMART;
    }

    @Override
    public void validate() {
        super.validate();
        this.getProxy().validate();
    }

    @Override
    public void remove() {
        super.remove();
        this.getProxy().remove();
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
    public IGridNode getActionableNode() {
        return this.getProxy().getNode();
    }

}
