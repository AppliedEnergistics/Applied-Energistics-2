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

package appeng.helpers;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import appeng.util.Platform;

public class InvalidPatternHelper {

    private final List<PatternIngredient> outputs = new ArrayList<>();
    private final List<PatternIngredient> inputs = new ArrayList<>();
    private final boolean isCrafting;
    private final boolean canSubstitute;

    public InvalidPatternHelper(final ItemStack is) {
        final CompoundNBT encodedValue = is.getTag();

        if (encodedValue == null) {
            throw new IllegalArgumentException("No pattern here!");
        }

        final ListNBT inTag = encodedValue.getList("in", 10);
        final ListNBT outTag = encodedValue.getList("out", 10);
        this.isCrafting = encodedValue.getBoolean("crafting");

        this.canSubstitute = this.isCrafting && encodedValue.getBoolean("substitute");

        for (int i = 0; i < outTag.size(); i++) {
            this.outputs.add(new PatternIngredient(outTag.getCompound(i)));
        }

        for (int i = 0; i < inTag.size(); i++) {
            CompoundNBT in = inTag.getCompound(i);

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

    public class PatternIngredient {
        private String id;
        private int count;
        private int damage;

        private ItemStack stack;

        public PatternIngredient(CompoundNBT tag) {
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

        public ITextComponent getName() {
            return this.isValid() ? Platform.getItemDisplayName(this.stack)
                    : new StringTextComponent(this.id + '@' + this.getDamage());
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

        public ITextComponent getFormattedToolTip() {
            IFormattableTextComponent result = new StringTextComponent(this.getCount() + " ").append(this.getName());

            if (!this.isValid()) {
                result.withStyle(TextFormatting.RED);
            }

            return result;
        }
    }
}
