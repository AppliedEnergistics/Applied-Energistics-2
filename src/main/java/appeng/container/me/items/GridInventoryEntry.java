package appeng.container.me.items;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

public class GridInventoryEntry {
    private final long serial;

    // it is acceptable for this to be an empty ItemStack for incremental updates
    private final ItemStack item;

    private final long storedAmount;

    private final long requestableAmount;

    private final boolean craftable;

    public GridInventoryEntry(long serial, ItemStack item, long storedAmount, long requestableAmount,
                              boolean craftable) {
        this.serial = serial;
        this.item = item;
        this.storedAmount = storedAmount;
        this.requestableAmount = requestableAmount;
        this.craftable = craftable;
    }

    public long getSerial() {
        return serial;
    }

    public ItemStack getItem() {
        return item;
    }

    public long getStoredAmount() {
        return storedAmount;
    }

    public long getRequestableAmount() {
        return requestableAmount;
    }

    public boolean isCraftable() {
        return craftable;
    }

    public void write(PacketBuffer buffer) {
        buffer.writeVarLong(serial);
        buffer.writeVarLong(storedAmount);
        buffer.writeVarLong(requestableAmount);
        buffer.writeBoolean(craftable);
        buffer.writeItemStack(item, true);
    }

    public static GridInventoryEntry read(PacketBuffer buffer) {
        long serial = buffer.readVarLong();
        long storedAmount = buffer.readVarLong();
        long requestableAmount = buffer.readVarLong();
        boolean craftable = buffer.readBoolean();
        ItemStack item = buffer.readItemStack();
        return new GridInventoryEntry(serial, item, storedAmount, requestableAmount, craftable);
    }

}
