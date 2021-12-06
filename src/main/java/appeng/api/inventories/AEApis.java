package appeng.api.inventories;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import team.reborn.energy.api.EnergyStorage;

import appeng.api.ids.AEConstants;
import appeng.api.implementations.blockentities.ICraftingMachine;
import appeng.api.storage.IStorageMonitorableAccessor;

/**
 * Stores AE's own api lookups / capabilities, and the platform item/fluid/energy transfer ones.
 */
public final class AEApis {
    public static final BlockApiLookup<IStorageMonitorableAccessor, Direction> STORAGE_ACCESSOR = BlockApiLookup.get(
            new ResourceLocation(AEConstants.MOD_ID, "storage"), IStorageMonitorableAccessor.class, Direction.class);

    public static final BlockApiLookup<ICraftingMachine, Direction> CRAFTING_MACHINE = BlockApiLookup.get(
            new ResourceLocation(AEConstants.MOD_ID, "icraftingmachine"), ICraftingMachine.class, Direction.class);

    public static final BlockApiLookup<Storage<ItemVariant>, Direction> ITEMS = ItemStorage.SIDED;
    public static final BlockApiLookup<Storage<FluidVariant>, Direction> FLUIDS = FluidStorage.SIDED;
    public static final BlockApiLookup<EnergyStorage, Direction> ENERGY = EnergyStorage.SIDED;

    private AEApis() {
    }
}
