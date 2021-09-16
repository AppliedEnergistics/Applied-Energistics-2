package appeng.api.networking.crafting;

import java.util.Map;

import appeng.api.crafting.IPatternDetails;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.MixedItemList;

/**
 * Result of a {@linkplain ICraftingService#beginCraftingJob crafting job calculation}. Do not edit any of the
 * map/lists, they are exposed directly!
 */
public interface ICraftingPlan {
    /**
     * Final output of the job.
     */
    IAEStack finalOutput();

    /**
     * Total bytes used by the job.
     */
    long bytes();

    /**
     * True if some things were missing and this is just a simulation.
     */
    boolean simulation();

    /**
     * True there were multiple paths in the crafting tree, i.e. at least one item had multiple patterns that could
     * produce it.
     */
    boolean multiplePaths();

    /**
     * List of items that were used. (They would need to be extracted to start the job).
     */
    MixedItemList usedItems();

    /**
     * List of items that need to be emitted for this job.
     */
    MixedItemList emittedItems();

    /**
     * List of missing items if this is a simulation.
     */
    MixedItemList missingItems();

    /**
     * Map of each pattern to the number of times it needs to be crafted. Can be used to retrieve the crafted items:
     * outputs * times.
     */
    Map<IPatternDetails, Long> patternTimes();
}
