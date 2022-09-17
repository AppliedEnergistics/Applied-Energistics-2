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


import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.util.AEPartLocation;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.tile.AEBaseInvTile;
import net.minecraft.nbt.NBTTagCompound;


public abstract class AENetworkInvTile extends AEBaseInvTile implements IActionHost, IGridProxyable {

    private final AENetworkProxy gridProxy = new AENetworkProxy(this, "proxy", this.getItemFromTile(this), true);

    @Override
    public void readFromNBT(final NBTTagCompound data) {
        super.readFromNBT(data);
        this.getProxy().readFromNBT(data);
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound data) {
        super.writeToNBT(data);
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
    public void onChunkUnload() {
        super.onChunkUnload();
        this.getProxy().onChunkUnload();
    }

    @Override
    public void onReady() {
        super.onReady();
        this.getProxy().onReady();
    }

    @Override
    public void invalidate() {
        super.invalidate();
        this.getProxy().invalidate();
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
