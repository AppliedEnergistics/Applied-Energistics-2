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

package appeng.core.features.registries.cell;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.cells.ICellGuiHandler;
import appeng.api.storage.cells.ICellHandler;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.container.me.items.ItemTerminalContainer;
import appeng.core.Api;

public class BasicItemCellGuiHandler implements ICellGuiHandler {
    @Override
    public <T extends IAEStack<T>> boolean isHandlerFor(final IStorageChannel<T> channel) {
        return channel == Api.instance().storage().getStorageChannel(IItemStorageChannel.class);
    }

    @Override
    public <T extends IAEStack<T>> void openChestGui(final PlayerEntity player, final IChestOrDrive chest,
            final ICellHandler cellHandler,
            final IMEInventoryHandler<T> inv, final ItemStack is, final IStorageChannel<T> chan) {
        ContainerOpener.openContainer(ItemTerminalContainer.TYPE, player,
                ContainerLocator.forTileEntitySide((TileEntity) chest, chest.getUp()));
    }
}
