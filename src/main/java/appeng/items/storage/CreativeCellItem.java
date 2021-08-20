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

package appeng.items.storage;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import appeng.api.config.FuzzyMode;
import appeng.api.inventories.InternalInventory;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.StorageCells;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.cells.ICellInventoryHandler;
import appeng.api.storage.cells.ICellWorkbenchItem;
import appeng.api.storage.data.IAEStack;
import appeng.helpers.FluidCellConfig;
import appeng.items.AEBaseItem;
import appeng.items.contents.CellConfig;
import appeng.me.cells.CreativeCellHandler;

public class CreativeCellItem extends AEBaseItem implements ICellWorkbenchItem {

    private final IStorageChannel<?> storageChannel;

    public CreativeCellItem(Properties props, IStorageChannel<?> storageChannel) {
        super(props);
        this.storageChannel = storageChannel;
    }

    @Override
    public boolean isEditable(final ItemStack is) {
        return true;
    }

    @Override
    public InternalInventory getConfigInventory(final ItemStack is) {
        if (this.storageChannel == StorageChannels.fluids()) {
            return new FluidCellConfig(is);
        } else {
            return new CellConfig(is);
        }
    }

    @Override
    public FuzzyMode getFuzzyMode(final ItemStack is) {
        return FuzzyMode.IGNORE_ALL;
    }

    @Override
    public void setFuzzyMode(final ItemStack is, final FuzzyMode fzMode) {
    }

    @Nonnull
    @Override
    public IStorageChannel<?> getChannel() {
        return this.storageChannel;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> lines, TooltipFlag advancedTooltips) {
        var inventory = StorageCells.getCellInventory(stack, null, storageChannel);

        if (inventory != null) {
            var cc = getConfigInventory(stack);

            for (var is : cc) {
                if (!is.isEmpty()) {
                    lines.add(is.getHoverName());
                }
            }
        }
    }

    @Nullable
    public <T extends IAEStack> ICellInventoryHandler<T> getCellInventory(IStorageChannel<T> channel,
            ItemStack stack) {
        Preconditions.checkArgument(stack.getItem() == this);
        if (this.storageChannel == channel) {
            return CreativeCellHandler.getCell(channel, stack);
        }
        return null;
    }
}
