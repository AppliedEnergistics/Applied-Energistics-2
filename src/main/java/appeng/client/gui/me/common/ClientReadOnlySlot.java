package appeng.client.gui.me.common;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

/**
 * Base class for virtual client-side only slots that do not allow Vanilla to directly interact with the contained item
 * (since it purely exists client-side).
 */
public class ClientReadOnlySlot extends Slot {
    /**
     * We use this fake/empty inventory to prevent other mods from attempting to interact with anything based on this
     * slot's inventory/slot index.
     */
    private static final IInventory EMPTY_INVENTORY = new Inventory(0);

    public ClientReadOnlySlot(int xPosition, int yPosition) {
        super(EMPTY_INVENTORY, 0, xPosition, yPosition);
    }

    @Override
    public final boolean isItemValid(final ItemStack stack) {
        return false;
    }

    @Override
    public final void putStack(final ItemStack stack) {
    }

    @Override
    public final int getSlotStackLimit() {
        return 0;
    }

    @Override
    public final ItemStack decrStackSize(int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public final boolean canTakeStack(PlayerEntity player) {
        return false;
    }
}
