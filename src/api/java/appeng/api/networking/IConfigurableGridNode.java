package appeng.api.networking;

import java.util.Set;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;

import appeng.api.util.AEColor;

/**
 * This interface is intended for the host that created this node. It is used to configure the node's properties.
 */
public interface IConfigurableGridNode extends IGridNode {
    /**
     * By destroying your node, you destroy any connections, and its existence in the grid, use in invalidate, or
     * onChunkUnload. The node is marked as not ready. It should not be reused.
     */
    void destroy();

    /**
     * Mark as node as ready, which means it'll try to make a connection to adjacent nodes if it's exposed on the host,
     * and it'll be available for connections from other nodes.
     */
    void markReady();

    /**
     * inform the node that your IGridBlock has changed its internal state, and force the node to update.
     * <p>
     * ALWAYS make sure that your tile entity is in the world, and has its node properly saved to be returned from the
     * host before updating state,
     * <p>
     * If your entity is not in the world, or if you IGridHost returns a different node for the same side you will
     * likely crash the game.
     */
    void updateState();

    /**
     * this should be called for each node you create, if you have a nodeData compound to load from, you can store all
     * your nods on a single compound using name.
     * <p>
     * Important: You must call this before updateState.
     *
     * @param name     nbt name
     * @param nodeData to be loaded data
     */
    void loadFromNBT(@Nonnull String name, @Nonnull CompoundNBT nodeData);

    /**
     * this should be called for each node you maintain, you can save all your nodes to the same tag with different
     * names, if you fail to complete the load / save procedure, network state may be lost between game load/saves.
     *
     * @param name     nbt name
     * @param nodeData to be saved data
     */
    void saveToNBT(@Nonnull String name, @Nonnull CompoundNBT nodeData);

    /**
     * Changes the sides of the node's host this node is exposed on.
     */
    void setExposedOnSides(@Nonnull Set<Direction> directions);

    /**
     * tell the node who was responsible for placing it, failure to do this may result in in-compatibility with the
     * security system. Called instead of loadFromNBT when initially placed, once set never required again, the value is
     * saved with the Node NBT.
     *
     * @param ownerPlayerId ME player id of the owner. See {@link appeng.api.features.IPlayerRegistry}.
     */
    void setOwningPlayerId(int ownerPlayerId);

    /**
     * @param usagePerTick The power in AE/t that will be drained by this node.
     */
    void setIdlePowerUsage(@Nonnegative double usagePerTick);

    /**
     * Sets an itemstack that will only be used to represent this grid node in user interfaces. Can be set to
     * {@link ItemStack#EMPTY} to hide the node from UIs.
     */
    void setVisualRepresentation(@Nonnull ItemStack visualRepresentation);

    /**
     * Colors can be used to prevent adjacent grid nodes from connecting. {@link AEColor#TRANSPARENT} indicates that the
     * node will connect to nodes of any color.
     */
    void setGridColor(@Nonnull AEColor gridColor);

    /**
     * Adds a service to this grid node that can be used by overlay grids such as
     * {@link appeng.api.networking.crafting.ICraftingGrid}.
     *
     * @param serviceClass The service interface that should be provided by this node.
     * @param service      The implementation of the service.
     * @throws IllegalStateException If the node has already been marked as ready. Services can only be added while a
     *                               node has not been fully initialized yet.
     */
    <T extends IGridNodeService> void addService(Class<T> serviceClass, T service);
}
