/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

import java.util.Objects;

import com.google.common.base.Preconditions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import appeng.api.storage.AEKeySpace;
import appeng.api.storage.GenericStack;

/**
 * Helper functions to work with patterns, mostly related to (de)serialization.
 */
class ProcessingPatternEncoding {
    private static final String NBT_INPUTS = "in";
    private static final String NBT_OUTPUTS = "out";

    public static GenericStack[] getProcessingInputs(CompoundTag nbt) {
        return getMixedList(nbt, NBT_INPUTS, 9);
    }

    public static GenericStack[] getProcessingOutputs(CompoundTag nbt) {
        return getMixedList(nbt, NBT_OUTPUTS, 3);
    }

    public static GenericStack[] getMixedList(CompoundTag nbt, String nbtKey, int maxSize) {
        Objects.requireNonNull(nbt, "Pattern must have a tag.");

        ListTag tag = nbt.getList(nbtKey, Tag.TAG_COMPOUND);
        Preconditions.checkArgument(tag.size() <= maxSize, "Cannot use more than " + maxSize + " ingredients");

        var result = new GenericStack[tag.size()];
        for (int x = 0; x < tag.size(); ++x) {
            var entry = tag.getCompound(x);
            if (entry.isEmpty()) {
                continue;
            }
            var stack = GenericStack.readTag(entry);
            if (stack == null) {
                throw new IllegalArgumentException("Pattern references missing stack: " + entry);
            }
            if (stack.what().getChannel() != AEKeySpace.items()
                    && stack.what().getChannel() != AEKeySpace.fluids()) {
                throw new IllegalArgumentException("Only items and fluids are supported in AE2 patterns.");
            }
            result[x] = stack;
        }
        return result;
    }

    public static void encodeProcessingPattern(CompoundTag tag, GenericStack[] sparseInputs,
            GenericStack[] sparseOutputs) {
        tag.put(NBT_INPUTS, encodeStackList(sparseInputs));
        tag.put(NBT_OUTPUTS, encodeStackList(sparseOutputs));
    }

    private static ListTag encodeStackList(GenericStack[] stacks) {
        ListTag tag = new ListTag();
        boolean foundStack = false;
        for (var stack : stacks) {
            tag.add(GenericStack.writeTag(stack));
            if (stack != null && stack.amount() > 0) {
                foundStack = true;
            }
        }
        Preconditions.checkArgument(foundStack, "List passed to pattern must contain at least one stack.");
        return tag;
    }

}
