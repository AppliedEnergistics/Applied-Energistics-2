package appeng.server.testworld;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

public interface BlockPlacingBuildAction extends BuildAction {
    @Override
    default void build(ServerLevel level, Player player, BlockPos origin) {
        var actualBox = getBoundingBox().moved(origin.getX(), origin.getY(), origin.getZ());
        var minPos = new BlockPos(actualBox.minX(), actualBox.minY(), actualBox.minZ());
        var maxPos = new BlockPos(actualBox.maxX(), actualBox.maxY(), actualBox.maxZ());
        BlockPos.betweenClosedStream(actualBox).forEach(pos -> placeBlock(level, player, pos, minPos, maxPos));
    }

    void placeBlock(ServerLevel level, Player player, BlockPos pos, BlockPos minPos, BlockPos maxPos);
}
