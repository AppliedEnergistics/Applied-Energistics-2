package appeng.api.inventories;

import net.minecraftforge.items.IItemHandler;

/**
 * Implementation aid for {@link InternalInventory} that ensures the platorm adapter maintains its referential equality
 * over time.
 */
public abstract class BaseInternalInventory implements InternalInventory {

    private IItemHandler platformWrapper;

    @Override
    public final IItemHandler toItemHandler() {
        if (platformWrapper == null) {
            platformWrapper = new InternalInventoryItemHandler(this);
        }
        return platformWrapper;
    }

}
