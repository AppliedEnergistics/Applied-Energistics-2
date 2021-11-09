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

package appeng.me.cells;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.IncludeExclude;
import appeng.api.storage.cells.ICellHandler;
import appeng.api.storage.cells.ISaveProvider;
import appeng.core.localization.GuiText;

/**
 * Cell handler that manages all normal storage cells (items, fluids).
 */
public class BasicCellHandler implements ICellHandler {
    public static final BasicCellHandler INSTANCE = new BasicCellHandler();

    @Override
    public boolean isCell(ItemStack is) {
        return BasicCellInventory.isCell(is);
    }

    @Override
    public BasicCellInventory<?> getCellInventory(ItemStack is, ISaveProvider container) {
        return BasicCellInventory.createInventory(is, container);
    }

    public void addCellInformationToTooltip(ItemStack is, List<Component> lines) {
        var handler = getCellInventory(is, null);
        if (handler == null) {
            return;
        }

        lines.add(new TextComponent(handler.getUsedBytes() + " ").append(GuiText.Of.text())
                .append(" " + handler.getTotalBytes() + " ").append(GuiText.BytesUsed.text()));

        lines.add(new TextComponent(handler.getStoredItemTypes() + " ").append(GuiText.Of.text())
                .append(" " + handler.getTotalItemTypes() + " ").append(GuiText.Types.text()));

        if (handler.isPreformatted()) {
            var list = (handler.getPartitionListMode() == IncludeExclude.WHITELIST ? GuiText.Included
                    : GuiText.Excluded)
                            .text();

            if (handler.isFuzzy()) {
                lines.add(GuiText.Partitioned.withSuffix(" - ").append(list).append(" ").append(GuiText.Fuzzy.text()));
            } else {
                lines.add(
                        GuiText.Partitioned.withSuffix(" - ").append(list).append(" ").append(GuiText.Precise.text()));
            }
        }
    }

}
