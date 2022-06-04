package appeng.api.networking.crafting;

import appeng.api.stacks.GenericStack;

public record CraftingJobStatus(GenericStack crafting, long totalItems, long progress, long elapsedTimeNanos) {
}
