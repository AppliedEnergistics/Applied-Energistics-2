/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2017, tyra314, All rights reserved.
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

package appeng.crafting.pattern;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;

import appeng.util.Platform;

class InvalidPatternHelper {

    private final List<PatternIngredient> outputs = new ArrayList<>();
    private final List<PatternIngredient> inputs = new ArrayList<>();
    private final boolean isCrafting;
    private final boolean canSubstitute;

    public InvalidPatternHelper(final ItemStack is) {
        var encodedValue = is.getTag();

        if (encodedValue == null) {
            throw new IllegalArgumentException("No pattern here!");
        }

        final ListTag inTag = encodedValue.getList("in", Tag.TAG_COMPOUND);
        final ListTag outTag = encodedValue.getList("out", Tag.TAG_COMPOUND);
        this.isCrafting = encodedValue.getBoolean("crafting");

        this.canSubstitute = this.isCrafting && encodedValue.getBoolean("substitute");

        for (int i = 0; i < outTag.size(); i++) {
            this.outputs.add(new PatternIngredient(outTag.getCompound(i)));
        }

        for (int i = 0; i < inTag.size(); i++) {
            CompoundTag in = inTag.getCompound(i);

            // skip empty slots in the crafting grid
            if (in.isEmpty()) {
                continue;
            }

            this.inputs.add(new PatternIngredient(in));
        }
    }

    public List<PatternIngredient> getOutputs() {
        return this.outputs;
    }

    public List<PatternIngredient> getInputs() {
        return this.inputs;
    }

    public boolean isCraftable() {
        return this.isCrafting;
    }

    public boolean canSubstitute() {
        return this.canSubstitute;
    }

    public static class PatternIngredient {
        private String id;
        private int count;
        private int damage;

        private ItemStack stack;

        public PatternIngredient(CompoundTag tag) {
            this.stack = ItemStack.of(tag);

            if (this.stack.isEmpty()) {
                this.id = tag.getString("id");
                this.count = tag.getByte("Count");
                this.damage = Math.max(0, tag.getShort("Damage"));
            }
        }

        public boolean isValid() {
            return !this.stack.isEmpty();
        }

        public Component getName() {
            return this.isValid() ? Platform.getItemDisplayName(this.stack)
                    : new TextComponent(this.id + '@' + this.getDamage());
        }

        public int getDamage() {
            return this.isValid() ? this.stack.getDamageValue() : this.damage;
        }

        public int getCount() {
            return this.isValid() ? this.stack.getCount() : this.count;
        }

        public ItemStack getItem() {
            if (!this.isValid()) {
                throw new IllegalArgumentException("There is no valid ItemStack for this PatternIngredient");
            }

            return this.stack;
        }

        public Component getFormattedToolTip() {
            MutableComponent result = new TextComponent(this.getCount() + " ")
                    .append(this.getName());

            if (!this.isValid()) {
                result.withStyle(ChatFormatting.RED);
            }

            return result;
        }
    }
}
