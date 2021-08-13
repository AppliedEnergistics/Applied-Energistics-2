package appeng.api.features;

import net.minecraft.world.item.ItemStack;

/**
 * Handles the linking of items to specific grids when they're put into the security terminal linking slot.
 * <p/>
 * 
 * @see GridLinkables
 */
public interface IGridLinkableHandler {

    /**
     * Tests if the given item stack supports being linked to a grid.
     */
    boolean canLink(ItemStack stack);

    /**
     * Link the given stack to the given grid security key.
     */
    void link(ItemStack itemStack, long securityKey);

    /**
     * Unlink the given stack from any previously linked grid.
     */
    void unlink(ItemStack itemStack);

}
