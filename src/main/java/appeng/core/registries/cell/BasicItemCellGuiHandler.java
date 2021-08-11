/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.cells.ICellGuiHandler;
import appeng.api.storage.cells.ICellHandler;
import appeng.api.storage.data.IAEStack;
import appeng.menu.MenuLocator;
import appeng.menu.MenuOpener;
import appeng.menu.me.items.ItemTerminalMenu;

public class BasicItemCellGuiHandler implements ICellGuiHandler {
    @Override
    public <T extends IAEStack<T>> boolean isHandlerFor(final IStorageChannel<T> channel) {
        return channel == StorageChannels.items();
    }

    @Override
    public <T extends IAEStack<T>> void openChestGui(final Player player, final IChestOrDrive chest,
            final ICellHandler cellHandler,
            final IMEInventoryHandler<T> inv, final ItemStack is, final IStorageChannel<T> chan) {
        MenuOpener.open(ItemTerminalMenu.TYPE, player,
                MenuLocator.forBlockEntitySide((BlockEntity) chest, chest.getUp()));
    }
}
