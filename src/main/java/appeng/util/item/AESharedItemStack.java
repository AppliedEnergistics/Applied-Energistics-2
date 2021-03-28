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

import appeng.api.config.FuzzyMode;
import com.google.common.base.Preconditions;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

import java.util.Objects;

final class AESharedItemStack implements Comparable<AESharedItemStack> {

    /**
     * Marker Item Stack to facilitate bound comparisons for stacks that
     * are otherwise randomly ordered.
     */
    private static final ItemStack LOWER_BOUND_STACK = new ItemStack(null);
    private static final ItemStack UPPER_BOUND_STACK = new ItemStack(null);

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
        Preconditions.checkState(!isBound(), "Bounds may not be used for anything other than compareTo");
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
        Preconditions.checkState(!isBound(), "Bounds may not be used for anything other than compareTo");
        return this.hashCode;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AESharedItemStack)) {
            return false;
        }
        final AESharedItemStack other = (AESharedItemStack) obj;
        Preconditions.checkArgument(!other.isBound(), "Bounds may not be used for anything other than compareTo");
        Preconditions.checkArgument(!isBound(), "Bounds may not be used for anything other than compareTo");
        Preconditions.checkState(this.itemStack.getCount() == 1, "ItemStack#getCount() has to be 1");
        Preconditions.checkArgument(other.getDefinition().getCount() == 1, "ItemStack#getCount() has to be 1");

        if (this.itemStack == other.itemStack) {
            return true;
        }
        return ItemStack.areItemStacksEqual(this.itemStack, other.itemStack);
    }

    @Override
    public int compareTo(final AESharedItemStack b) {
        if (!isBound() && !b.isBound()) {
            Preconditions.checkState(this.itemStack.getCount() == 1, "ItemStack#getCount() has to be 1");
            Preconditions.checkArgument(b.getDefinition().getCount() == 1, "ItemStack#getCount() has to be 1");

            if (this.itemStack == b.getDefinition()) {
                return 0;
            }

            final int id = this.itemId - b.itemId;
            if (id != 0) {
                return id;
            }
        }

        // Damaged items are sorted before undamaged items
        final int damageValue = b.itemDamage - this.itemDamage;
        if (damageValue != 0) {
            return damageValue;
        }

        // As a final tie breaker, order by the object identity of the item stack
        // While this will order seemingly at random, we only need the order of
        // damage values to be predictable, while still having to satisfy the
        // complete order requirements of the sorted map
        return Long.compare(getItemStackOrder(this.itemStack), getItemStackOrder(b.itemStack));
    }

    private boolean isBound() {
        return itemStack == LOWER_BOUND_STACK || itemStack == UPPER_BOUND_STACK;
    }

    private static long getItemStackOrder(ItemStack stack) {
        // the identity hash code is 32-bit, so to ensure no collisions with the
        // upper/lower bounds, we up-cast to 64-bit
        if (stack == LOWER_BOUND_STACK) {
            return Integer.MIN_VALUE - 1L;
        } else if (stack == UPPER_BOUND_STACK) {
            return Integer.MAX_VALUE + 1L;
        } else {
            return System.identityHashCode(stack);
        }
    }

    private int makeHashCode() {
        return Objects.hash(
                this.itemId,
                this.itemDamage,
                this.itemStack.hasTag() ? this.itemStack.getTag() : 0
        );
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

        private final AESharedItemStack lower;
        private final AESharedItemStack upper;

        public Bounds(final ItemStack stack, final FuzzyMode fuzzy) {
            Preconditions.checkState(!stack.isEmpty(), "ItemStack#isEmpty() has to be false");
            Preconditions.checkState(stack.getCount() == 1, "ItemStack#getCount() has to be 1");
            Preconditions.checkState(stack.isDamageable(), "ItemStack#isDamageable() has to be true");

            this.lower = this.makeLowerBound(stack, fuzzy);
            this.upper = this.makeUpperBound(stack, fuzzy);

            Preconditions.checkState(this.lower.compareTo(this.upper) < 0);
        }

        public AESharedItemStack lower() {
            return this.lower;
        }

        public AESharedItemStack upper() {
            return this.upper;
        }

        /*
         * Keep in mind that the stack order is from most damaged to least damaged, so this lower bound
         * will actually be a higher number than the upper bound.
         */
        private AESharedItemStack makeLowerBound(final ItemStack itemStack, final FuzzyMode fuzzy) {
            int damage;
            if (fuzzy == FuzzyMode.IGNORE_ALL) {
                damage = itemStack.getMaxDamage();
            } else {
                final int breakpoint = fuzzy.calculateBreakPoint(itemStack.getMaxDamage());
                damage = itemStack.getDamage() <= breakpoint ? breakpoint : itemStack.getMaxDamage();
            }

            return new AESharedItemStack(LOWER_BOUND_STACK, damage);
        }

        /*
         * Keep in mind that the stack order is from most damaged to least damaged, so this upper bound
         * will actually be a lower number than the lower bound. It also is exclusive.
         */
        private AESharedItemStack makeUpperBound(final ItemStack itemStack, final FuzzyMode fuzzy) {
            int damage;
            if (fuzzy == FuzzyMode.IGNORE_ALL) {
                damage = MIN_DAMAGE_VALUE;
            } else {
                final int breakpoint = fuzzy.calculateBreakPoint(itemStack.getMaxDamage());
                damage = itemStack.getDamage() <= breakpoint ? MIN_DAMAGE_VALUE : breakpoint;
            }

            return new AESharedItemStack(UPPER_BOUND_STACK, damage);
        }

    }

}
