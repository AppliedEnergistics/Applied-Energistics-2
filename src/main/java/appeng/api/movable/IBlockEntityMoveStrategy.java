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

package appeng.api.movable;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * A strategy for moving block entities in and out of spatial storage.
 */
public interface IBlockEntityMoveStrategy {

    /**
     * Tests if this strategy is capable of moving the given block entity type.
     */
    boolean canHandle(BlockEntityType<?> type);

    /**
     * Called to begin moving a block entity.
     *
     * @param blockEntity The block entity to move.
     * @return The saved representation of the block entity that can be used by this strategy to restore the block
     *         entity at the target position. Return null to prevent the block entity from being moved.
     */
    @Nullable
    CompoundTag beginMove(BlockEntity blockEntity);

    /**
     * Complete moving a block entity for which a move was initiated successfully with {@link #beginMove(BlockEntity)}.
     * The block entity has already been invalidated, and the blocks have already been fully moved.
     * <p/>
     * You are responsible for adding the new block entity to the target level, i.e. using
     * {@link Level#setBlockEntity(BlockEntity)}.
     *
     * @param entity      The block entity being moved, which has already been removed from the original chunk and
     *                    should not be reused.
     * @param savedData   Data saved by this strategy in {@link #beginMove(BlockEntity)}.
     * @param newLevel    Level to moved to
     * @param newPosition Position to move to
     * @return True if moving succeeded. If false is returned, AE2 will attempt to recover the original entity.
     */
    boolean completeMove(BlockEntity entity, CompoundTag savedData, Level newLevel, BlockPos newPosition);

}
