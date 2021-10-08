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

package appeng.api.implementations.items;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface IGrowableCrystal {

    /**
     * Trigger a single growth tick, which can happen multiple times in quick succession.
     * 
     * Each call should be consider as grow by the smallest step possible.
     * 
     * Must return the updated {@link ItemStack}, can change the {@link Item} once fully grown or for other reasons.
     * 
     * @param is The {@link ItemStack} to trigger
     * @return Return the updated itemstack, can be a different item.
     */
    ItemStack triggerGrowth(ItemStack is);

    /**
     * Gets the crystal growth multiplier based on the material it is submerged in.
     *
     * @param state The blockstate of the block the crystal is currently in.
     * @return The multiplier for the growth tick progress. Zero if the crystal cannot grow in the current material.
     */
    float getMultiplier(BlockState state, @Nullable Level level, @Nullable BlockPos pos);
}
