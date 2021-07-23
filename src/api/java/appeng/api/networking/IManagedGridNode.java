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

package appeng.api.networking;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import appeng.api.util.AEColor;

/**
 * This interface is intended for the host that created this node. It is used to configure the node's properties.
 */
public interface IManagedGridNode {

    /**
     * By destroying your node, you destroy any connections, and its existence in the grid, use in invalidate, or
     * onChunkUnload. After calling this method, {@link #isReady()} will return false. The node cannot be reused.
     */
    void destroy();

    /**
     * Finish creation of the node, which means it'll try to make a connection to adjacent nodes if it's exposed on the
     * host, and it'll be available for connections from other nodes.
     */
    void create(World world, @Nullable BlockPos blockPos);

    /**
     * this should be called for each node you create, if you have a nodeData compound to load from, you can store all
     * your nods on a single compound using name.
     * <p>
     * Important: You must call this before {@link #create(World, BlockPos)}.
     *
     * @param nodeData to be loaded data
     */
    void loadFromNBT(@Nonnull CompoundNBT nodeData);

    /**
     * this should be called for each node you maintain, you can save all your nodes to the same tag with different
     * names, if you fail to complete the load / save procedure, network state may be lost between game load/saves.
     *
     * @param nodeData to be saved data
     */
    void saveToNBT(@Nonnull CompoundNBT nodeData);

    /**
     * Call the given function on the grid this node is connected to. Will do nothing if the grid node isn't initialized
     * yet or has been destroyed.
     *
     * @return True if the action was called, false otherwise.
     */
    default boolean ifPresent(Consumer<IGrid> action) {
        var node = getNode();
        if (node == null) {
            return false;
        }
        var grid = node.getGrid();
        if (grid == null) {
            return false;
        }
        action.accept(grid);
        return true;
    }

    default boolean ifPresent(BiConsumer<IGrid, IGridNode> action) {
        var node = getNode();
        if (node == null) {
            return false;
        }
        var grid = node.getGrid();
        if (grid == null) {
            return false;
        }
        action.accept(grid, node);
        return true;
    }

    /**
     * Get the grid this managed grid node is currently connected to.
     *
     * @return The grid if {@link #isReady()} is true, null otherwise.
     */
    @Nullable
    default IGrid getGrid() {
        var node = getNode();
        if (node == null) {
            return null;
        }
        return node.getGrid();
    }

    IManagedGridNode setFlags(GridFlags... requireChannel);

    /**
     * Changes the sides of the node's host this node is exposed on.
     */
    IManagedGridNode setExposedOnSides(@Nonnull Set<Direction> directions);

    /**
     * @param usagePerTick The power in AE/t that will be drained by this node.
     */
    IManagedGridNode setIdlePowerUsage(@Nonnegative double usagePerTick);

    /**
     * Sets an itemstack that will only be used to represent this grid node in user interfaces. Can be set to
     * {@link ItemStack#EMPTY} to hide the node from UIs.
     */
    IManagedGridNode setVisualRepresentation(@Nonnull ItemStack visualRepresentation);

    IManagedGridNode setInWorldNode(boolean accessible);

    IManagedGridNode setTagName(String tagName);

    /**
     * Colors can be used to prevent adjacent grid nodes from connecting. {@link AEColor#TRANSPARENT} indicates that the
     * node will connect to nodes of any color.
     */
    IManagedGridNode setGridColor(@Nonnull AEColor gridColor);

    <T extends IGridNodeService> IManagedGridNode addService(Class<T> serviceClass, T service);

    /**
     * @return True if the node and its grid are available. This will never be the case on the client-side. Server-side,
     *         it'll be true after {@link #create(World, BlockPos)} and before {@link #destroy()} are called.
     */
    boolean isReady();

    boolean isActive();

    boolean isPowered();

    /**
     * tell the node who was responsible for placing it, failure to do this may result in in-compatibility with the
     * security system. Called instead of loadFromNBT when initially placed, once set never required again, the value is
     * saved with the Node NBT.
     *
     * @param ownerPlayerId ME player id of the owner. See {@link appeng.api.features.IPlayerRegistry}.
     */
    void setOwningPlayerId(int ownerPlayerId);

    /**
     * Same as {@link #setOwningPlayerId(int)}, but resolves the numeric player ID automatically.
     *
     * @param ownerPlayer The owning player.
     */
    void setOwningPlayer(PlayerEntity ownerPlayer);

    /**
     * @return The node that was created by the managed node. Will be non-null when {@link #isReady()} is true.
     */
    @Nullable
    IGridNode getNode();

}
