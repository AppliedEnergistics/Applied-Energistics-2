package appeng.parts;

import javax.annotation.Nullable;

import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.capabilities.Capability;

import appeng.util.BlockApiCache;
import appeng.util.Platform;

/**
 * Utility class to cache an API that is adjacent to a part.
 */
public class PartAdjacentApi<C> {
    private final AEBasePart part;
    private final Capability<C> apiLookup;
    private BlockApiCache<C> apiCache;

    public PartAdjacentApi(AEBasePart part, Capability<C> apiLookup) {
        this.apiLookup = apiLookup;
        this.part = part;
    }

    @Nullable
    public C find() {
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
