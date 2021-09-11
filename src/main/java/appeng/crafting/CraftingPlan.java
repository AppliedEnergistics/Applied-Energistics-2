package appeng.crafting;

import java.util.Map;

import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.IPatternDetails;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.MixedItemList;

public record CraftingPlan(IAEStack finalOutput, long bytes, boolean simulation, boolean multiplePaths,
                           MixedItemList usedItems,
                           MixedItemList emittedItems,
                           MixedItemList missingItems,
                           Map<IPatternDetails, Long> patternTimes) implements ICraftingPlan {
}
