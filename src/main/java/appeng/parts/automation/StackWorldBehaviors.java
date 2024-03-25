package appeng.parts.automation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.behaviors.ExternalStorageStrategy;
import appeng.api.behaviors.PickupStrategy;
import appeng.api.behaviors.PlacementStrategy;
import appeng.api.behaviors.StackExportStrategy;
import appeng.api.behaviors.StackImportStrategy;
import appeng.api.stacks.AEKeyType;
import appeng.api.storage.AEKeyFilter;
import appeng.util.CowMap;

public final class StackWorldBehaviors {
    private static final CowMap<AEKeyType, StackImportStrategy.Factory> importStrategies = CowMap.identityHashMap();
    private static final CowMap<AEKeyType, StackExportStrategy.Factory> exportStrategies = CowMap.identityHashMap();
    private static final CowMap<AEKeyType, ExternalStorageStrategy.Factory> externalStorageStrategies = CowMap
            .identityHashMap();
    private static final CowMap<AEKeyType, PlacementStrategy.Factory> placementStrategies = CowMap.identityHashMap();
    private static final CowMap<AEKeyType, PickupStrategy.Factory> pickupStrategies = CowMap.identityHashMap();

    static {
        registerImportStrategy(AEKeyType.items(), StorageImportStrategy::createItem);
        registerImportStrategy(AEKeyType.fluids(), StorageImportStrategy::createFluid);
        registerExportStrategy(AEKeyType.items(), StorageExportStrategy::createItem);
        registerExportStrategy(AEKeyType.fluids(), StorageExportStrategy::createFluid);
        registerExternalStorageStrategy(AEKeyType.items(), ForgeExternalStorageStrategy::createItem);
        registerExternalStorageStrategy(AEKeyType.fluids(), ForgeExternalStorageStrategy::createFluid);
        registerPlacementStrategy(AEKeyType.fluids(), FluidPlacementStrategy::new);
        registerPlacementStrategy(AEKeyType.items(), ItemPlacementStrategy::new);
        registerPickupStrategy(AEKeyType.fluids(), FluidPickupStrategy::new);
        registerPickupStrategy(AEKeyType.items(), ItemPickupStrategy::new);
    }

    private StackWorldBehaviors() {
    }

    public static void registerImportStrategy(AEKeyType type, StackImportStrategy.Factory factory) {
        importStrategies.putIfAbsent(type, factory);
    }

    public static void registerExportStrategy(AEKeyType type, StackExportStrategy.Factory factory) {
        exportStrategies.putIfAbsent(type, factory);
    }

    public static void registerExternalStorageStrategy(AEKeyType type, ExternalStorageStrategy.Factory factory) {
        externalStorageStrategies.putIfAbsent(type, factory);
    }

    public static void registerPlacementStrategy(AEKeyType type, PlacementStrategy.Factory factory) {
        placementStrategies.putIfAbsent(type, factory);
    }

    public static void registerPickupStrategy(AEKeyType type, PickupStrategy.Factory factory) {
        pickupStrategies.putIfAbsent(type, factory);
    }

    /**
     * {@return filter matching any key for which there is an import strategy}
     */
    public static AEKeyFilter hasImportStrategyFilter() {
        return what -> importStrategies.getMap().containsKey(what.getType());
    }

    public static Predicate<AEKeyType> hasImportStrategyTypeFilter() {
        return type -> importStrategies.getMap().containsKey(type);
    }

    /**
     * {@return set of key types that have an import strategy}
     */
    public static Set<AEKeyType> withImportStrategy() {
        return Collections.unmodifiableSet(importStrategies.getMap().keySet());
    }

    /**
     * {@return filter matching any key for which there is an export strategy}
     */
    public static AEKeyFilter hasExportStrategyFilter() {
        return what -> exportStrategies.getMap().containsKey(what.getType());
    }

    /**
     * {@return set of key types that have an export strategy}
     */
    public static Set<AEKeyType> withExportStrategy() {
        return Collections.unmodifiableSet(exportStrategies.getMap().keySet());
    }

    /**
     * {@return filter matching any key for which there is an export strategy}
     */
    public static AEKeyFilter hasPlacementStrategy() {
        return what -> placementStrategies.getMap().containsKey(what.getType());
    }

    /**
     * {@return set of key types that have a placement strategy}
     */
    public static Set<AEKeyType> withPlacementStrategy() {
        return Collections.unmodifiableSet(placementStrategies.getMap().keySet());
    }

    public static StackImportStrategy createImportFacade(ServerLevel level, BlockPos fromPos, Direction fromSide,
            Predicate<AEKeyType> forTypes) {
        var strategies = new ArrayList<StackImportStrategy>(importStrategies.getMap().size());
        for (var entry : importStrategies.getMap().entrySet()) {
            if (forTypes.test(entry.getKey())) {
                strategies.add(entry.getValue().create(level, fromPos, fromSide));
            }
        }
        return new StackImportFacade(strategies);
    }

    public static StackExportStrategy createExportFacade(ServerLevel level, BlockPos fromPos, Direction fromSide) {
        var strategies = new ArrayList<StackExportStrategy>(exportStrategies.getMap().size());
        for (var supplier : exportStrategies.getMap().values()) {
            strategies.add(supplier.create(level, fromPos, fromSide));
        }
        return new StackExportFacade(strategies);
    }

    public static Map<AEKeyType, ExternalStorageStrategy> createExternalStorageStrategies(ServerLevel level,
            BlockPos fromPos, Direction fromSide) {
        var strategies = new IdentityHashMap<AEKeyType, ExternalStorageStrategy>(
                externalStorageStrategies.getMap().size());
        for (var entry : externalStorageStrategies.getMap().entrySet()) {
            strategies.put(entry.getKey(), entry.getValue().create(level, fromPos, fromSide));
        }
        return strategies;
    }

    public static PlacementStrategy createPlacementStrategies(ServerLevel level, BlockPos fromPos, Direction fromSide,
            BlockEntity host, @Nullable UUID owningPlayerId) {
        var strategies = new IdentityHashMap<AEKeyType, PlacementStrategy>(placementStrategies.getMap().size());
        for (var entry : placementStrategies.getMap().entrySet()) {
            strategies.put(entry.getKey(), entry.getValue().create(level, fromPos, fromSide, host, owningPlayerId));
        }
        return new PlacementStrategyFacade(strategies);
    }

    public static List<PickupStrategy> createPickupStrategies(ServerLevel level, BlockPos fromPos, Direction fromSide,
            BlockEntity host, Map<Enchantment, Integer> enchantments, @Nullable UUID owningPlayerId) {
        return pickupStrategies.getMap().values()
                .stream()
                .map(f -> f.create(level, fromPos, fromSide, host, enchantments, owningPlayerId))
                .toList();
    }

}
