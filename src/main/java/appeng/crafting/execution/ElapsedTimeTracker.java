package appeng.crafting.execution;

import net.minecraft.nbt.CompoundTag;

public class ElapsedTimeTracker {
    private static final String NBT_ELAPSED_TIME = "elapsedTime";
    private static final String NBT_START_ITEM_COUNT = "startItemCount";
    private static final String NBT_REMAINING_ITEM_COUNT = "remainingItemCount";

    private long lastTime = System.nanoTime();
    private long elapsedTime = 0;
    private final long startItemCount;
    private long remainingItemCount;

    public ElapsedTimeTracker(long startItemCount) {
        this.startItemCount = startItemCount;
        this.remainingItemCount = startItemCount;
    }

    public ElapsedTimeTracker(CompoundTag data) {
        this.elapsedTime = data.getLong(NBT_ELAPSED_TIME);
        this.startItemCount = data.getLong(NBT_START_ITEM_COUNT);
        this.remainingItemCount = data.getLong(NBT_REMAINING_ITEM_COUNT);
    }

    public CompoundTag writeToNBT() {
        CompoundTag data = new CompoundTag();
        data.putLong(NBT_ELAPSED_TIME, elapsedTime);
        data.putLong(NBT_START_ITEM_COUNT, startItemCount);
        data.putLong(NBT_REMAINING_ITEM_COUNT, remainingItemCount);
        return data;
    }

    void decrementItems(long itemDiff) {
        long currentTime = System.nanoTime();
        this.elapsedTime = this.elapsedTime + (currentTime - this.lastTime);
        this.lastTime = currentTime;
        this.remainingItemCount -= itemDiff;
    }

    public long getElapsedTime() {
        return this.elapsedTime;
    }

    public long getRemainingItemCount() {
        return this.remainingItemCount;
    }

    public long getStartItemCount() {
        return this.startItemCount;
    }
}
