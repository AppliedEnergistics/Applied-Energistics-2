package appeng.api.networking.crafting;

import appeng.api.stacks.GenericStack;

public record CraftingJobStatus(GenericStack crafting, long totalItems, long progress, long elapsedTimeNanos,
        boolean suspended) {
    /**
     * Retained for binary compatibility with addons compiled against the previous record signature.
     */
    public CraftingJobStatus(GenericStack crafting, long totalItems, long progress, long elapsedTimeNanos) {
        this(crafting, totalItems, progress, elapsedTimeNanos, false);
    }
}
