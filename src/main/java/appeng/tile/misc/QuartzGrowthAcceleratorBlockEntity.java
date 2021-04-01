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

package appeng.tile.misc;

import java.io.IOException;
import java.util.EnumSet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.implementations.tiles.ICrystalGrowthAccelerator;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.me.GridAccessException;
import appeng.tile.grid.AENetworkBlockEntity;
import appeng.util.Platform;

public class QuartzGrowthAcceleratorBlockEntity extends AENetworkBlockEntity
        implements IPowerChannelState, ICrystalGrowthAccelerator {

    private boolean hasPower = false;

    public QuartzGrowthAcceleratorBlockEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
        this.getProxy().setValidSides(EnumSet.noneOf(Direction.class));
        this.getProxy().setFlags();
        this.getProxy().setIdlePowerUsage(8);
    }

    @MENetworkEventSubscribe
    public void onPower(final MENetworkPowerStatusChange ch) {
        this.markForUpdate();
    }

    @Override
    public AECableType getCableConnectionType(final AEPartLocation dir) {
        return AECableType.COVERED;
    }

    @Override
    public boolean readFromStream(final PacketBuffer data) throws IOException {
        final boolean c = super.readFromStream(data);
        final boolean hadPower = this.isPowered();
        this.setPowered(data.readBoolean());
        return this.isPowered() != hadPower || c;
    }

    @Override
    public void writeToStream(final PacketBuffer data) throws IOException {
        super.writeToStream(data);
        try {
            data.writeBoolean(this.getProxy().getEnergy().isNetworkPowered());
        } catch (final GridAccessException e) {
            data.writeBoolean(false);
        }
    }

    @Override
    public void setOrientation(final Direction inForward, final Direction inUp) {
        super.setOrientation(inForward, inUp);
        this.getProxy().setValidSides(EnumSet.of(this.getUp(), this.getUp().getOpposite()));
    }

    @Override
    public boolean isPowered() {
        if (Platform.isServer()) {
            try {
                return this.getProxy().getEnergy().isNetworkPowered();
            } catch (final GridAccessException e) {
                return false;
            }
        }

        return this.hasPower;
    }

    @Override
    public boolean isActive() {
        return this.isPowered();
    }

    private void setPowered(final boolean hasPower) {
        this.hasPower = hasPower;
    }
}
