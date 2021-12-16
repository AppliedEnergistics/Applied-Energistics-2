package appeng.server.testworld;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public record PlaceBlockState(BoundingBox bb, BlockState what) implements BlockPlacingBuildAction {
    @Override
    public BoundingBox getBoundingBox() {
        return bb;
    }

    @Override
    public void placeBlock(ServerLevel level, Player player, BlockPos pos, BlockPos minPos, BlockPos maxPos) {
        level.setBlock(pos, what, Block.UPDATE_ALL);
    }
}
