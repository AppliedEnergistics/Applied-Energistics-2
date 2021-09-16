package appeng.util.inv;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import appeng.api.inventories.InternalInventory;

/**
 * Exposes the carried item stored in a menu as an {@link InternalInventory}.
 */
public class CarriedItemInventory implements InternalInventory {
    private final AbstractContainerMenu menu;

    public CarriedItemInventory(AbstractContainerMenu menu) {
        this.menu = menu;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        Preconditions.checkArgument(slotIndex == 0);
        return menu.getCarried();
    }

    @Override
    public void setItemDirect(int slotIndex, @Nonnull ItemStack stack) {
        Preconditions.checkArgument(slotIndex == 0);
        menu.setCarried(stack);
    }
}
