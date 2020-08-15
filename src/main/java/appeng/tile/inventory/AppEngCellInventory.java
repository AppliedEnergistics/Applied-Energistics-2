package appeng.tile.inventory;

import net.minecraft.item.ItemStack;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.item.filter.ItemFilter;
import alexiil.mc.lib.attributes.item.impl.DelegatingFixedItemInv;

import appeng.api.storage.cells.ICellInventory;
import appeng.api.storage.cells.ICellInventoryHandler;
import appeng.core.Api;
import appeng.util.inv.IAEAppEngInventory;

public class AppEngCellInventory extends DelegatingFixedItemInv {
    private static final ItemFilter CELL_FILTER = stack -> !stack.isEmpty()
            && Api.instance().registries().cell().isCellHandled(stack);

    private final ICellInventoryHandler<?>[] handlerForSlot;

    public AppEngCellInventory(final IAEAppEngInventory host, final int slots) {
        super(new AppEngInternalInventory(host, slots, 1));
        this.handlerForSlot = new ICellInventoryHandler[slots];
    }

    @Override
    public ItemFilter getFilterForSlot(int slot) {
        return CELL_FILTER;
    }

    public void setHandler(final int slot, final ICellInventoryHandler<?> handler) {
        this.handlerForSlot[slot] = handler;
    }

    @Override
    public boolean setInvStack(int slot, ItemStack to, Simulation simulation) {
        this.persist(slot);
        boolean result = super.setInvStack(slot, to, simulation);
        this.cleanup(slot);
        return result;
    }

    @Override
    public ItemStack getInvStack(int slot) {
        this.persist(slot);
        return super.getInvStack(slot);
    }

    private void persist(int slot) {
        if (this.handlerForSlot[slot] != null) {
            final ICellInventory<?> ci = this.handlerForSlot[slot].getCellInv();
            if (ci != null) {
                ci.persist();
            }
        }
    }

    private void cleanup(int slot) {
        if (this.handlerForSlot[slot] != null) {
            final ICellInventory<?> ci = this.handlerForSlot[slot].getCellInv();

            if (ci == null || ci.getItemStack() != super.getInvStack(slot)) {
                this.handlerForSlot[slot] = null;
            }
        }
    }
}
