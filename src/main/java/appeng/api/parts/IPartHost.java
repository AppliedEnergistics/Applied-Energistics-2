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

package appeng.api.parts;

import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import appeng.api.util.AEColor;
import appeng.api.util.DimensionalBlockPos;

/**
 * Implemented on AE's block entities.
 *
 * Do Not Implement
 */
public interface IPartHost extends ICustomCableConnection {

    /**
     * @return the facade container
     */
    IFacadeContainer getFacadeContainer();

    /**
     * Get a part attached to the host based on the location it's attached to.
     *
     * @param side side of host or null for center.
     *
     * @return the part located on the specified side, or null if there is no part.
     */
    @Nullable
    IPart getPart(@Nullable Direction side);

    /**
     * Test if you can add a part to the specified side of the Part Host. A null side represents the center of the part
     * host, where a cable would normally be.
     *
     * @param part to be added part
     * @param side onto side or null for center of host.
     *
     * @return returns false if the part cannot be added.
     */
    boolean canAddPart(ItemStack part, @Nullable Direction side);

    /**
     * Add a new part to the specified side, returns false if it failed to be added.
     *
     * @param is    new part
     * @param side  onto side or null for center of host.
     * @param owner with owning player
     * @return If the part could be placed.
     */
    boolean addPart(ItemStack is, @Nullable Direction side, @Nullable Player owner);

    /**
     * Replace an existing part on the specific side with a new one. Returns false if it failed to be replaced.
     *
     * @param is    new part
     * @param side  onto side or null for center of host.
     * @param owner with owning player
     * @return If the part could be replaced.
     */
    boolean replacePart(ItemStack is, @Nullable Direction side, @Nullable Player owner, @Nullable InteractionHand hand);

    /**
     * Removes the part on the side, this doesn't drop it or anything, if you don't do something with it, its just
     * "gone" and its never coming back; think about it.
     *
     * if you want to drop the part you must request it prior to removing it.
     *
     * @param side onto side or null for center of host
     */
    void removePart(@Nullable Direction side);

    /**
     * something changed, might want to send a packet to clients to update state.
     */
    void markForUpdate();

    /**
     * @return the physical location of the part host in the universe.
     */
    DimensionalBlockPos getLocation();

    /**
     * @return the block entity for the host, this can either be an FMP block entity, or a AE block entity
     */
    BlockEntity getBlockEntity();

    /**
     * @return the color of the host type ( this is determined by the middle cable. ) if no cable is present, it returns
     *         {@link AEColor} .Transparent other wise it returns the color of the cable in the center.
     */
    AEColor getColor();

    /**
     * destroys the part container, for internal use.
     */
    void clearContainer();

    /**
     * Used to test for FMP microblock blocking internally.
     *
     * @return returns if microblocks are blocking this cable path.
     */
    boolean isBlocked(Direction side);

    /**
     * Finds the part located at the position in block-local coordinates (0,0,0 is located at the block pos).
     *
     * @param pos part position
     *
     * @return a new SelectedPart, this is never null.
     */
    SelectedPart selectPartLocal(Vec3 pos);

    /**
     * Same as {@link #selectPartLocal(Vec3)}, but with world instead of local coordinates. Provided for easier
     * interoperability with {@link BlockHitResult#getLocation()}.
     */
    default SelectedPart selectPartWorld(Vec3 pos) {
        var worldPos = getLocation();
        return selectPartLocal(pos.subtract(
                worldPos.getPos().getX(),
                worldPos.getPos().getY(),
                worldPos.getPos().getZ()));
    }

    /**
     * can be used by parts to trigger the block entity or part to save.
     */
    void markForSave();

    void partChanged();

    /**
     * get the redstone state of host on this side, this value is cached internally.
     *
     * @param side side of part
     *
     * @return true of the part host is receiving redstone from an external source.
     */
    boolean hasRedstone(@Nullable Direction side);

    /**
     * returns false if this block contains any parts or facades, true other wise.
     */
    boolean isEmpty();

    /**
     * remove host from level...
     */
    void cleanup();

    /**
     * notify neighbors uf updated status.
     */
    void notifyNeighbors();

    /**
     * true if the block entity is in the world, other wise false.
     *
     * @return true if block entity is in world
     */
    boolean isInWorld();

}
