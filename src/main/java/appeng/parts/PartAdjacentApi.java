package appeng.parts;

import javax.annotation.Nullable;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import appeng.util.Platform;

/**
 * Utility class to cache an API that is adjacent to a part.
 *
 * @param <A>
 */
public class PartAdjacentApi<A> {
    private final AEBasePart part;
    private final BlockApiLookup<A, Direction> apiLookup;
    private BlockApiCache<A, Direction> apiCache;

    public PartAdjacentApi(AEBasePart part, BlockApiLookup<A, Direction> apiLookup) {
        this.apiLookup = apiLookup;
        this.part = part;
    }

    @Nullable
    public A find() {
        if (!(part.getLevel() instanceof ServerLevel serverLevel)) {
            return null;
        }

        var host = part.getHost().getBlockEntity();
        var attachedSide = part.getSide();
        var targetPos = host.getBlockPos().relative(attachedSide);

        if (!Platform.areBlockEntitiesTicking(serverLevel, targetPos)) {
            return null;
        }

        if (apiCache == null) {
            apiCache = BlockApiCache.create(apiLookup, serverLevel, targetPos);
        }

        return apiCache.find(attachedSide.getOpposite());
    }
}
