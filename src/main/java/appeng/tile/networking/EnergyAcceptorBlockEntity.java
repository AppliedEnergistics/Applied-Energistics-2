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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;

import appeng.api.config.Actionable;
import appeng.api.util.AECableType;
import appeng.tile.grid.AENetworkPowerBlockEntity;
import appeng.util.inv.InvOperation;

public class EnergyAcceptorBlockEntity extends AENetworkPowerBlockEntity {

    public EnergyAcceptorBlockEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState blockState) {
        super(tileEntityTypeIn, pos, blockState);
        this.getMainNode().setIdlePowerUsage(0.0);
        this.setInternalMaxPower(0);
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.COVERED;
    }

    @Override
    protected double getFunnelPowerDemand(final double maxRequired) {
        var grid = getMainNode().getGrid();
        if (grid != null) {
            return grid.getEnergyService().getEnergyDemand(maxRequired);
        } else {
            return this.getInternalMaxPower();
        }
    }

    @Override
    protected double funnelPowerIntoStorage(final double power, final Actionable mode) {
        var grid = getMainNode().getGrid();
        if (grid != null) {
            return grid.getEnergyService().injectPower(power, mode);
        } else {
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
