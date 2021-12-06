package appeng.util;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

/**
 * Platform-independent wrapper for repeated API lookup / capability queries with the same parameters.
 */
public class AEApiCache<A> {
    private final BlockApiCache<A, Direction> cache;
    private final Direction sideFrom;

    public AEApiCache(BlockApiLookup<A, Direction> lookup, ServerLevel level, BlockPos pos, Direction sideFrom) {
        this.cache = BlockApiCache.create(lookup, level, pos);
        this.sideFrom = sideFrom;
    }

    @Nullable
    public A find() {
        return cache.find(sideFrom);
    }
}
