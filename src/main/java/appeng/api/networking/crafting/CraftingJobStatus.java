package appeng.api.networking.crafting;

import appeng.api.stacks.GenericStack;

public record CraftingJobStatus(GenericStack crafting, long progress, long elapsedTimeNanos) {
}
