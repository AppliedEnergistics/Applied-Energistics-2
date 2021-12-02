package appeng.parts.automation;

import appeng.api.config.Actionable;
import appeng.api.storage.data.AEKey;

/**
 * Strategy for importing or exporting stacks from adjacent blocks into the grid. Used by the import/export bus.
 */
interface StackExportStrategy {
    long push(StackTransferContext context, AEKey what, long maxAmount, Actionable mode);
}
