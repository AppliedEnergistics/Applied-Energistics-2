package appeng.api.lookup;

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
 * Holder for all API lookups / capabilities exposed or used by AE2.
 */
public final class AEApis {
    // AE2's
    public static final AEApiLookup<ICraftingMachine> CRAFTING_MACHINE = new AEApiLookup<>(BlockApiLookup.get(
            new ResourceLocation(AEConstants.MOD_ID, "icraftingmachine"), ICraftingMachine.class, Direction.class));
    public static final AEApiLookup<IStorageMonitorableAccessor> STORAGE_MONITORABLE_ACCESSOR = new AEApiLookup<>(
            BlockApiLookup.get(new ResourceLocation(AEConstants.MOD_ID, "storage"), IStorageMonitorableAccessor.class,
                    Direction.class));
    // platform's
    public static final AEApiLookup<Storage<ItemVariant>> ITEMS = new AEApiLookup<>(ItemStorage.SIDED);
    public static final AEApiLookup<Storage<FluidVariant>> FLUIDS = new AEApiLookup<>(FluidStorage.SIDED);
    public static final AEApiLookup<EnergyStorage> ENERGY = new AEApiLookup<>(EnergyStorage.SIDED);

    private AEApis() {
    }
}
