package appeng.container.me.crafting;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

/**
 * Describes an entry in a crafting job, which describes how many items of one type are yet to be crafted, or currently
 * scheduled to be crafted.
 */
public class CraftingStatusEntry {
    private final long serial;
    private final ItemStack item;
    private final long storedAmount;
    private final long activeAmount;
    private final long pendingAmount;

    public CraftingStatusEntry(long serial, ItemStack item, long storedAmount, long activeAmount, long pendingAmount) {
        this.serial = serial;
        this.item = item;
        this.storedAmount = storedAmount;
        this.activeAmount = activeAmount;
        this.pendingAmount = pendingAmount;
    }

    public long getSerial() {
        return serial;
    }

    public long getActiveAmount() {
        return activeAmount;
    }

    public long getStoredAmount() {
        return storedAmount;
    }

    public long getPendingAmount() {
        return pendingAmount;
    }

    public ItemStack getItem() {
        return item;
    }

    public void write(PacketBuffer buffer) {
        buffer.writeVarLong(serial);
        buffer.writeVarLong(activeAmount);
        buffer.writeVarLong(storedAmount);
        buffer.writeVarLong(pendingAmount);
        buffer.writeItemStack(item, true);
    }

    public static CraftingStatusEntry read(PacketBuffer buffer) {
        long serial = buffer.readVarLong();
        long missingAmount = buffer.readVarLong();
        long storedAmount = buffer.readVarLong();
        long craftAmount = buffer.readVarLong();
        ItemStack item = buffer.readItemStack();
        return new CraftingStatusEntry(serial, item, storedAmount, missingAmount, craftAmount);
    }

}
