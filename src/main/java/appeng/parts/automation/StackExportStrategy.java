package appeng.parts.automation;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;

/**
 * Strategy for importing or exporting stacks from adjacent blocks into the grid. Used by the import/export bus.
 */
interface StackExportStrategy {
    /**
     * Transfer from the network inventory in {@param context} to the external inventory this strategy was created for.
     */
    long transfer(StackTransferContext context, AEKey what, long maxAmount, Actionable mode);

    /**
     * Tries inserting into the adjacent inventory and returns the amount that was pushed.
     */
    long push(AEKey what, long maxAmount, Actionable mode);
}
