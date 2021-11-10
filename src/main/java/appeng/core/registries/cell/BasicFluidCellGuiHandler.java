/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.core.registries.cell;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.implementations.blockentities.IChestOrDrive;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.cells.ICellGuiHandler;
import appeng.api.storage.cells.ICellHandler;
import appeng.api.storage.data.AEKey;
import appeng.menu.MenuLocator;
import appeng.menu.MenuOpener;
import appeng.menu.me.fluids.FluidTerminalMenu;

public class BasicFluidCellGuiHandler implements ICellGuiHandler {
    @Override
    public <T extends AEKey> boolean isHandlerFor(final IStorageChannel<T> channel) {
        return channel == StorageChannels.fluids();
    }

    @Override
    public void openChestGui(Player player, IChestOrDrive chest, ICellHandler cellHandler, ItemStack cell) {
        MenuOpener.open(FluidTerminalMenu.TYPE, player,
                MenuLocator.forBlockEntitySide((BlockEntity) chest, chest.getUp()));
    }
}
