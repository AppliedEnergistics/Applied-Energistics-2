package appeng.block.crafting;

import net.minecraft.world.item.Item;

import appeng.core.definitions.AEBlocks;

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
    public int getStorageBytes() {
        return 1024 * this.storageKb;
    }

    @Override
    public int getAcceleratorThreads() {
        return this == ACCELERATOR ? 1 : 0;
    }

    @Override
    public Item getItemFromType() {
        var definition = switch (this) {
            case UNIT -> AEBlocks.CRAFTING_UNIT;
            case ACCELERATOR -> AEBlocks.CRAFTING_ACCELERATOR;
            case STORAGE_1K -> AEBlocks.CRAFTING_STORAGE_1K;
            case STORAGE_4K -> AEBlocks.CRAFTING_STORAGE_4K;
            case STORAGE_16K -> AEBlocks.CRAFTING_STORAGE_16K;
            case STORAGE_64K -> AEBlocks.CRAFTING_STORAGE_64K;
            case STORAGE_256K -> AEBlocks.CRAFTING_STORAGE_256K;
            case MONITOR -> AEBlocks.CRAFTING_MONITOR;
        };
        return definition.asItem();
    }
}
