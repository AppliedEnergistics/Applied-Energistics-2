package appeng.server.testworld;

import java.util.List;
import java.util.function.BiConsumer;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;

public record PostGridInitAction(List<BlockPos> positions,
        BiConsumer<IGrid, IGridNode> consumer,
        boolean waitForActive) implements BuildAction {

    @Override
    public void build(ServerLevel level, Player player, BlockPos origin) {
        var absolutePositions = positions.stream().map(p -> p.offset(origin)).toList();

        GridInitHelper.doAfterGridInit(level, absolutePositions, waitForActive, consumer);
    }

    @Override
    public BoundingBox getBoundingBox() {
        return new BoundingBox(positions.getFirst());
    }
}
