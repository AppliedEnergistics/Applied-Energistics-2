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

package appeng.util.item;

import java.util.Objects;

import com.google.common.base.Preconditions;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

import appeng.api.config.FuzzyMode;

final class AESharedItemStack implements Comparable<AESharedItemStack> {

    private final ItemStack itemStack;
    private final int itemId;
    private final int itemDamage;
    private final int hashCode;

    public AESharedItemStack(final ItemStack itemStack) {
        this(itemStack, itemStack.getDamage());
    }

    /**
     * A constructor to explicitly set the damage value and not fetch it from the {@link ItemStack}
     * 
     * @param itemStack The {@link ItemStack} to filter
     * @param damage    The damage of the item
     */
    private AESharedItemStack(ItemStack itemStack, int damage) {
        this.itemStack = itemStack;
        this.itemId = Item.getIdFromItem(itemStack.getItem());
        this.itemDamage = damage;

        // Ensure this is always called last.
        this.hashCode = this.makeHashCode();
    }

    Bounds getBounds(final FuzzyMode fuzzy) {
        return new Bounds(this.itemStack, fuzzy);
    }

    ItemStack getDefinition() {
        return this.itemStack;
    }

    int getItemDamage() {
        return this.itemDamage;
    }

    int getItemID() {
        return this.itemId;
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof AESharedItemStack) {
            final AESharedItemStack other = (AESharedItemStack) obj;

            Preconditions.checkState(this.itemStack.getCount() == 1, "ItemStack#getCount() has to be 1");
            Preconditions.checkArgument(other.getDefinition().getCount() == 1, "ItemStack#getCount() has to be 1");

            if (this.itemStack == other.itemStack) {
                return true;
            }
            return ItemStack.areItemStacksEqual(this.itemStack, other.itemStack);
        }
        return false;
    }

    @Override
    public int compareTo(final AESharedItemStack b) {
        Preconditions.checkState(this.itemStack.getCount() == 1, "ItemStack#getCount() has to be 1");
        Preconditions.checkArgument(b.getDefinition().getCount() == 1, "ItemStack#getCount() has to be 1");

        if (this.itemStack == b.getDefinition()) {
            return 0;
        }

        final int id = this.itemId - b.itemId;
        if (id != 0) {
            return id;
        }

        final int damageValue = this.itemDamage - b.itemDamage;
        if (damageValue != 0) {
            return damageValue;
        }

        return this.hashCode - b.hashCode;
    }

    private int makeHashCode() {
        return Objects.hash(this.itemId, this.itemDamage, this.itemStack.hasTag() ? this.itemStack.getTag() : 0);
    }

    /**
     * Creates the lower and upper bounds for a specific shared itemstack.
     */
    public static final class Bounds {
        /**
         * Minecraft reverses the damage values. So anything with a damage of 0 is undamaged and increases the more
         * damaged the item is.
         * 
         * Further the used subMap follows [MAX_DAMAGE, MIN_DAMAGE), so to include undamaged items, we have to start
         * with a lower damage value than 0, while it is fine to use {@link ItemStack#getMaxDamage()} for the upper
         * bound.
         */
        private static final int MIN_DAMAGE_VALUE = -1;
        private static final int UNDAMAGED_DAMAGE_VALUE = 0;

        private final AESharedItemStack lower;
        private final AESharedItemStack upper;

        public Bounds(final ItemStack stack, final FuzzyMode fuzzy) {
            Preconditions.checkState(!stack.isEmpty(), "ItemStack#isEmpty() has to be false");
            Preconditions.checkState(stack.getCount() == 1, "ItemStack#getCount() has to be 1");

            final CompoundNBT tag = stack.hasTag() ? stack.getTag().copy() : null;

            this.lower = this.makeLowerBound(stack, tag, fuzzy);
            this.upper = this.makeUpperBound(stack, tag, fuzzy);

            Preconditions.checkState(this.lower.compareTo(this.upper) < 0);
        }

        public AESharedItemStack lower() {
            return this.lower;
        }

        public AESharedItemStack upper() {
            return this.upper;
        }

        private AESharedItemStack makeLowerBound(final ItemStack itemStack, final CompoundNBT tag,
                final FuzzyMode fuzzy) {
            final ItemStack newDef = itemStack.copy();
            newDef.setTag(tag);
            int damage = newDef.getDamage();

            if (newDef.getItem().isDamageable()) {
                if (fuzzy == FuzzyMode.IGNORE_ALL) {
                    damage = MIN_DAMAGE_VALUE;
                } else if (fuzzy == FuzzyMode.PERCENT_99) {
                    if (itemStack.getDamage() == UNDAMAGED_DAMAGE_VALUE) {
                        damage = MIN_DAMAGE_VALUE;
                    } else {
                        damage = UNDAMAGED_DAMAGE_VALUE;
                    }
                } else {
                    final int breakpoint = fuzzy.calculateBreakPoint(itemStack.getMaxDamage());
                    damage = breakpoint <= itemStack.getDamage() ? breakpoint : -1;
                }
            }

            return new AESharedItemStack(newDef, damage);
        }

        private AESharedItemStack makeUpperBound(final ItemStack itemStack, final CompoundNBT tag,
                final FuzzyMode fuzzy) {
            final ItemStack newDef = itemStack.copy();
            newDef.setTag(tag);
            int damage = newDef.getDamage();

            if (newDef.getItem().isDamageable()) {
                if (fuzzy == FuzzyMode.IGNORE_ALL) {
                    damage = itemStack.getMaxDamage();
                } else if (fuzzy == FuzzyMode.PERCENT_99) {
                    if (itemStack.getDamage() == UNDAMAGED_DAMAGE_VALUE) {
                        damage = UNDAMAGED_DAMAGE_VALUE;
                    } else {
                        damage = itemStack.getMaxDamage();
                    }
                } else {
                    final int breakpoint = fuzzy.calculateBreakPoint(itemStack.getMaxDamage());
                    damage = itemStack.getDamage() < breakpoint ? breakpoint - 1 : itemStack.getMaxDamage();
                }
            }

            return new AESharedItemStack(newDef, damage);
        }

    }

}
