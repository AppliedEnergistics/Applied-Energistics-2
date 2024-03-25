package appeng.parts;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;

import appeng.api.parts.IPartHost;
import appeng.util.Platform;

/**
 * Utility class to cache an API that is adjacent to a part.
 */
public class PartAdjacentApi<T> {
    private final AEBasePart part;
    private final BlockCapability<T, Direction> capability;
    private final Runnable invalidationListener;
    private BlockCapabilityCache<T, Direction> cache;

    public PartAdjacentApi(AEBasePart part, BlockCapability<T, Direction> capability) {
        this(part, capability, () -> {
        });
    }

    public PartAdjacentApi(AEBasePart part, BlockCapability<T, Direction> capability, Runnable invalidationListener) {
        this.capability = capability;
        this.part = part;
        this.invalidationListener = invalidationListener;
    }

    @Nullable
    public T find() {
        if (!(part.getLevel() instanceof ServerLevel serverLevel)) {
            return null;
        }

        var host = part.getHost().getBlockEntity();
        var attachedSide = part.getSide();
        var targetPos = host.getBlockPos().relative(attachedSide);

        if (!Platform.areBlockEntitiesTicking(serverLevel, targetPos)) {
            return null;
        }

        if (cache == null) {
            cache = BlockCapabilityCache.create(
                    capability,
                    serverLevel,
                    targetPos,
                    attachedSide.getOpposite(),
                    () -> isPartValid(part),
                    invalidationListener);
        }

        return cache.getCapability();
    }

    public static boolean isPartValid(AEBasePart part) {
        var be = part.getBlockEntity();
        return be instanceof IPartHost host && host.getPart(part.getSide()) == part && !be.isRemoved();
    }
}
