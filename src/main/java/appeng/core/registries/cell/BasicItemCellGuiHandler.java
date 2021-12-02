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
import appeng.api.storage.AEKeySpace;
import appeng.api.storage.cells.IBasicCellItem;
import appeng.api.storage.cells.ICellGuiHandler;
import appeng.api.storage.cells.ICellHandler;
import appeng.menu.MenuLocator;
import appeng.menu.MenuOpener;
import appeng.menu.me.common.MEMonitorableMenu;

public class BasicItemCellGuiHandler implements ICellGuiHandler {
    @Override
    public boolean isSpecializedFor(ItemStack cell) {
        return cell.getItem() instanceof IBasicCellItem basicCellItem
                && basicCellItem.getKeySpace() == AEKeySpace.items();
    }

    @Override
    public void openChestGui(Player player, IChestOrDrive chest, ICellHandler cellHandler, ItemStack cell) {
        MenuOpener.open(MEMonitorableMenu.TYPE, player,
                MenuLocator.forBlockEntitySide((BlockEntity) chest, chest.getUp()));
    }
}
