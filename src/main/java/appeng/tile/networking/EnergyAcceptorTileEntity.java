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

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;

import appeng.api.config.Actionable;
import appeng.api.networking.GridAccessException;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.util.AECableType;
import appeng.tile.grid.AENetworkPowerTileEntity;
import appeng.util.inv.InvOperation;

public class EnergyAcceptorTileEntity extends AENetworkPowerTileEntity {

    public EnergyAcceptorTileEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
        this.getMainNode().setIdlePowerUsage(0.0);
        this.setInternalMaxPower(0);
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.COVERED;
    }

    @Override
    protected double getFunnelPowerDemand(final double maxRequired) {
        try {
            final IEnergyService grid = this.getMainNode().getEnergy();

            return grid.getEnergyDemand(maxRequired);
        } catch (final GridAccessException e) {
            return this.getInternalMaxPower();
        }
    }

    @Override
    protected double funnelPowerIntoStorage(final double power, final Actionable mode) {
        try {
            final IEnergyService grid = this.getMainNode().getEnergy();
            final double leftOver = grid.injectPower(power, mode);

            return leftOver;
        } catch (final GridAccessException e) {
            return super.funnelPowerIntoStorage(power, mode);
        }
    }

    @Override
    public IItemHandler getInternalInventory() {
        return EmptyHandler.INSTANCE;
    }

    @Override
    public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc,
            final ItemStack removed, final ItemStack added) {

    }
}
