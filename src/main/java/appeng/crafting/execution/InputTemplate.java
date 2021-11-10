package appeng.crafting.execution;

import appeng.api.crafting.IPatternDetails;
import appeng.api.storage.data.AEKey;

/**
 * Represents a single "unit" of input for a slot in a crafting/processing pattern. How many units of input are required
 * for a pattern is returned by {@link IPatternDetails.IInput#getMultiplier()}.
 */
public record InputTemplate(AEKey key, long amount) {
}
