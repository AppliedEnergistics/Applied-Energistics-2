package appeng.tile;

/**
 * Implement this on your block entity subclass of {@link AEBaseTileEntity} to handle tick events for both server and
 * client in the same way.
 */
public interface CommonTickingBlockEntity extends ServerTickingBlockEntity, ClientTickingBlockEntity {

    default void serverTick() {
        commonTick();
    }

    default void clientTick() {
        commonTick();
    }

    void commonTick();

}
