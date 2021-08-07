package appeng.blockentity;

/**
 * Implement this on your block entity subclass of {@link AEBaseBlockEntity} to handle tick events for both server and
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
