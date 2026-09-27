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

package appeng.api.movable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * The default strategy for moving block entities in/out of spatial storage. Can be extended to create custom logic that
 * runs after {@link #completeMove} or prevents moving specific entities in {@link IBlockEntityMoveStrategy#beginMove}
 * by returning null.
 * <p/>
 * The default strategy uses {@link BlockEntity#saveWithId(ValueOutput)} in {@link IBlockEntityMoveStrategy#beginMove}
 * to persist the block entity data before it is removed, and then creates a new block entity at the target position
 * using {@link BlockEntity#loadStatic(BlockPos, BlockState, CompoundTag, HolderLookup.Provider)} in
 * {@link #completeMove}.
 */
public abstract class DefaultBlockEntityMoveStrategy implements IBlockEntityMoveStrategy {

    @Override
    public CompoundTag beginMove(BlockEntity blockEntity, HolderLookup.Provider registries) {
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
        blockEntity.saveWithId(output);
        return output.buildResult();
    }

    @Override
    public boolean completeMove(BlockEntity blockEntity, BlockState state, CompoundTag savedData, Level newLevel,
            BlockPos newPosition) {
        var be = BlockEntity.loadStatic(newPosition, state, savedData, newLevel.registryAccess());
        if (be != null) {
            newLevel.setBlockEntity(be);
            return true;
        } else {
            return false;
        }
    }

}
