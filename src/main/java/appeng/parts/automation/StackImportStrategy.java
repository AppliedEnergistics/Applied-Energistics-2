package appeng.parts.automation;

/**
 * Strategy for importing stacks from adjacent blocks into the grid. Used by the import bus.
 */
interface StackImportStrategy {
    boolean move(StackTransferContext context);
}
