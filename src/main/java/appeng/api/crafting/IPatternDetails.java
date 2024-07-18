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

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;

import appeng.api.implementations.blockentities.ICraftingMachine;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;

/**
 * Information about a pattern for use by the autocrafting system.
 * <p/>
 * <strong>Implementing classes need to properly implement equals/hashCode for crafting jobs to resume properly after
 * world or chunk reloads.</strong>
 */
public interface IPatternDetails {
    /**
     * Return the type of the encoded item of this pattern, containing all the data to retrieve the pattern later from
     * {@link PatternDetailsHelper#decodePattern}.
     */
    AEItemKey getDefinition();

    /**
     * The inputs of this pattern. <b>The return array must never be edited</b>.
     */
    IInput[] getInputs();

    /**
     * The primary output of this pattern. The pattern will only be used to craft the primary output; the others are
     * just byproducts.
     */
    default GenericStack getPrimaryOutput() {
        return getOutputs()[0];
    }

    /**
     * The outputs of this pattern. <b>The return array or any of its stacks must never be edited</b>.
     */
    GenericStack[] getOutputs();

    /**
     * @return True if this pattern allows its inputs to be pushed to generic external inventories that would accept
     *         those inputs. This would usually be true for custom processing patterns, but not true for patterns that
     *         require custom machines or molecular assemblers (since those are pushed via
     *         {@link ICraftingMachine#pushPattern}).
     */
    default boolean supportsPushInputsToExternalInventory() {
        return true;
    }

    /**
     * Gives the pattern a chance to reorder its inputs for pushing to external inventories (i.e. NOT to
     * {@link ICraftingMachine}s).
     *
     * @param inputHolder For each {@link IInput}, the relevant items. The ownership is given to the pattern, do
     *                    whatever with the key counters as long as all of their contents end up in the input sink.
     * @param inputSink   Where to push the inputs to.
     */
    default void pushInputsToExternalInventory(KeyCounter[] inputHolder, PatternInputSink inputSink) {
        for (var inputList : inputHolder) {
            for (var input : inputList) {
                inputSink.pushInput(input.getKey(), input.getLongValue());
            }
        }
    }

    interface IInput {
        /**
         * A list of possible inputs for this pattern: the first input is the primary input, others are just substitutes
         * that will be used if available but won't be autocrafted. For example you can return [1000 mb of water fluid,
         * 1 bucket of water item] to use water if possible, but use stored buckets otherwise.
         * <p>
         * <b>The return array or any of its stacks must never be edited</b>.
         */
        GenericStack[] getPossibleInputs();

        /**
         * Multiplier for the inputs: how many possible inputs are necessary to craft this pattern.
         */
        long getMultiplier();

        /**
         * Check if the passed stack is a valid input.
         */
        boolean isValid(AEKey input, Level level);

        /**
         * Optionally return a remaining key. This will generally be null for processing patterns, and return the
         * corresponding slot of {@link Recipe#getRemainingItems} for crafting patterns.
         */
        @Nullable
        AEKey getRemainingKey(AEKey template);
    }

    interface PatternInputSink {
        void pushInput(AEKey key, long amount);
    }
}
