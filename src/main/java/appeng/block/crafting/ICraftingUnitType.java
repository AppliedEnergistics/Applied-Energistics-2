package appeng.block.crafting;

public interface ICraftingUnitType {
    int getStorageBytes();

    boolean isAccelerator();

    boolean isStatus();

    default boolean isStorage() {
        return this.getStorageBytes() > 0;
    }
}
