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

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The default strategy for moving block entities in/out of spatial storage. Can be extended to create custom logic that
 * runs after {@link #completeMove} or prevents moving specific entities in {@link #beginMove} by returning null.
 * <p/>
 * The default strategy uses {@link BlockEntity#saveWithId()} in {@link #beginMove} to persist the block entity data
 * before it is removed, and then creates a new block entity at the target position using
 * {@link BlockEntity#loadStatic(BlockPos, BlockState, CompoundTag)} in {@link #completeMove}.
 */
public abstract class DefaultBlockEntityMoveStrategy implements IBlockEntityMoveStrategy {

    @Nullable
    @Override
    public CompoundTag beginMove(BlockEntity blockEntity) {
        return blockEntity.saveWithId();
    }

    @Override
    public boolean completeMove(BlockEntity blockEntity, CompoundTag savedData, Level newLevel, BlockPos newPosition) {
        var be = BlockEntity.loadStatic(newPosition, blockEntity.getBlockState(), savedData);
        if (be != null) {
            newLevel.setBlockEntity(be);
            return true;
        } else {
            return false;
        }
    }

}
