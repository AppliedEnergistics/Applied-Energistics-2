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

import java.io.IOException;
import java.util.EnumSet;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.items.IItemHandler;

import appeng.api.implementations.IPowerChannelState;
import appeng.api.implementations.tiles.IWirelessAccessPoint;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNodeListener;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalBlockPos;
import appeng.core.AEConfig;
import appeng.core.definitions.AEItems;
import appeng.tile.grid.AENetworkInvTileEntity;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.inv.InvOperation;
import appeng.util.inv.filter.AEItemDefinitionFilter;

public class WirelessTileEntity extends AENetworkInvTileEntity implements IWirelessAccessPoint, IPowerChannelState {

    public static final int POWERED_FLAG = 1;
    public static final int CHANNEL_FLAG = 2;

    private final AppEngInternalInventory inv = new AppEngInternalInventory(this, 1);

    private int clientFlags = 0;

    public WirelessTileEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
        this.inv.setFilter(new AEItemDefinitionFilter(AEItems.WIRELESS_BOOSTER));
        this.getMainNode().setFlags(GridFlags.REQUIRE_CHANNEL);
        this.getMainNode().setExposedOnSides(EnumSet.noneOf(Direction.class));
    }

    @Override
    public void setOrientation(final Direction inForward, final Direction inUp) {
        super.setOrientation(inForward, inUp);
        this.getMainNode().setExposedOnSides(EnumSet.of(this.getForward().getOpposite()));
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        this.markForUpdate();
    }

    @Override
    protected boolean readFromStream(final PacketBuffer data) throws IOException {
        final boolean c = super.readFromStream(data);
        final int old = this.getClientFlags();
        this.setClientFlags(data.readByte());

        return old != this.getClientFlags() || c;
    }

    @Override
    protected void writeToStream(final PacketBuffer data) throws IOException {
        super.writeToStream(data);
        this.setClientFlags(0);

        getMainNode().ifPresent((grid, node) -> {
            if (grid.getEnergyService().isNetworkPowered()) {
                this.setClientFlags(this.getClientFlags() | POWERED_FLAG);
            }

            if (node.meetsChannelRequirements()) {
                this.setClientFlags(this.getClientFlags() | CHANNEL_FLAG);
            }
        });

        data.writeByte((byte) this.getClientFlags());
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.SMART;
    }

    @Override
    public DimensionalBlockPos getLocation() {
        return new DimensionalBlockPos(this);
    }

    @Override
    public IItemHandler getInternalInventory() {
        return this.inv;
    }

    @Override
    public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc,
            final ItemStack removed, final ItemStack added) {
        // :P
    }

    @Override
    public void onReady() {
        this.getMainNode().setExposedOnSides(EnumSet.of(this.getForward().getOpposite()));
        this.updatePower();
        super.onReady();
    }

    private void updatePower() {
        this.getMainNode().setIdlePowerUsage(AEConfig.instance().wireless_getPowerDrain(this.getBoosters()));
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
        if (isRemote()) {
            return this.isPowered() && CHANNEL_FLAG == (this.getClientFlags() & CHANNEL_FLAG);
        }

        return this.getMainNode().isActive();
    }

    @Override
    public IGrid getGrid() {
        return getMainNode().getGrid();
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
