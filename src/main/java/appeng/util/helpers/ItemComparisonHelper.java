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

import javax.annotation.Nonnull;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import appeng.api.config.FuzzyMode;

/**
 * A helper class for comparing {@link Item}, {@link ItemStack} or NBT
 */
public class ItemComparisonHelper {

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
    public boolean isEqualItemType(@Nonnull final ItemStack that, @Nonnull final ItemStack other) {
        return !that.isEmpty() && !other.isEmpty() && that.getItem() == other.getItem();
    }

    /**
     * Compares two {@link ItemStack} and their NBT tag for equality.
     * <p>
     * Use this when a precise check is required and the same item is required. Not just something with different NBT
     * tags.
     *
     * @return true, if both are identical.
     */
    public boolean isSameItem(@Nonnull final ItemStack is, @Nonnull final ItemStack filter) {
        return ItemStack.areItemsEqual(is, filter) && this.isNbtTagEqual(is.getTag(), filter.getTag());
    }

    /**
     * Similar to {@link ItemComparisonHelper#isEqualItem(ItemStack, ItemStack)}, but it can further check, if both
     * match the same {@link FuzzyMode} or are considered equal by the {@link OreDictionary}
     *
     * @param mode how to compare the two {@link ItemStack}s
     * @return true, if both are matching the mode or considered equal by the {@link OreDictionary}
     */
    public boolean isFuzzyEqualItem(final ItemStack a, final ItemStack b, final FuzzyMode mode) {
        if (a.isEmpty() && b.isEmpty()) {
            return true;
        }

        if (a.isEmpty() || b.isEmpty()) {
            return false;
        }

        // test damageable items..
        if (a.getItem() == b.getItem() && a.getItem().isDamageable()) {
            if (mode == FuzzyMode.IGNORE_ALL) {
                return true;
            } else if (mode == FuzzyMode.PERCENT_99) {
                return (a.getDamage() > 1) == (b.getDamage() > 1);
            } else {
                final float percentDamagedOfA = (float) a.getDamage() / a.getMaxDamage();
                final float percentDamagedOfB = (float) b.getDamage() / b.getMaxDamage();

                return (percentDamagedOfA > mode.breakPoint) == (percentDamagedOfB > mode.breakPoint);
            }
        }
// FIXME
//		final OreReference aOR = OreHelper.INSTANCE.getOre( a ).orElse( null );
//		final OreReference bOR = OreHelper.INSTANCE.getOre( b ).orElse( null );
//
//		if( OreHelper.INSTANCE.sameOre( aOR, bOR ) )
//		{
//			return true;
//		}

        return a.isItemEqual(b);
    }

    /**
     * recursive test for NBT Equality, this was faster then trying to compare / generate hashes, its also more reliable
     * then the vanilla version which likes to fail when NBT Compound data changes order, it is pretty expensive
     * performance wise, so try an use shared tag compounds as long as the system remains in AE.
     */
    public boolean isNbtTagEqual(final CompoundTag left, final CompoundTag right) {
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

        if (left != null) {
            return left.equals(right);
        }

        return false;
    }
}
