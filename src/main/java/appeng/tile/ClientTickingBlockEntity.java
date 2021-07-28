package appeng.tile;

/**
 * Implement this on your block entity subclass of {@link AEBaseTileEntity} to receive client-side calls each tick.
 */
public interface ClientTickingBlockEntity {

    void clientTick();

}
