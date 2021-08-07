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

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * Used to determine if a block entity is marked as movable, a block will be considered movable, if...
 *
 * 1. The Block Entity or its super classes have been white listed with whiteListBlockentity.
 *
 * 2. The Block Entity implements IMovableBlockEntity
 *
 * 3. A IMovableHandler is register that returns canHandle = true for the {@link BlockEntity} subclass
 *
 *
 * The movement process is as follows,
 *
 * 1. IMovableBlockEntity.prepareToMove() or Blockentity.invalidate() depending on your opt-in method. 2. The block
 * entity will be removed from the world. 3. Its world, coordinates will be changed. *** this can be overridden with a
 * IMovableHandler *** 4. It will then be re-added to the world, or a new world. 5. Blockentity.clearRemoved() 6.
 * IMovableBlockEntity.doneMoving ( if you implemented IMovableBlockEntity )
 *
 * Please note, this is a 100% white list only feature, I will never opt in any non-vanilla, non-AE blocks. If you do
 * not want to support your block entities being moved, you don't have to do anything.
 *
 * I appreciate anyone that takes the effort to get their block entities to work with this system to create a better use
 * experience.
 *
 * If you need a build of deobf build of AE for testing, do not hesitate to ask.
 */
public interface IMovableRegistry {

    /**
     * Black list a block from movement, please only use this to prevent exploits.
     *
     * @param blk block
     */
    void blacklistBlock(Block blk);

    /**
     * White list your block entity with the registry.
     *
     * If you block entity is handled with IMovableHandler or IMovableBlockEntity you do not need to white list it.
     */
    void whitelistBlockEntity(BlockEntityType<?> type);

    /**
     * @param be to be moved block entity
     *
     * @return true if the block entity has accepted your request to move it
     */
    boolean askToMove(BlockEntity be);

    /**
     * tells the block entity you are done moving it.
     *
     * @param be moved block entity
     */
    void doneMoving(BlockEntity be);

    /**
     * add a new handler movable handler.
     *
     * @param handler moving handler
     */
    void addHandler(IMovableHandler handler);

    /**
     * handlers are used to perform movement, this allows you to override AE's internal version.
     *
     * only valid after askToMove(...) = true
     *
     * @param be block entity
     *
     * @return moving handler of block entity
     */
    IMovableHandler getHandler(BlockEntity be);

    /**
     * @return a copy of the default handler
     */
    IMovableHandler getDefaultHandler();

    /**
     * @param blk block
     *
     * @return true if this block is blacklisted
     */
    boolean isBlacklisted(Block blk);
}
