package appeng.parts.automation;

/**
 * Strategy for importing or exporting stacks from adjacent blocks into the grid. Used by the import/export bus.
 */
interface StackImportStrategy {
    boolean transfer(StackTransferContext context);
}
