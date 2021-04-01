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
import appeng.api.util.AEPartLocation;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.tile.AEBaseInvBlockEntity;

public abstract class AENetworkInvBlockEntity extends AEBaseInvBlockEntity implements IActionHost, IGridProxyable {

    private final AENetworkProxy gridProxy = new AENetworkProxy(this, "proxy", this.getItemFromTile(this), true);

    public AENetworkInvBlockEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    @Override
    public void read(BlockState state, final CompoundNBT data) {
        super.read(state, data);
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
    public void gridChanged() {

    }

    @Override
    public IGridNode getGridNode(final AEPartLocation dir) {
        return this.getProxy().getNode();
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
        // if the cached block state is not current, the following method may call
        // markForUpdate
        // and cause the block state to be changed back to non-air
        updateContainingBlockInfo();
        this.getProxy().remove();
    }

    @Override
    public void validate() {
        super.validate();
        this.getProxy().validate();
    }

    @Override
    public IGridNode getActionableNode() {
        return this.getProxy().getNode();
    }
}
