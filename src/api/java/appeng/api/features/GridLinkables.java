package appeng.api.features;

import java.util.IdentityHashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import com.google.common.base.Preconditions;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

/**
 * A registry for items that can be linked to a specific network using for example the security station's user
 * interface.
 * <p/>
 * This can be used by items like wireless terminals to encode the network security key in their NBT. This security key
 * can then be used to locate the grid for that security key later, when the item wants to interact with the grid.
 */
@ThreadSafe
public final class GridLinkables {

    private static final Map<Item, IGridLinkableHandler> registry = new IdentityHashMap<>();

    private GridLinkables() {
    }

    /**
     * Register a handler to link or unlink stacks of a given item with a network.
     *
     * @param itemLike The type of item to register a handler for.
     * @param handler  The handler that handles linking and unlinking for the item stacks.
     */
    public synchronized static void register(ItemLike itemLike, IGridLinkableHandler handler) {
        Preconditions.checkNotNull(itemLike, "itemLike");
        Preconditions.checkNotNull(itemLike.asItem(), "itemLike.asItem()");
        Preconditions.checkNotNull(handler, "handler");
        var item = itemLike.asItem();
        Preconditions.checkState(!registry.containsKey(item), "Handler for %s already registered", item);
        registry.put(item, handler);
    }

    /**
     * Gets the registered handler for a given item.
     */
    @Nullable
    public static synchronized IGridLinkableHandler get(ItemLike itemLike) {
        Preconditions.checkNotNull(itemLike, "itemLike");
        Preconditions.checkNotNull(itemLike.asItem(), "itemLike.asItem()");
        return registry.get(itemLike.asItem());
    }

}
