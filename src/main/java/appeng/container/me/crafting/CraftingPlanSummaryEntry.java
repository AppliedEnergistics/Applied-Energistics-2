package appeng.container.me.crafting;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

/**
 * Describes an entry in the crafting plan which describes how many items of one type are missing, already stored in
 * the network, or have to be crafted.
 */
public class CraftingPlanSummaryEntry {
    private final ItemStack item;
    private final long missingAmount;
    private final long storedAmount;
    private final long craftAmount;

    public CraftingPlanSummaryEntry(ItemStack item, long missingAmount, long storedAmount, long craftAmount) {
        this.item = item;
        this.missingAmount = missingAmount;
        this.storedAmount = storedAmount;
        this.craftAmount = craftAmount;
    }

    public ItemStack getItem() {
        return item;
    }

    public long getMissingAmount() {
        return missingAmount;
    }

    public long getStoredAmount() {
        return storedAmount;
    }

    public long getCraftAmount() {
        return craftAmount;
    }

    public void write(PacketBuffer buffer) {
        buffer.writeItemStack(item, true);
        buffer.writeVarLong(missingAmount);
        buffer.writeVarLong(storedAmount);
        buffer.writeVarLong(craftAmount);
    }

    public static CraftingPlanSummaryEntry read(PacketBuffer buffer) {
        ItemStack item = buffer.readItemStack();
        long missingAmount = buffer.readVarLong();
        long storedAmount = buffer.readVarLong();
        long craftAmount = buffer.readVarLong();
        return new CraftingPlanSummaryEntry(item, missingAmount, storedAmount, craftAmount);
    }

}
