package appeng.block.crafting;

public interface ICraftingUnitType {
    int getStorageKb();

    boolean isAccelerator();

    boolean isStatus();

    boolean isStorage();
}
