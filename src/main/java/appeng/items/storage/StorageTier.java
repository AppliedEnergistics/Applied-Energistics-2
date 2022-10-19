package appeng.items.storage;

import java.util.function.Supplier;

import net.minecraft.core.Registry;
import net.minecraft.world.item.Item;

import appeng.api.ids.AEItemIds;

public record StorageTier(int index, String namePrefix, int bytes, double idleDrain, Supplier<Item> componentSupplier) {
    public static final StorageTier SIZE_1K = new StorageTier(1, "1k", 1024, 0.5,
            () -> Registry.ITEM.get(AEItemIds.CELL_COMPONENT_1K));
    public static final StorageTier SIZE_4K = new StorageTier(2, "4k", 4096, 1.0,
            () -> Registry.ITEM.get(AEItemIds.CELL_COMPONENT_4K));
    public static final StorageTier SIZE_16K = new StorageTier(3, "16k", 16384, 1.5,
            () -> Registry.ITEM.get(AEItemIds.CELL_COMPONENT_16K));
    public static final StorageTier SIZE_64K = new StorageTier(4, "64k", 65536, 2.0,
            () -> Registry.ITEM.get(AEItemIds.CELL_COMPONENT_64K));
    public static final StorageTier SIZE_256K = new StorageTier(5, "256k", 262144, 2.5,
            () -> Registry.ITEM.get(AEItemIds.CELL_COMPONENT_256K));
}
