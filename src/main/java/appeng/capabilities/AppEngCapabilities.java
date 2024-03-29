package appeng.capabilities;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;

import appeng.api.AECapabilities;
import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.implementations.blockentities.ICraftingMachine;
import appeng.api.implementations.blockentities.ICrankable;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.storage.MEStorage;

/**
 * Utility class that holds the capabilities provided by AE2.
 * 
 * @deprecated Use {@link AECapabilities} instead
 */
@Deprecated(forRemoval = true, since = "1.20.4")
public final class AppEngCapabilities {
    private AppEngCapabilities() {
    }

    public static BlockCapability<MEStorage, @Nullable Direction> ME_STORAGE = AECapabilities.ME_STORAGE;

    public static BlockCapability<ICraftingMachine, @Nullable Direction> CRAFTING_MACHINE = AECapabilities.CRAFTING_MACHINE;

    public static BlockCapability<GenericInternalInventory, @Nullable Direction> GENERIC_INTERNAL_INV = AECapabilities.GENERIC_INTERNAL_INV;

    public static BlockCapability<IInWorldGridNodeHost, @Nullable Direction> IN_WORLD_GRID_NODE_HOST = AECapabilities.IN_WORLD_GRID_NODE_HOST;

    public static BlockCapability<ICrankable, @Nullable Direction> CRANKABLE = AECapabilities.CRANKABLE;
}
