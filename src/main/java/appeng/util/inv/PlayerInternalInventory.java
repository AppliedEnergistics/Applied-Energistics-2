package appeng.util.inv;

import javax.annotation.Nonnull;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import appeng.api.inventories.InternalInventory;

/**
 * Exposes the main player inventory and hotbar as an {@link InternalInventory}.
 */
public class PlayerInternalInventory implements InternalInventory {
    private final Inventory inventory;

    public PlayerInternalInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public int size() {
        return Inventory.INVENTORY_SIZE;
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        return inventory.getItem(slotIndex);
    }

    @Override
    public void setItemDirect(int slotIndex, @Nonnull ItemStack stack) {
        inventory.setItem(slotIndex, stack);
        if (!stack.isEmpty()) {
            inventory.getItem(slotIndex).setPopTime(5);
        }
    }

}
