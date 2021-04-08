package appeng.container.me.crafting;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import appeng.api.networking.crafting.CraftingItemList;
import appeng.api.storage.data.IAEItemStack;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.util.item.IncrementalUpdateHelper;

/**
 * Describes a currently running crafting job. A crafting status can either be a full update which replaces any
 * previously kept state on the client ({@link #isFullStatus()}, or an incremental update, which uses previously sent
 * {@link CraftingStatusEntry#getSerial() serials} to update entries on the client that were previously sent. To reduce
 * the packet size for updates, the {@link CraftingStatusEntry#getItem() display item} for entries that were previously
 * sent to the client are set to {@link ItemStack#EMPTY}.
 */
public class CraftingStatus {

    public static final CraftingStatus EMPTY = new CraftingStatus(true, 0, 0, 0, Collections.emptyList());

    /**
     * True if this status update replaces any previous status information. Otherwise it should be considered an
     * incremental update.
     */
    private final boolean fullStatus;

    /**
     * @see CraftingCPUCluster#getElapsedTime()
     */
    private final long elapsedTime;

    /**
     * @see CraftingCPUCluster#getRemainingItemCount()
     */
    private final long remainingItemCount;

    /**
     * @see CraftingCPUCluster#getStartItemCount()
     */
    private final long startItemCount;

    private final List<CraftingStatusEntry> entries;

    public CraftingStatus(boolean fullStatus, long elapsedTime, long remainingItemCount, long startItemCount,
            List<CraftingStatusEntry> entries) {
        this.fullStatus = fullStatus;
        this.elapsedTime = elapsedTime;
        this.remainingItemCount = remainingItemCount;
        this.startItemCount = startItemCount;
        this.entries = ImmutableList.copyOf(entries);
    }

    public boolean isFullStatus() {
        return fullStatus;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public long getRemainingItemCount() {
        return remainingItemCount;
    }

    public long getStartItemCount() {
        return startItemCount;
    }

    public List<CraftingStatusEntry> getEntries() {
        return entries;
    }

    public void write(PacketBuffer buffer) {
        buffer.writeBoolean(fullStatus);
        buffer.writeVarLong(elapsedTime);
        buffer.writeVarLong(remainingItemCount);
        buffer.writeVarLong(startItemCount);
        buffer.writeVarInt(entries.size());
        for (CraftingStatusEntry entry : entries) {
            entry.write(buffer);
        }
    }

    public static CraftingStatus read(PacketBuffer buffer) {
        boolean fullStatus = buffer.readBoolean();
        long elapsedTime = buffer.readVarLong();
        long remainingItemCount = buffer.readVarLong();
        long startItemCount = buffer.readVarLong();
        int entryCount = buffer.readVarInt();

        ImmutableList.Builder<CraftingStatusEntry> entries = ImmutableList.builder();
        for (int i = 0; i < entryCount; i++) {
            entries.add(CraftingStatusEntry.read(buffer));
        }

        return new CraftingStatus(fullStatus, elapsedTime, remainingItemCount, startItemCount, entries.build());
    }

    public static CraftingStatus create(IncrementalUpdateHelper<IAEItemStack> changes,
            CraftingCPUCluster cpu) {

        boolean full = changes.isFullUpdate();

        ImmutableList.Builder<CraftingStatusEntry> newEntries = ImmutableList.builder();
        for (IAEItemStack stack : changes) {
            IAEItemStack stored = cpu.getItemStack(stack, CraftingItemList.STORAGE);
            IAEItemStack active = cpu.getItemStack(stack, CraftingItemList.ACTIVE);
            IAEItemStack pending = cpu.getItemStack(stack, CraftingItemList.PENDING);

            long storedCount = stored != null ? stored.getStackSize() : 0;
            long activeCount = active != null ? active.getStackSize() : 0;
            long pendingCount = pending != null ? pending.getStackSize() : 0;

            ItemStack item = stack.getDefinition();
            if (!full) {
                if (changes.getSerial(stack) != null) {
                    // The item was already sent to the client, so we can skip the item stack
                    item = ItemStack.EMPTY;
                }
            }

            newEntries.add(new CraftingStatusEntry(
                    changes.getOrAssignSerial(stack),
                    item,
                    storedCount,
                    activeCount,
                    pendingCount));
        }

        long elapsedTime = cpu.getElapsedTime();
        long remainingItems = cpu.getRemainingItemCount();
        long startItems = cpu.getStartItemCount();

        return new CraftingStatus(
                full,
                elapsedTime,
                remainingItems,
                startItems,
                newEntries.build());
    }

}
