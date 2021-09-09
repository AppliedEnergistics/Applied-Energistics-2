package appeng.crafting;

import java.util.Map;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.IPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;

public record CraftingPlan(IAEItemStack finalOutput, long bytes, boolean simulation,
        IItemList<IAEItemStack> usedItems,
        IItemList<IAEItemStack> emittedItems,
        IItemList<IAEItemStack> missingItems,
        Map<IPatternDetails, Long> patternTimes) implements ICraftingPlan {
}
