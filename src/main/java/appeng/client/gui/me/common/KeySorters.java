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

package appeng.client.gui.me.common;

import java.util.Comparator;

import net.minecraft.world.item.ItemStack;

import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.stacks.AEKey;

final class KeySorters {

    private KeySorters() {
    }

    // FIXME: Calling .getString() to compare two untranslated strings is a problem, we need to investigate how to do
    // this better
    public static final Comparator<AEKey> NAME_ASC = Comparator.comparing(
            key -> {
                final ItemStack stack = key.wrapForDisplayOrFilter();
                return stack.getDisplayName().getString()
                        + (stack.getTag() != null ? stack.getTag().getAsString() : null);
            },
            String::compareToIgnoreCase);

    public static final Comparator<AEKey> NAME_DESC = NAME_ASC.reversed();

    public static final Comparator<AEKey> MOD_ASC = Comparator.comparing(
            AEKey::getModId,
            String::compareToIgnoreCase).thenComparing(NAME_ASC);

    public static final Comparator<AEKey> MOD_DESC = MOD_ASC.reversed();

    public static Comparator<AEKey> getComparator(SortOrder order, SortDir dir) {
        return switch (order) {
            case NAME -> dir == SortDir.ASCENDING ? NAME_ASC : NAME_DESC;
            case MOD -> dir == SortDir.ASCENDING ? MOD_ASC : MOD_DESC;
            case AMOUNT -> throw new UnsupportedOperationException();
        };
    }

}
