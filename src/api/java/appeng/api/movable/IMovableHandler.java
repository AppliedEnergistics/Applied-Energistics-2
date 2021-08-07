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

public interface IMovableHandler {

    /**
     * if you return true from this, your saying you can handle the class, not that single entity, you cannot opt out of
     * single entities.
     *
     * @param type The type of block entity to move.
     *
     * @return true if it can handle moving
     */
    boolean canHandle(BlockEntityType<?> type);

    /**
     * request that the handler move the block entity from its current location to the new one. the block entity has already been
     * invalidated, and the blocks have already been fully moved.
     * <p/>
     * You are responsible for adding the new block entity to the target world, i.e. using
     * {@link Level#setBlockEntity(BlockEntity)}.
     *
     * Potential Example:
     *
     * <pre>
     * {
     *     &#064;code
     *     Chunk c = world.getChunkAt(x, z);
     *     c.setChunkBlockBlockentity(x &amp; 0xF, y + y, z &amp; 0xF, blockEntity);
     *
     *     if (c.isChunkLoaded) {
     *         world.addBlockentity(blockEntity);
     *         world.markBlockForUpdate(x, y, z);
     *     }
     * }
     * </pre>
     *
     * @param entity      to be moved block entity
     * @param savedData   the original entities data saved using {@link BlockEntity#save(CompoundTag)}.
     * @param world       world of block entity
     * @param newPosition the new location
     * @return True if moving succeeded. If false is returned, AE2 will attempt to recover the original entity.
     */
    @Nullable
    boolean moveBlockEntity(BlockEntity entity, CompoundTag savedData, Level world, BlockPos newPosition);
}
