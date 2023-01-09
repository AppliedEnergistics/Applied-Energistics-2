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


import appeng.util.Platform;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

import static appeng.helpers.ItemStackHelper.stackFromNBT;


public class InvalidPatternHelper {

    private final List<PatternIngredient> outputs = new ArrayList<>();
    private final List<PatternIngredient> inputs = new ArrayList<>();
    private final boolean isCrafting;
    private final boolean canSubstitute;

    public InvalidPatternHelper(final ItemStack is) {
        final NBTTagCompound encodedValue = is.getTagCompound();

        if (encodedValue == null) {
            throw new IllegalArgumentException("No pattern here!");
        }

        final NBTTagList inTag = encodedValue.getTagList("in", 10);
        final NBTTagList outTag = encodedValue.getTagList("out", 10);
        this.isCrafting = encodedValue.getBoolean("crafting");

        this.canSubstitute = this.isCrafting && encodedValue.getBoolean("substitute");

        for (int i = 0; i < outTag.tagCount(); i++) {
            this.outputs.add(new PatternIngredient(outTag.getCompoundTagAt(i)));
        }

        for (int i = 0; i < inTag.tagCount(); i++) {
            NBTTagCompound in = inTag.getCompoundTagAt(i);

            // skip empty slots in the crafting grid
            if (in.hasNoTags()) {
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

        private final ItemStack stack;

        public PatternIngredient(NBTTagCompound tag) {
            this.stack = stackFromNBT(tag);

            if (this.stack.isEmpty()) {
                this.id = tag.getString("id");
                this.count = tag.getByte("Count");
                if (tag.hasKey("stackSize")) {
                    this.count = tag.getInteger("stackSize");
                }
                this.damage = Math.max(0, tag.getShort("Damage"));
            }
        }

        public boolean isValid() {
            return !this.stack.isEmpty();
        }

        public String getName() {
            return this.isValid() ? Platform.getItemDisplayName(this.stack) : this.id + '@' + this.getDamage();
        }

        public int getDamage() {
            return this.isValid() ? this.stack.getItemDamage() : this.damage;
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

        public String getFormattedToolTip() {
            String result = String.valueOf(this.getCount()) + ' ' + this.getName();

            if (!this.isValid()) {
                result = TextFormatting.RED + (' ' + result);
            }

            return result;
        }
    }
}
