package appeng.block.crafting;

import net.minecraft.world.item.Item;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.BlockDefinition;

public enum CraftingUnitType implements ICraftingUnitType {
    UNIT(0, AEBlocks.CRAFTING_UNIT),
    ACCELERATOR(0, AEBlocks.CRAFTING_ACCELERATOR),
    STORAGE_1K(1, AEBlocks.CRAFTING_STORAGE_1K),
    STORAGE_4K(4, AEBlocks.CRAFTING_STORAGE_4K),
    STORAGE_16K(16, AEBlocks.CRAFTING_STORAGE_16K),
    STORAGE_64K(64, AEBlocks.CRAFTING_STORAGE_64K),
    STORAGE_256K(256, AEBlocks.CRAFTING_STORAGE_256K),
    MONITOR(0, AEBlocks.CRAFTING_MONITOR);

    private final int storageKb;
    private final BlockDefinition<?> craftingBlock;

    CraftingUnitType(int storageKb, BlockDefinition<?> craftingBlock) {
        this.storageKb = storageKb;
        this.craftingBlock = craftingBlock;
    }

    @Override
    public int getStorageBytes() {
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
    public Item getItemFromType() {
        return this.craftingBlock.asItem();
    }
}
