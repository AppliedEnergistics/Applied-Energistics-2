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

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.client.AEStackRendering;
import appeng.api.config.FuzzyMode;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.StorageCells;
import appeng.api.storage.cells.ICellWorkbenchItem;
import appeng.core.definitions.AEItems;
import appeng.items.AEBaseItem;
import appeng.items.contents.CellConfig;
import appeng.util.ConfigInventory;

public class CreativeCellItem extends AEBaseItem implements ICellWorkbenchItem {
    public CreativeCellItem(Properties props) {
        super(props);
    }

    @Override
    public boolean isEditable(ItemStack is) {
        return true;
    }

    @Override
    public ConfigInventory getConfigInventory(ItemStack is) {
        return CellConfig.create(is);
    }

    @Override
    public FuzzyMode getFuzzyMode(ItemStack is) {
        return FuzzyMode.IGNORE_ALL;
    }

    @Override
    public void setFuzzyMode(ItemStack is, FuzzyMode fzMode) {
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> lines, TooltipFlag advancedTooltips) {
        var inventory = StorageCells.getCellInventory(stack, null);

        if (inventory != null) {
            var cc = getConfigInventory(stack);
            for (var key : cc.keySet()) {
                lines.add(AEStackRendering.getDisplayName(key));
            }
        }
    }

    public static ItemStack ofItems(ItemLike... items) {
        var cell = AEItems.ITEM_CELL_CREATIVE.stack();
        var configInv = AEItems.ITEM_CELL_CREATIVE.asItem().getConfigInventory(cell);
        for (int i = 0; i < items.length; i++) {
            configInv.setStack(i, GenericStack.fromItemStack(new ItemStack(items[i])));
        }
        return cell;
    }

    public static ItemStack ofFluids(Fluid... fluids) {
        var cell = AEItems.FLUID_CELL_CREATIVE.stack();
        var configInv = AEItems.FLUID_CELL_CREATIVE.asItem().getConfigInventory(cell);
        for (int i = 0; i < fluids.length; i++) {
            configInv.setStack(i, new GenericStack(AEFluidKey.of(fluids[i]), 1));
        }
        return cell;
    }
}
