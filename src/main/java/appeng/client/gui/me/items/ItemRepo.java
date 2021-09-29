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

package appeng.client.gui.me.items;

import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import net.minecraft.network.chat.Component;

import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.me.common.Repo;
import appeng.client.gui.widgets.IScrollSource;
import appeng.client.gui.widgets.ISortSource;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;

public class ItemRepo extends Repo<IAEItemStack> {

    public ItemRepo(IScrollSource src, ISortSource sortSrc) {
        super(src, sortSrc);
        setSynchronizeWithJEI(true);
    }

    @Override
    protected boolean matchesSearch(SearchMode searchMode, Pattern searchPattern, IAEItemStack stack) {
        if (searchMode == SearchMode.MOD) {
            String modId = Platform.getModId(stack);
            return searchPattern.matcher(modId).find();
        }

        AEItemStack aeStack = (AEItemStack) stack;

        String displayName = aeStack.getDisplayName().getString();
        if (searchPattern.matcher(displayName).find()) {
            return true;
        }

        if (searchMode == SearchMode.NAME_OR_TOOLTIP) {
            List<Component> tooltip = aeStack.getToolTip();

            for (Component line : tooltip) {
                if (searchPattern.matcher(line.getString()).find()) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    protected Comparator<? super IAEItemStack> getComparator(SortOrder sortBy, SortDir sortDir) {
        return ItemSorters.getComparator(sortBy, sortDir);
    }

}
