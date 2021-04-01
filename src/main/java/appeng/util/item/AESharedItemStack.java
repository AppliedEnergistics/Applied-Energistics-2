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
        this.itemStack = itemStack;
        this.itemId = Item.getIdFromItem(itemStack.getItem());
        this.itemDamage = itemStack.getDamage();
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

        return System.identityHashCode(this.getDefinition().getTag())
                - System.identityHashCode(b.getDefinition().getTag());
    }

    private int makeHashCode() {
        return Objects.hash(this.itemId, this.itemDamage, this.itemStack.hasTag() ? this.itemStack.getTag() : 0);
    }

    /**
     * Creates the lower and upper bounds for a specific shared itemstack.
     */
    public static final class Bounds {
        /**
         * Bounds enforced by {@link ItemStack#isEmpty()}
         */
        private static final int MIN_DAMAGE_VALUE = 0;
        private static final int MAX_DAMAGE_VALUE = Short.MAX_VALUE;

        private final AESharedItemStack lower;
        private final AESharedItemStack upper;

        public Bounds(final ItemStack stack, final FuzzyMode fuzzy) {
            Preconditions.checkState(!stack.isEmpty(), "ItemStack#isEmpty() has to be false");
            Preconditions.checkState(stack.getCount() == 1, "ItemStack#getCount() has to be 1");

            final CompoundNBT tag = stack.hasTag() ? stack.getTag() : null;

            this.lower = this.makeLowerBound(stack, tag, fuzzy);
            this.upper = this.makeUpperBound(stack, tag, fuzzy);
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

            if (newDef.getItem().isDamageable()) {
                if (fuzzy == FuzzyMode.IGNORE_ALL) {
                    newDef.setDamage(MIN_DAMAGE_VALUE);
                } else if (fuzzy == FuzzyMode.PERCENT_99) {
                    if (itemStack.getDamage() == MIN_DAMAGE_VALUE) {
                        newDef.setDamage(MIN_DAMAGE_VALUE);
                    } else {
                        newDef.setDamage(MIN_DAMAGE_VALUE + 1);
                    }
                } else {
                    final int breakpoint = fuzzy.calculateBreakPoint(itemStack.getMaxDamage());
                    final int damage = breakpoint <= itemStack.getDamage() ? breakpoint : 0;
                    newDef.setDamage(damage);
                }
            }

            return new AESharedItemStack(newDef);
        }

        private AESharedItemStack makeUpperBound(final ItemStack itemStack, final CompoundNBT tag,
                final FuzzyMode fuzzy) {
            final ItemStack newDef = itemStack.copy();

            if (newDef.getItem().isDamageable()) {
                if (fuzzy == FuzzyMode.IGNORE_ALL) {
                    newDef.setDamage(itemStack.getMaxDamage() + 1);
                } else if (fuzzy == FuzzyMode.PERCENT_99) {
                    if (itemStack.getDamage() == MIN_DAMAGE_VALUE) {
                        newDef.setDamage(MIN_DAMAGE_VALUE);
                    } else {
                        newDef.setDamage(itemStack.getMaxDamage() + 1);
                    }
                } else {
                    final int breakpoint = fuzzy.calculateBreakPoint(itemStack.getMaxDamage());
                    final int damage = itemStack.getDamage() < breakpoint ? breakpoint - 1
                            : itemStack.getMaxDamage() + 1;
                    newDef.setDamage(damage);
                }
            }

            return new AESharedItemStack(newDef);
        }

    }

}
