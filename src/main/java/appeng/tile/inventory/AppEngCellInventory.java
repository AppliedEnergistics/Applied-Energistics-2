package appeng.tile.inventory;


import appeng.api.storage.ICellInventory;
import appeng.api.storage.ICellInventoryHandler;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.filter.IAEItemFilter;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;


public class AppEngCellInventory implements IItemHandlerModifiable {
    private final AppEngInternalInventory inv;
    private final ICellInventoryHandler[] handlerForSlot;

    public AppEngCellInventory(final IAEAppEngInventory host, final int slots) {
        this.inv = new AppEngInternalInventory(host, slots, 1);
        this.handlerForSlot = new ICellInventoryHandler[slots];
    }

    public void setHandler(final int slot, final ICellInventoryHandler handler) {
        this.handlerForSlot[slot] = handler;
    }

    public void setFilter(IAEItemFilter filter) {
        this.inv.setFilter(filter);
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        this.persist(slot);
        this.inv.setStackInSlot(slot, stack);
        this.cleanup(slot);
    }

    @Override
    public int getSlots() {
        return this.inv.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        this.persist(slot);
        return this.inv.getStackInSlot(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        this.persist(slot);
        final ItemStack ret = this.inv.insertItem(slot, stack, simulate);
        this.cleanup(slot);
        return ret;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        this.persist(slot);
        final ItemStack ret = this.inv.extractItem(slot, amount, simulate);
        this.cleanup(slot);
        return ret;
    }

    @Override
    public int getSlotLimit(int slot) {
        return this.inv.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return this.inv.isItemValid(slot, stack);
    }

    public void persist() {
        for (int i = 0; i < this.getSlots(); ++i) {
            this.persist(i);
        }
    }

    private void persist(int slot) {
        if (this.handlerForSlot[slot] != null) {
            final ICellInventory ci = this.handlerForSlot[slot].getCellInv();
            if (ci != null) {
                ci.persist();
            }
        }
    }

    private void cleanup(int slot) {
        if (this.handlerForSlot[slot] != null) {
            final ICellInventory ci = this.handlerForSlot[slot].getCellInv();

            if (ci == null || ci.getItemStack() != this.inv.getStackInSlot(slot)) {
                this.handlerForSlot[slot] = null;
            }
        }
    }
}
