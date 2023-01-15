package appeng.helpers.patternprovider;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import appeng.api.behaviors.ExternalStorageStrategy;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.storage.IStorageMonitorableAccessor;
import appeng.api.storage.MEStorage;
import appeng.me.storage.CompositeStorage;
import appeng.parts.automation.StackWorldBehaviors;

class PatternProviderTargetCache {
    private final BlockApiCache<IStorageMonitorableAccessor, Direction> cache;
    private final Direction direction;
    private final IActionSource src;
    private final Map<AEKeyType, ExternalStorageStrategy> strategies;

    PatternProviderTargetCache(ServerLevel l, BlockPos pos, Direction direction, IActionSource src) {
        this.cache = BlockApiCache.create(IStorageMonitorableAccessor.SIDED, l, pos);
        this.direction = direction;
        this.src = src;
        this.strategies = StackWorldBehaviors.createExternalStorageStrategies(l, pos, direction);
    }

    @Nullable
    PatternProviderTarget find() {
        // our capability first: allows any storage channel
        var accessor = cache.find(direction);
        if (accessor != null) {
            return wrapStorageMonitorable(accessor);
        }

        // otherwise fall back to the platform capability
        var externalStorages = new IdentityHashMap<AEKeyType, MEStorage>(2);
        for (var entry : strategies.entrySet()) {
            var wrapper = entry.getValue().createWrapper(false, () -> {
            });
            if (wrapper != null) {
                externalStorages.put(entry.getKey(), wrapper);
            }
        }

        if (externalStorages.size() > 0) {
            return wrapMeStorage(new CompositeStorage(externalStorages));
        }

        return null;
    }

    private PatternProviderTarget wrapStorageMonitorable(IStorageMonitorableAccessor accessor) {
        var storage = accessor.getInventory(src);
        if (storage == null) {
            return null;
        } else {
            return wrapMeStorage(storage);
        }
    }

    private PatternProviderTarget wrapMeStorage(MEStorage storage) {
        return new PatternProviderTarget() {
            @Override
            public long insert(AEKey what, long amount, Actionable type) {
                return storage.insert(what, amount, type, src);
            }

            @Override
            public boolean containsPatternInput(Set<AEKey> patternInputs) {
                for (var stack : storage.getAvailableStacks()) {
                    if (patternInputs.contains(stack.getKey().dropSecondary())) {
                        return true;
                    }
                }
                return false;
            }
        };
    }
}
