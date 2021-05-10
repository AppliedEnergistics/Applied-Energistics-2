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

package appeng.client.gui.me.items;

import java.util.Comparator;

import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.util.Platform;

final class ItemSorters {

    private ItemSorters() {
    }

    // FIXME: Calling .getString() to compare two untranslated strings is a problem, we need to investigate how to do
    // this better
    public static final Comparator<IAEItemStack> NAME_ASC = Comparator.comparing(
            is -> Platform.getItemDisplayName(is).getString(),
            String::compareToIgnoreCase);

    public static final Comparator<IAEItemStack> NAME_DESC = NAME_ASC.reversed();

    public static final Comparator<IAEItemStack> MOD_ASC = Comparator.comparing(
            (IAEItemStack fs) -> Platform.getModId(fs),
            String::compareToIgnoreCase).thenComparing(NAME_ASC);

    public static final Comparator<IAEItemStack> MOD_DESC = MOD_ASC.reversed();

    public static final Comparator<IAEItemStack> SIZE_ASC = Comparator.comparingLong(IAEStack::getStackSize);

    public static final Comparator<IAEItemStack> SIZE_DESC = SIZE_ASC.reversed();

    public static Comparator<IAEItemStack> getComparator(SortOrder order, SortDir dir) {
        switch (order) {
            default:
            case NAME:
                return dir == SortDir.ASCENDING ? NAME_ASC : NAME_DESC;
            case AMOUNT:
                return dir == SortDir.ASCENDING ? SIZE_ASC : SIZE_DESC;
            case MOD:
                return dir == SortDir.ASCENDING ? MOD_ASC : MOD_DESC;
        }
    }

}
