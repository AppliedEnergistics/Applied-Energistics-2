package appeng.blockentity;

/**
 * Implement this on your block entity subclass of {@link AEBaseBlockEntity} to receive client-side calls each tick.
 */
public interface ClientTickingBlockEntity {

    void clientTick();

}
