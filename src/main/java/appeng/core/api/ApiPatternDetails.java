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

package appeng.core.api;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.Nullable;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.Level;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.IPatternDetailsDecoder;
import appeng.api.crafting.IPatternDetailsHelper;
import appeng.api.storage.data.IAEStack;
import appeng.core.definitions.AEItems;
import appeng.crafting.pattern.AEPatternDecoder;

public class ApiPatternDetails implements IPatternDetailsHelper {
    private final List<IPatternDetailsDecoder> decoders = new CopyOnWriteArrayList<>();

    public ApiPatternDetails() {
        // Register support for our own stacks.
        registerDecoder(AEPatternDecoder.INSTANCE);
    }

    @Override
    public void registerDecoder(IPatternDetailsDecoder decoder) {
        Objects.requireNonNull(decoder);
        decoders.add(decoder);
    }

    @Override
    public boolean isEncodedPattern(ItemStack stack) {
        for (var decoder : decoders) {
            if (decoder.isEncodedPattern(stack)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public IPatternDetails decodePattern(ItemStack stack, Level level, boolean autoRecovery) {
        for (var decoder : decoders) {
            var decoded = decoder.decodePattern(stack, level, autoRecovery);
            if (decoded != null) {
                return decoded;
            }
        }
        return null;
    }

    @Override
    public ItemStack encodeCraftingPattern(CraftingRecipe recipe, ItemStack[] in,
            ItemStack out, boolean allowSubstitutes) {
        return AEItems.CRAFTING_PATTERN.asItem().encode(recipe, in, out, allowSubstitutes);
    }

    @Override
    public ItemStack encodeProcessingPattern(IAEStack[] in, IAEStack[] out) {
        return AEItems.PROCESSING_PATTERN.asItem().encode(in, out);
    }

}
