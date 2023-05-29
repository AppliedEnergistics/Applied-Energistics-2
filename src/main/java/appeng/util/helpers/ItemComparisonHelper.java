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

package appeng.util.helpers;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.FuzzyMode;

/**
 * A helper class for comparing {@link Item}, {@link ItemStack} or NBT
 */
public final class ItemComparisonHelper {

    private ItemComparisonHelper() {
    }

    /**
     * Compare the two {@link ItemStack}s based on the same {@link Item} and damage value.
     * <p>
     * In case of the item being damageable, only the {@link Item} will be considered. If not it will also compare both
     * damage values.
     * <p>
     * Ignores NBT.
     *
     * @return true, if both are equal.
     */
    public static boolean isEqualItemType(ItemStack that, ItemStack other) {
        return !that.isEmpty() && !other.isEmpty() && that.getItem() == other.getItem();
    }

    /**
     * recursive test for NBT Equality, this was faster then trying to compare / generate hashes, its also more reliable
     * then the vanilla version which likes to fail when NBT Compound data changes order, it is pretty expensive
     * performance wise, so try an use shared tag compounds as long as the system remains in AE.
     */
    public boolean isNbtTagEqual(@Nullable CompoundTag left, @Nullable CompoundTag right) {
        if (left == right) {
            return true;
        }

        final boolean isLeftEmpty = left == null || left.isEmpty();
        final boolean isRightEmpty = right == null || right.isEmpty();

        if (isLeftEmpty && isRightEmpty) {
            return true;
        }

        if (isLeftEmpty != isRightEmpty) {
            return false;
        }

        return left.equals(right);
    }

    /**
     * Similar to {@link ItemStack#isSameItemSameTags}, but it can further check, if both are equal considering a
     * {@link FuzzyMode}.
     *
     * @param mode how to compare the two {@link ItemStack}s
     * @return true, if both are matching the mode
     */
    public static boolean isFuzzyEqualItem(ItemStack a, ItemStack b, FuzzyMode mode) {
        if (a.isEmpty() && b.isEmpty()) {
            return true;
        }

        if (a.isEmpty() || b.isEmpty()) {
            return false;
        }

        // test damageable items..
        if (a.getItem() == b.getItem() && a.getItem().canBeDepleted()) {
            if (mode == FuzzyMode.IGNORE_ALL) {
                return true;
            } else if (mode == FuzzyMode.PERCENT_99) {
                return a.getDamageValue() > 0 == b.getDamageValue() > 0;
            } else {
                final float percentDamagedOfA = (float) a.getDamageValue() / a.getMaxDamage();
                final float percentDamagedOfB = (float) b.getDamageValue() / b.getMaxDamage();

                return percentDamagedOfA > mode.breakPoint == percentDamagedOfB > mode.breakPoint;
            }
        }

        return ItemStack.isSameItem(a, b);
    }

}
