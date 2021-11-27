package appeng.parts.automation;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.data.AEKey;

import javax.annotation.Nullable;

/**
 * Strategy for importing stacks from adjacent blocks into the grid.
 * Used by the import bus.
 */
interface StackImportStrategy {
    boolean move(StackTransferContext context);
}
