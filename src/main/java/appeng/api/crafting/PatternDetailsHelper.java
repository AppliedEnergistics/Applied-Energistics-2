/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 TeamAppliedEnergistics
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package appeng.api.crafting;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.item.crafting.UpgradeRecipe;
import net.minecraft.world.level.Level;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.core.definitions.AEItems;
import appeng.crafting.pattern.AEPatternDecoder;

public final class PatternDetailsHelper {
    private static final List<IPatternDetailsDecoder> DECODERS = new CopyOnWriteArrayList<>();

    static {
        // Register support for our own stacks.
        registerDecoder(AEPatternDecoder.INSTANCE);
    }

    public static void registerDecoder(IPatternDetailsDecoder decoder) {
        Objects.requireNonNull(decoder);
        DECODERS.add(decoder);
    }

    public static boolean isEncodedPattern(ItemStack stack) {
        for (var decoder : DECODERS) {
            if (decoder.isEncodedPattern(stack)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public static IPatternDetails decodePattern(ItemStack stack, Level level) {
        return decodePattern(stack, level, false);
    }

    @Nullable
    public static IPatternDetails decodePattern(AEItemKey what, Level level) {
        for (var decoder : DECODERS) {
            var decoded = decoder.decodePattern(what, level);
            if (decoded != null) {
                return decoded;
            }
        }
        return null;
    }

    @Nullable
    public static IPatternDetails decodePattern(ItemStack stack, Level level, boolean autoRecovery) {
        for (var decoder : DECODERS) {
            var decoded = decoder.decodePattern(stack, level, autoRecovery);
            if (decoded != null) {
                return decoded;
            }
        }
        return null;
    }

    /**
     * Encodes a processing pattern which represents the ability to convert the given inputs into the given outputs
     * using some process external to the ME system.
     *
     * @param out The first element is considered the primary output and must be present
     * @return A new encoded pattern.
     * @throws IllegalArgumentException If either in or out contain only empty ItemStacks, or no primary output
     */
    public static ItemStack encodeProcessingPattern(GenericStack[] in, GenericStack[] out) {
        return AEItems.PROCESSING_PATTERN.asItem().encode(in, out);
    }

    /**
     * Encodes a crafting pattern which represents a Vanilla crafting recipe.
     *
     * @param recipe                The Vanilla crafting recipe to be encoded.
     * @param in                    The items in the crafting grid, which are used to determine what items are supplied
     *                              from the ME system to craft using this pattern.
     * @param out                   What is to be expected as the result of this crafting operation by the ME system.
     * @param allowSubstitutes      Controls whether the ME system will allow the use of equivalent items to craft this
     *                              recipe.
     * @param allowFluidSubstitutes Controls whether the ME system will allow the use of equivalent fluids.
     * @throws IllegalArgumentException If either in or out contain only empty ItemStacks.
     */
    public static ItemStack encodeCraftingPattern(CraftingRecipe recipe, ItemStack[] in,
            ItemStack out, boolean allowSubstitutes, boolean allowFluidSubstitutes) {
        return AEItems.CRAFTING_PATTERN.asItem().encode(recipe, in, out, allowSubstitutes, allowFluidSubstitutes);
    }

    /**
     * Encodes a stonecutting pattern which represents a Vanilla Stonecutter recipe.
     *
     * @param recipe           The Vanilla stonecutter recipe to be encoded.
     * @param in               The input item for the stonecutter, which is used to determine which item is supplied
     *                         from the ME system to craft using this pattern.
     * @param out              The selected output item from the stonecutter recipe. Used to restore the recipe if it is
     *                         renamed later.
     * @param allowSubstitutes Controls whether the ME system will allow the use of equivalent items to craft this
     *                         recipe.
     */
    public static ItemStack encodeStonecuttingPattern(StonecutterRecipe recipe, AEItemKey in, AEItemKey out,
            boolean allowSubstitutes) {
        Preconditions.checkNotNull(recipe, "recipe");
        Preconditions.checkNotNull(in, "in");
        Preconditions.checkNotNull(out, "out");
        return AEItems.STONECUTTING_PATTERN.asItem().encode(recipe, in, out, allowSubstitutes);
    }

    /**
     * Encodes a smithing table pattern which represents a Vanilla Smithing Table recipe.
     *
     * @param recipe           The Vanilla smithing table recipe to be encoded.
     * @param base             The base item for the smithing table, which is used to determine which item is supplied
     *                         from the ME system to craft using this pattern.
     * @param addition         The additional item for the smithing table, which is used to determine which item is
     *                         supplied from the ME system to craft using this pattern.
     * @param out              The selected output item from the smithing table recipe. Used to restore the recipe if it
     *                         is renamed later.
     * @param allowSubstitutes Controls whether the ME system will allow the use of equivalent items to craft this
     *                         recipe.
     */
    public static ItemStack encodeSmithingTablePattern(UpgradeRecipe recipe, AEItemKey base, AEItemKey addition,
            AEItemKey out,
            boolean allowSubstitutes) {
        Preconditions.checkNotNull(recipe, "recipe");
        Preconditions.checkNotNull(base, "base");
        Preconditions.checkNotNull(addition, "addition");
        Preconditions.checkNotNull(out, "out");
        return AEItems.SMITHING_TABLE_PATTERN.asItem().encode(recipe, base, addition, out, allowSubstitutes);
    }
}
