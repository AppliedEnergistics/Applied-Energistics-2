package appeng.tile;

/**
 * Implement this on your block entity subclass of {@link AEBaseBlockEntity} to receive server-side calls each tick.
 */
public interface ServerTickingBlockEntity {

    void serverTick();

}
