package appeng.server.testworld;

import java.util.function.BiConsumer;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.hooks.ticking.TickHandler;
import appeng.me.helpers.IGridConnectedBlockEntity;

public record PostGridInitAction(BoundingBox bb,
        BiConsumer<IGrid, IGridNode> consumer,
        boolean waitForActive) implements BlockPlacingBuildAction {

    @Override
    public void placeBlock(ServerLevel level, Player player, BlockPos pos, BlockPos minPos, BlockPos maxPos) {
        Runnable delayedAction = new Runnable() {
            private int attempts = 120;

            @Override
            public void run() {
                // Check if there's a grid node there
                var be = level.getBlockEntity(pos);
                IGridNode gridNode = null;
                if (be instanceof IGridConnectedBlockEntity host) {
                    gridNode = host.getMainNode().getNode();
                } else if (be instanceof CableBusBlockEntity cableBus) {
                    var centerPart = cableBus.getCableBus().getPart(null);
                    if (centerPart != null) {
                        gridNode = centerPart.getGridNode();
                    } else {
                        return; // Stop -> not eligible
                    }
                } else {
                    return; // Stop -> not eligible
                }

                if (gridNode == null || waitForActive && !gridNode.isActive()) {
                    if (--attempts > 0) {
                        TickHandler.instance().addCallable(level, this);
                    } else {
                        throw new IllegalStateException("Couldn't access grid node @ " + pos);
                    }
                } else {
                    consumer.accept(gridNode.getGrid(), gridNode);
                }
            }
        };
        TickHandler.instance().addCallable(level, delayedAction);
    }

    @Override
    public BoundingBox getBoundingBox() {
        return bb;
    }
}
