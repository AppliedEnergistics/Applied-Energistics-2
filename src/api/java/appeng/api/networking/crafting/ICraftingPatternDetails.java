/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 AlgorithmX2
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

package appeng.api.networking.crafting;

import java.util.List;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import appeng.api.storage.data.IAEItemStack;

/**
 * Describes a crafting or processing pattern decoded by {@link appeng.api.crafting.ICraftingHelper}.
 * <p>
 * Do not cache instances of this class unless you handle recipe reloads on the server and client correctly.
 */
public interface ICraftingPatternDetails {

    /**
     * @return encodes this crafting pattern into a new item stack.
     */
    ItemStack getPattern();

    /**
     * @param slotIndex specific slot index
     * @param itemStack item in slot
     * @param world     crafting world
     * @return if an item can be used in the specific slot for this pattern.
     */
    boolean isValidItemForSlot(int slotIndex, ItemStack itemStack, World world);

    /**
     * @return if this pattern is for a Vanilla {@link net.minecraft.item.crafting.IRecipeType#CRAFTING crafting
     *         recipe}.
     */
    boolean isCraftable();

    /**
     * Equal itemstacks will be aggregated into one, respectively 3*64 will be returned as one stack of 192, up to 576
     * of a single type.
     * <p>
     * This should be the preferred way to deal with the list of inputs.
     * <p>
     * The list will be sorted in descending order by stack size. However there is no guarantee about maintaining the
     * placement order of the inputs in case of equal values.
     *
     * @return an immutable list of inputs without nulls
     */
    List<IAEItemStack> getInputs();

    /**
     * Equal itemstacks will be aggregated into one, respectively 2*32 will be returned as one stack of 64, up to 192 of
     * a single type.
     * <p>
     * This should be the preferred way to deal with the list of outputs.
     * <p>
     * <p>
     * The list will be sorted in descending order by stack size. However there is no guarantee about maintaining the
     * placement order of the outputs in case of equal values.
     *
     * @return an immutable list of outputs without nulls
     */
    List<IAEItemStack> getOutputs();

    /**
     * A sparse list representing the placement order of a crafting grid, left to right, then top to bottom.
     * <p>
     * Only use when absolutely necessary, always prefer {@link ICraftingPatternDetails#getInputs()}
     * <p>
     * This will contain exactly 9 entries.
     * <p>
     * This can return a copy from the internal structure, so there are no guarantees about modifications.
     *
     * @return a list of the inputs, will include nulls.
     */
    IAEItemStack[] getSparseInputs();

    /**
     * A sparse list representing the placement order of the respective output slots.
     * <p>
     * Only use when absolutely necessary, always prefer {@link ICraftingPatternDetails#getOutputs()}
     * <p>
     * This will either contain 1 entry for crafting patterns or 3 for processing.
     * <p>
     * This can return a copy from the internal structure, so there are no guarantees about modifications.
     *
     * @return a list of the outputs, will include nulls.
     */
    IAEItemStack[] getSparseOutputs();

    /**
     * @return if this pattern is enabled to support substitutions.
     */
    boolean canSubstitute();

    List<IAEItemStack> getSubstituteInputs(int slot);

    /**
     * Allow using this INSTANCE of the pattern details to preform the crafting action with performance enhancements.
     *
     * @param craftingInv inventory
     * @param world       crafting world
     * @return the crafted ( work bench ) item.
     */
    ItemStack getOutput(CraftingInventory craftingInv, World world);

    /**
     * Get the priority of this pattern
     *
     * @return the priority of this pattern
     */
    int getPriority();

    /**
     * Set the priority the of this pattern.
     *
     * @param priority priority of pattern
     */
    void setPriority(int priority);
}
