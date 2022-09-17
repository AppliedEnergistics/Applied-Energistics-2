package appeng.api.networking.crafting;

/**
 * Details about unsuitable crafting CPUs unavailable for a job. Detail for
 * {@link appeng.api.networking.crafting.CraftingSubmitErrorCode#NO_SUITABLE_CPU_FOUND}.
 */
public record UnsuitableCpus(int offline, int busy, int tooSmall, int excluded) {
}
