package appeng.container.me.common;

import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;
import net.minecraft.network.PacketBuffer;

import javax.annotation.Nullable;

public class GridInventoryEntry<T extends IAEStack<T>> {
    private final long serial;

    @Nullable
    private final T stack;

    private final long storedAmount;

    private final long requestableAmount;

    private final boolean craftable;

    public GridInventoryEntry(long serial, @Nullable T stack, long storedAmount, long requestableAmount,
                              boolean craftable) {
        this.serial = serial;
        this.stack = stack;
        this.storedAmount = storedAmount;
        this.requestableAmount = requestableAmount;
        this.craftable = craftable;
    }

    public long getSerial() {
        return serial;
    }

    public T getStack() {
        return stack;
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
        buffer.writeBoolean(stack != null);
        if (stack != null) {
            stack.writeToPacket(buffer);
        }
        buffer.writeVarLong(storedAmount);
        buffer.writeVarLong(requestableAmount);
        buffer.writeBoolean(craftable);
    }

    public static <T extends IAEStack<T>> GridInventoryEntry<T> read(IStorageChannel<T> storageChannel, PacketBuffer buffer) {
        long serial = buffer.readVarLong();
        T stack = null;
        if (buffer.readBoolean()) {
            stack = storageChannel.readFromPacket(buffer);
        }
        long storedAmount = buffer.readVarLong();
        long requestableAmount = buffer.readVarLong();
        boolean craftable = buffer.readBoolean();
        return new GridInventoryEntry<>(serial, stack, storedAmount, requestableAmount, craftable);
    }

    public boolean isMeaningful() {
        return storedAmount > 0 || requestableAmount > 0 || craftable;
    }
}
