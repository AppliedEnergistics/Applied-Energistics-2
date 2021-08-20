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

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

final class AESharedItemStack {

    private final ItemStack itemStack;
    private final ItemVariant variant;
    private final int itemId;
    private final int itemDamage;
    private final int hashCode;

    public AESharedItemStack(final ItemStack itemStack) {
        this(itemStack, itemStack.getDamageValue());
    }

    /**
     * A constructor to explicitly set the damage value and not fetch it from the {@link ItemStack}
     *
     * @param itemStack The {@link ItemStack} to filter
     * @param damage    The damage of the item
     */
    private AESharedItemStack(ItemStack itemStack, int damage) {
        this.itemStack = itemStack;
        ItemVariant variant = null;
        try {
            variant = ItemVariant.of(itemStack);
        } catch (ClassCastException e) {
            // FIXME TEST HACKS
            // Running from tests: mixins don't apply, so the cast of Item to ItemVariantCache fails.
        }
        this.variant = variant;
        this.itemId = Item.getId(itemStack.getItem());
        this.itemDamage = damage;

        // Ensure this is always called last.
        this.hashCode = this.makeHashCode();
    }

    ItemStack getDefinition() {
        return this.itemStack;
    }

    int getItemDamage() {
        return this.itemDamage;
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AESharedItemStack other)) {
            return false;
        }

        Preconditions.checkState(this.itemStack.getCount() == 1, "ItemStack#getCount() has to be 1");
        Preconditions.checkArgument(other.getDefinition().getCount() == 1, "ItemStack#getCount() has to be 1");

        if (this.itemStack == other.itemStack) {
            return true;
        }
        return ItemStack.matches(this.itemStack, other.itemStack);
    }

    private int makeHashCode() {
        return Objects.hash(
                this.itemId,
                this.itemDamage,
                this.itemStack.hasTag() ? this.itemStack.getTag() : 0);
    }

    public ItemVariant getVariant() {
        return variant;
    }
}
