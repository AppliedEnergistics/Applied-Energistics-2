package appeng.parts.automation;

import appeng.api.storage.AEKeyFilter;
import appeng.api.storage.AEKeySpace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Map;

public final class StackWorldBehaviors {
    private static final Map<AEKeySpace, ImportStrategyFactory> importStrategies = new IdentityHashMap<>();

    static {
        importStrategies.put(AEKeySpace.items(), StorageImportStrategy::createItem);
        importStrategies.put(AEKeySpace.fluids(), StorageImportStrategy::createFluid);
    }

    private StackWorldBehaviors() {
    }

    /**
     * {@return filter that matches any key for which there is in-world behavior}
     */
    public static AEKeyFilter supportedFilter() {
        return what -> importStrategies.containsKey(what.getChannel());
    }

    public static StackImportStrategy createImportFacade(ServerLevel level, BlockPos fromPos, Direction fromSide) {
        var strategies = new ArrayList<StackImportStrategy>(importStrategies.size());
        for (var supplier : importStrategies.values()) {
            strategies.add(supplier.create(level, fromPos, fromSide));
        }
        return new StackImportFacade(strategies);
    }

    @FunctionalInterface
    interface ImportStrategyFactory {
        StackImportStrategy create(ServerLevel level, BlockPos fromPos, Direction fromSide);
    }

}
