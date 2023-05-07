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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import appeng.api.stacks.GenericStack;
import appeng.api.storage.cells.ICellHandler;
import appeng.api.storage.cells.ISaveProvider;
import appeng.api.storage.cells.StorageCell;
import appeng.core.AEConfig;
import appeng.items.contents.CellConfig;
import appeng.items.storage.CreativeCellItem;
import appeng.items.storage.StorageCellTooltipComponent;

/**
 * Cell handler for creative storage cells (both fluid and item), which do not allow item insertion.
 */
public class CreativeCellHandler implements ICellHandler {
    public static final CreativeCellHandler INSTANCE = new CreativeCellHandler();

    @Override
    public boolean isCell(ItemStack is) {
        return !is.isEmpty() && is.getItem() instanceof CreativeCellItem;
    }

    @Override
    public StorageCell getCellInventory(ItemStack is, ISaveProvider container) {
        if (!is.isEmpty() && is.getItem() instanceof CreativeCellItem) {
            return new CreativeCellInventory(is);
        }
        return null;
    }

    public Optional<TooltipComponent> getTooltipImage(ItemStack is) {
        var handler = getCellInventory(is, null);
        if (handler == null) { // Check that this is a creative cell
            return Optional.empty();
        }

        var cc = CellConfig.create(is);

        boolean hasMoreContent;
        List<GenericStack> content;
        if (AEConfig.instance().isTooltipShowCellContent()) {
            content = new ArrayList<>();

            var maxCountShown = AEConfig.instance().getTooltipMaxCellContentShown();

            for (var key : cc.keySet()) {
                content.add(new GenericStack(key, 1));
            }

            hasMoreContent = content.size() > maxCountShown;
            if (content.size() > maxCountShown) {
                content.subList(maxCountShown, content.size()).clear();
            }
        } else {
            hasMoreContent = false;
            content = Collections.emptyList();
        }

        return Optional.of(new StorageCellTooltipComponent(
                List.of(),
                content,
                hasMoreContent,
                false));
    }
}
