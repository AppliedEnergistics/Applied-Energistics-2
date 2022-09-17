package appeng.api.networking.crafting;

import appeng.api.stacks.GenericStack;

/**
 * Gives a reason for why submitting the crafting job failed.
 */
public enum CraftingSubmitErrorCode {
    /**
     * Trying to submit a plan that is incomplete. Plans that return true for {@link ICraftingPlan#simulation()} are
     * incomplete.
     */
    INCOMPLETE_PLAN,
    /**
     * Couldn't find any CPUs to execute this job.
     */
    NO_CPU_FOUND,
    /**
     * None of the available CPUs are suitable to execute this job. {@link ICraftingSubmitResult#errorDetail()} contains
     * an instance of {@link UnsuitableCpus} giving details as to why available CPUs are unsuitable.
     */
    NO_SUITABLE_CPU_FOUND,
    /**
     * The selected crafting CPU is already working on something else.
     */
    CPU_BUSY,
    /**
     * The CPU is currently offline (no power or not enough channels).
     */
    CPU_OFFLINE,
    /**
     * The CPU is too small to process the job.
     */
    CPU_TOO_SMALL,
    /**
     * Could not obtain one of the ingredients needed for the job. {@link ICraftingSubmitResult#errorDetail()} is a
     * {@link GenericStack} explaining what is missing.
     */
    MISSING_INGREDIENT
}
