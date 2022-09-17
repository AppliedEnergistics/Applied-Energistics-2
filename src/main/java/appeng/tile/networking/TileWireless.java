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

package appeng.tile.networking;


import appeng.api.AEApi;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.implementations.tiles.IWirelessAccessPoint;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.core.AEConfig;
import appeng.me.GridAccessException;
import appeng.tile.grid.AENetworkInvTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.Platform;
import appeng.util.inv.InvOperation;
import appeng.util.inv.filter.AEItemDefinitionFilter;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.IItemHandler;

import java.io.IOException;
import java.util.EnumSet;


public class TileWireless extends AENetworkInvTile implements IWirelessAccessPoint, IPowerChannelState {

    public static final int POWERED_FLAG = 1;
    public static final int CHANNEL_FLAG = 2;

    private final AppEngInternalInventory inv = new AppEngInternalInventory(this, 1);

    private int clientFlags = 0;

    public TileWireless() {
        this.inv.setFilter(new AEItemDefinitionFilter(AEApi.instance().definitions().materials().wirelessBooster()));
        this.getProxy().setFlags(GridFlags.REQUIRE_CHANNEL);
        this.getProxy().setValidSides(EnumSet.noneOf(EnumFacing.class));
    }

    @Override
    public void setOrientation(final EnumFacing inForward, final EnumFacing inUp) {
        super.setOrientation(inForward, inUp);
        this.getProxy().setValidSides(EnumSet.of(this.getForward().getOpposite()));
    }

    @MENetworkEventSubscribe
    public void chanRender(final MENetworkChannelsChanged c) {
        this.markForUpdate();
    }

    @MENetworkEventSubscribe
    public void powerRender(final MENetworkPowerStatusChange c) {
        this.markForUpdate();
    }

    @Override
    protected boolean readFromStream(final ByteBuf data) throws IOException {
        final boolean c = super.readFromStream(data);
        final int old = this.getClientFlags();
        this.setClientFlags(data.readByte());

        return old != this.getClientFlags() || c;
    }

    @Override
    protected void writeToStream(final ByteBuf data) throws IOException {
        super.writeToStream(data);
        this.setClientFlags(0);

        try {
            if (this.getProxy().getEnergy().isNetworkPowered()) {
                this.setClientFlags(this.getClientFlags() | POWERED_FLAG);
            }

            if (this.getProxy().getNode().meetsChannelRequirements()) {
                this.setClientFlags(this.getClientFlags() | CHANNEL_FLAG);
            }
        } catch (final GridAccessException e) {
            // meh
        }

        data.writeByte((byte) this.getClientFlags());
    }

    @Override
    public AECableType getCableConnectionType(final AEPartLocation dir) {
        return AECableType.SMART;
    }

    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this);
    }

    @Override
    public IItemHandler getInternalInventory() {
        return this.inv;
    }

    @Override
    public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc, final ItemStack removed, final ItemStack added) {
        // :P
    }

    @Override
    public void onReady() {
        this.updatePower();
        super.onReady();
    }

    private void updatePower() {
        this.getProxy().setIdlePowerUsage(AEConfig.instance().wireless_getPowerDrain(this.getBoosters()));
    }

    private int getBoosters() {
        final ItemStack boosters = this.inv.getStackInSlot(0);
        return boosters == null ? 0 : boosters.getCount();
    }

    @Override
    public void saveChanges() {
        this.updatePower();
        super.saveChanges();
    }

    @Override
    public double getRange() {
        return AEConfig.instance().wireless_getMaxRange(this.getBoosters());
    }

    @Override
    public boolean isActive() {
        if (Platform.isClient()) {
            return this.isPowered() && (CHANNEL_FLAG == (this.getClientFlags() & CHANNEL_FLAG));
        }

        return this.getProxy().isActive();
    }

    @Override
    public IGrid getGrid() {
        try {
            return this.getProxy().getGrid();
        } catch (final GridAccessException e) {
            return null;
        }
    }

    @Override
    public boolean isPowered() {
        return POWERED_FLAG == (this.getClientFlags() & POWERED_FLAG);
    }

    public int getClientFlags() {
        return this.clientFlags;
    }

    private void setClientFlags(final int clientFlags) {
        this.clientFlags = clientFlags;
    }
}
