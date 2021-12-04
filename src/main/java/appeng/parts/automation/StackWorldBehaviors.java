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
import appeng.api.stacks.AEKeyType;

public final class StackWorldBehaviors {
    private static final Map<AEKeyType, ImportStrategyFactory> importStrategies = new IdentityHashMap<>();
    private static final Map<AEKeyType, ExportStrategyFactory> exportStrategies = new IdentityHashMap<>();
    private static final Map<AEKeyType, ExternalStorageStrategyFactory> externalStorageStrategies = new IdentityHashMap<>();
    private static final Map<AEKeyType, PlacementStrategyFactory> placementStrategies = new IdentityHashMap<>();
    private static final Map<AEKeyType, PickupStrategyFactory> pickupStrategies = new IdentityHashMap<>();

    static {
        importStrategies.put(AEKeyType.items(), StorageImportStrategy::createItem);
        importStrategies.put(AEKeyType.fluids(), StorageImportStrategy::createFluid);
        exportStrategies.put(AEKeyType.items(), StorageExportStrategy::createItem);
        exportStrategies.put(AEKeyType.fluids(), StorageExportStrategy::createFluid);
        externalStorageStrategies.put(AEKeyType.items(), FabricExternalStorageStrategy::createItem);
        externalStorageStrategies.put(AEKeyType.fluids(), FabricExternalStorageStrategy::createFluid);
        placementStrategies.put(AEKeyType.fluids(), FluidPlacementStrategy::new);
        placementStrategies.put(AEKeyType.items(), ItemPlacementStrategy::new);
        pickupStrategies.put(AEKeyType.fluids(), FluidPickupStrategy::new);
        pickupStrategies.put(AEKeyType.items(), ItemPickupStrategy::new);
    }

    private StackWorldBehaviors() {
    }

    /**
     * {@return filter matching any key for which there is an import strategy}
     */
    public static AEKeyFilter hasImportStrategyFilter() {
        return what -> importStrategies.containsKey(what.getType());
    }

    /**
     * {@return filter matching any key for which there is an export strategy}
     */
    public static AEKeyFilter hasExportStrategyFilter() {
        return what -> exportStrategies.containsKey(what.getType());
    }

    /**
     * {@return filter matching any key for which there is an export strategy}
     */
    public static AEKeyFilter hasPlacementStrategy() {
        return what -> placementStrategies.containsKey(what.getType());
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

    public static Map<AEKeyType, ExternalStorageStrategy> createExternalStorageStrategies(ServerLevel level,
                                                                                          BlockPos fromPos, Direction fromSide) {
        var strategies = new IdentityHashMap<AEKeyType, ExternalStorageStrategy>(externalStorageStrategies.size());
        for (var entry : externalStorageStrategies.entrySet()) {
            strategies.put(entry.getKey(), entry.getValue().create(level, fromPos, fromSide));
        }
        return strategies;
    }

    public static PlacementStrategy createPlacementStrategies(ServerLevel level, BlockPos fromPos, Direction fromSide,
            BlockEntity host) {
        var strategies = new IdentityHashMap<AEKeyType, PlacementStrategy>(placementStrategies.size());
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
