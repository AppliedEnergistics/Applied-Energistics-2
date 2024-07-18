package appeng.menu.slot;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import appeng.api.inventories.InternalInventory;
import appeng.api.storage.StorageCells;
import appeng.client.Point;
import appeng.core.localization.GuiText;
import appeng.core.localization.Tooltips;

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
        return this.host.isPartitionSlotEnabled(this.slot);
    }

    @Override
    public void set(ItemStack is) {
        if (canFitInsideCell(is)) {
            super.set(is);
        }
    }

    @Override
    public boolean isRenderDisabled() {
        return true;
    }

    @Override
    public Point getBackgroundPos() {
        return new Point(x - 1, y - 1);
    }

    @Override
    public @Nullable List<Component> getCustomTooltip(ItemStack carriedItem) {
        if (!canFitInsideCell(carriedItem)) {
            return List.of(Tooltips.of(GuiText.CantFitInsideStorageCell, Tooltips.RED));
        }

        return super.getCustomTooltip(carriedItem);
    }

    private boolean canFitInsideCell(ItemStack stack) {
        // Prevent adding items to the partition that cannot be stored in the cell in the first place
        var cellInv = StorageCells.getCellInventory(stack, null);
        return cellInv == null || cellInv.canFitInsideCell();
    }
}
