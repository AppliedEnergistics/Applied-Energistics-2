package appeng.api.inventories;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Adapts an {@link InternalInventory} to the {@link Container} interface in a read-only fashion.
 */
class ContainerAdapter implements Container {
    private final InternalInventory inventory;

    public ContainerAdapter(InternalInventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public int getContainerSize() {
        return inventory.size();
    }

    @Override
    public boolean isEmpty() {
        return !inventory.iterator().hasNext();
    }

    @Override
    public ItemStack getItem(int slotIndex) {
        return inventory.getStackInSlot(slotIndex);
    }

    @Override
    public void setItem(int slotIndex, ItemStack stack) {
        inventory.setItemDirect(slotIndex, stack);
    }

    @Override
    public ItemStack removeItem(int slotIndex, int count) {
        return this.inventory.extractItem(slotIndex, count, false);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slotIndex) {
        return this.inventory.extractItem(slotIndex, this.inventory.getSlotLimit(slotIndex), false);
    }

    /**
     * Since our inventories support a per-slot max size, we find the largest max-size allowable and return that.
     */
    @Override
    public int getMaxStackSize() {
        int max = 0;
        for (int i = 0; i < inventory.size(); ++i) {
            max = Math.max(max, inventory.getSlotLimit(i));
        }
        return max;
    }

    @Override
    public boolean canPlaceItem(int slotIndex, ItemStack stack) {
        return inventory.isItemValid(slotIndex, stack);
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < inventory.size(); i++) {
            inventory.setItemDirect(i, ItemStack.EMPTY);
        }
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

}
