package appeng.parts;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import org.jetbrains.annotations.Nullable;

import net.minecraft.server.level.ServerLevel;

import appeng.util.Platform;

/**
 * Utility class to cache an API that is adjacent to a part.
 */
public class PartAdjacentApi<T> {
    private final AEBasePart part;
    private final BlockCapability<T, Direction> capability;
    private BlockCapabilityCache<T, Direction> cache;

    public PartAdjacentApi(AEBasePart part, BlockCapability<T, Direction> capability) {
        this.capability = capability;
        this.part = part;
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
            cache = BlockCapabilityCache.create(capability, serverLevel, targetPos, attachedSide.getOpposite());
        }

        return cache.getCapability();
    }
}
