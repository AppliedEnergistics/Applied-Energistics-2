package appeng.parts;

import javax.annotation.Nullable;

import net.minecraft.server.level.ServerLevel;

import appeng.api.lookup.AEApiCache;
import appeng.api.lookup.AEApiLookup;
import appeng.util.Platform;

/**
 * Utility class to cache an API that is adjacent to a part.
 *
 * @param <A>
 */
public class PartAdjacentApi<A> {
    private final AEBasePart part;
    private final AEApiLookup<A> apiLookup;
    private AEApiCache<A> apiCache;

    public PartAdjacentApi(AEBasePart part, AEApiLookup<A> apiLookup) {
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
            apiCache = apiLookup.createCache(serverLevel, targetPos, attachedSide.getOpposite());
        }

        return apiCache.find();
    }
}
