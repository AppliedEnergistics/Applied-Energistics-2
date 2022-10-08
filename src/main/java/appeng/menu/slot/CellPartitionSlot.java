package appeng.menu.slot;

import net.minecraft.world.item.ItemStack;

import appeng.api.inventories.InternalInventory;
import appeng.client.Point;

public class CellPartitionSlot extends FakeSlot implements IOptionalSlot {

    private final IPartitionSlotHost host;
    private final int slot;

    public CellPartitionSlot(InternalInventory inv, IPartitionSlotHost containerBus, int invSlot) {
        super(inv, invSlot);
        this.host = containerBus;
        this.slot = invSlot;
    }

    @Override
    public ItemStack getItem() {
        if (!this.isSlotEnabled()) {
            this.clearStack();
        }

        return super.getItem();
    }

    @Override
    public boolean isSlotEnabled() {
        if (this.host == null) {
            return false;
        }

        return this.host.isPartitionSlotEnabled(this.slot);
    }

    @Override
    public boolean isRenderDisabled() {
        return true;
    }

    @Override
    public Point getBackgroundPos() {
        return new Point(x - 1, y - 1);
    }
}
