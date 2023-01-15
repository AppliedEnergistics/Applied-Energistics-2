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
import java.util.Optional;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import appeng.api.client.AEKeyRendering;
import appeng.api.config.FuzzyMode;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.StorageCells;
import appeng.api.storage.cells.ICellWorkbenchItem;
import appeng.core.definitions.AEItems;
import appeng.core.localization.GuiText;
import appeng.core.localization.Tooltips;
import appeng.items.AEBaseItem;
import appeng.items.contents.CellConfig;
import appeng.me.cells.CreativeCellHandler;
import appeng.util.ConfigInventory;

public class CreativeCellItem extends AEBaseItem implements ICellWorkbenchItem {
    public CreativeCellItem(Properties props) {
        super(props);
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
            if (!cc.isEmpty()) {
                if (Screen.hasShiftDown()) {
                    for (var key : cc.keySet()) {
                        lines.add(Tooltips.of(AEKeyRendering.getDisplayName(key)));
                    }
                } else {
                    lines.add(Tooltips.of(GuiText.PressShiftForFullList));
                }
            }
        }
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        return CreativeCellHandler.INSTANCE.getTooltipImage(stack);
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
