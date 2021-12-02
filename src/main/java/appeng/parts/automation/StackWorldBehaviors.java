package appeng.parts.automation;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.storage.AEKeyFilter;
import appeng.api.storage.AEKeySpace;

public final class StackWorldBehaviors {
    private static final Map<AEKeySpace, ImportStrategyFactory> importStrategies = new IdentityHashMap<>();
    private static final Map<AEKeySpace, ExportStrategyFactory> exportStrategies = new IdentityHashMap<>();
    private static final Map<AEKeySpace, ExternalStorageStrategyFactory> externalStorageStrategies = new IdentityHashMap<>();
    private static final Map<AEKeySpace, PlacementStrategyFactory> placementStrategies = new IdentityHashMap<>();
    private static final Map<AEKeySpace, PickupStrategyFactory> pickupStrategies = new IdentityHashMap<>();

    static {
        importStrategies.put(AEKeySpace.items(), StorageImportStrategy::createItem);
        importStrategies.put(AEKeySpace.fluids(), StorageImportStrategy::createFluid);
        exportStrategies.put(AEKeySpace.items(), StorageExportStrategy::createItem);
        exportStrategies.put(AEKeySpace.fluids(), StorageExportStrategy::createFluid);
        externalStorageStrategies.put(AEKeySpace.items(), FabricExternalStorageStrategy::createItem);
        externalStorageStrategies.put(AEKeySpace.fluids(), FabricExternalStorageStrategy::createFluid);
        placementStrategies.put(AEKeySpace.fluids(), FluidPlacementStrategy::new);
        placementStrategies.put(AEKeySpace.items(), ItemPlacementStrategy::new);
        pickupStrategies.put(AEKeySpace.fluids(), FluidPickupStrategy::new);
        pickupStrategies.put(AEKeySpace.items(), ItemPickupStrategy::new);
    }

    private StackWorldBehaviors() {
    }

    /**
     * {@return filter matching any key for which there is an import strategy}
     */
    public static AEKeyFilter hasImportStrategyFilter() {
        return what -> importStrategies.containsKey(what.getChannel());
    }

    /**
     * {@return filter matching any key for which there is an export strategy}
     */
    public static AEKeyFilter hasExportStrategyFilter() {
        return what -> exportStrategies.containsKey(what.getChannel());
    }

    /**
     * {@return filter matching any key for which there is an export strategy}
     */
    public static AEKeyFilter hasPlacementStrategy() {
        return what -> placementStrategies.containsKey(what.getChannel());
    }

    public static StackImportStrategy createImportFacade(ServerLevel level, BlockPos fromPos, Direction fromSide) {
        var strategies = new ArrayList<StackImportStrategy>(importStrategies.size());
        for (var supplier : importStrategies.values()) {
            strategies.add(supplier.create(level, fromPos, fromSide));
        }
        return new StackImportFacade(strategies);
    }

    public static StackExportStrategy createExportFacade(ServerLevel level, BlockPos fromPos, Direction fromSide) {
        var strategies = new ArrayList<StackExportStrategy>(exportStrategies.size());
        for (var supplier : exportStrategies.values()) {
            strategies.add(supplier.create(level, fromPos, fromSide));
        }
        return new StackExportFacade(strategies);
    }

    public static Map<AEKeySpace, ExternalStorageStrategy> createExternalStorageStrategies(ServerLevel level,
            BlockPos fromPos, Direction fromSide) {
        var strategies = new IdentityHashMap<AEKeySpace, ExternalStorageStrategy>(externalStorageStrategies.size());
        for (var entry : externalStorageStrategies.entrySet()) {
            strategies.put(entry.getKey(), entry.getValue().create(level, fromPos, fromSide));
        }
        return strategies;
    }

    public static PlacementStrategy createPlacementStrategies(ServerLevel level, BlockPos fromPos, Direction fromSide,
            BlockEntity host) {
        var strategies = new IdentityHashMap<AEKeySpace, PlacementStrategy>(placementStrategies.size());
        for (var entry : placementStrategies.entrySet()) {
            strategies.put(entry.getKey(), entry.getValue().create(level, fromPos, fromSide, host));
        }
        return new PlacementStrategyFacade(strategies);
    }

    public static List<PickupStrategy> createPickupStrategies(ServerLevel level, BlockPos fromPos, Direction fromSide,
            BlockEntity host, boolean allowSilkTouch) {
        return pickupStrategies.values()
                .stream()
                .map(f -> f.create(level, fromPos, fromSide, host, allowSilkTouch))
                .toList();
    }

    @FunctionalInterface
    interface ImportStrategyFactory {
        StackImportStrategy create(ServerLevel level, BlockPos fromPos, Direction fromSide);
    }

    @FunctionalInterface
    interface ExportStrategyFactory {
        StackExportStrategy create(ServerLevel level, BlockPos fromPos, Direction fromSide);
    }

    @FunctionalInterface
    interface ExternalStorageStrategyFactory {
        ExternalStorageStrategy create(ServerLevel level, BlockPos fromPos, Direction fromSide);
    }

    @FunctionalInterface
    interface PlacementStrategyFactory {
        PlacementStrategy create(ServerLevel level, BlockPos fromPos, Direction fromSide, BlockEntity host);
    }

    @FunctionalInterface
    interface PickupStrategyFactory {
        PickupStrategy create(ServerLevel level, BlockPos fromPos, Direction fromSide, BlockEntity host,
                boolean allowSilkTouch);
    }

}
