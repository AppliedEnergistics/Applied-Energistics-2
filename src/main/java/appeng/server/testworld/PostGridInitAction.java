package appeng.server.testworld;

import java.util.function.BiConsumer;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;

public record PostGridInitAction(BoundingBox bb,
        BiConsumer<IGrid, IGridNode> consumer,
        boolean waitForActive) implements BlockPlacingBuildAction {

    @Override
    public void placeBlock(ServerLevel level, Player player, BlockPos pos, BlockPos minPos, BlockPos maxPos) {
        GridInitHelper.doAfterGridInit(level, pos, waitForActive, consumer);
    }

    @Override
    public BoundingBox getBoundingBox() {
        return bb;
    }
}
