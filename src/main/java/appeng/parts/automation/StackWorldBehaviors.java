package appeng.parts.automation;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import appeng.api.storage.AEKeyFilter;
import appeng.api.storage.AEKeySpace;

public final class StackWorldBehaviors {
    private static final Map<AEKeySpace, ImportStrategyFactory> importStrategies = new IdentityHashMap<>();
    private static final Map<AEKeySpace, ExportStrategyFactory> exportStrategies = new IdentityHashMap<>();

    static {
        importStrategies.put(AEKeySpace.items(), StorageImportStrategy::createItem);
        importStrategies.put(AEKeySpace.fluids(), StorageImportStrategy::createFluid);
        exportStrategies.put(AEKeySpace.items(), StorageExportStrategy::createItem);
        exportStrategies.put(AEKeySpace.fluids(), StorageExportStrategy::createFluid);
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

    @FunctionalInterface
    interface ImportStrategyFactory {
        StackImportStrategy create(ServerLevel level, BlockPos fromPos, Direction fromSide);
    }

    @FunctionalInterface
    interface ExportStrategyFactory {
        StackExportStrategy create(ServerLevel level, BlockPos fromPos, Direction fromSide);
    }

}
