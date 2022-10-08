package appeng.util;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.capabilities.Capability;

public class BlockApiCache<C> {
    private final ServerLevel level;
    private final BlockPos fromPos;
    private final Capability<C> capability;

    private BlockApiCache(Capability<C> capability, ServerLevel level, BlockPos fromPos) {
        this.capability = capability;
        this.level = level;
        this.fromPos = fromPos;
    }

    public static <C> BlockApiCache<C> create(Capability<C> capability, ServerLevel level, BlockPos fromPos) {
        return new BlockApiCache<>(capability, level, fromPos);
    }

    @Nullable
    public C find(Direction fromSide) {
        var be = level.getBlockEntity(fromPos);
        if (be != null) {
            return be.getCapability(capability, fromSide).orElse(null);
        }
        return null;
    }
}
