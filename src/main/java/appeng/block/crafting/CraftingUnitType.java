package appeng.block.crafting;

public enum CraftingUnitType implements ICraftingUnitType {
    UNIT(0),
    ACCELERATOR(0),
    STORAGE_1K(1),
    STORAGE_4K(4),
    STORAGE_16K(16),
    STORAGE_64K(64),
    STORAGE_256K(256),
    MONITOR(0);

    private final int storageKb;

    CraftingUnitType(int storageKb) {
        this.storageKb = storageKb;
    }

    @Override
    public int getStorageKb() {
        return 1024 * this.storageKb;
    }

    @Override
    public boolean isAccelerator() {
        return this == ACCELERATOR;
    }

    @Override
    public boolean isStatus() {
        return this == MONITOR;
    }

    @Override
    public boolean isStorage() {
        return this.storageKb > 0;
    }
}
