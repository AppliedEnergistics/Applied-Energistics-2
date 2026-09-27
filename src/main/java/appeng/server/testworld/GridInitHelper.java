package appeng.server.testworld;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.hooks.ticking.TickHandler;
import appeng.me.helpers.IGridConnectedBlockEntity;

final class GridInitHelper {

    static void doAfterGridInit(ServerLevel level, List<BlockPos> positions, boolean waitForActive,
            BiConsumer<IGrid, IGridNode> consumer) {
        Runnable delayedAction = new Runnable() {
            private int attempts = 240;

            @Override
            public void run() {
                // Check if there's a grid node there
                List<IGridNode> gridNodes = new ArrayList<>();

                for (var position : positions) {
                    var be = level.getBlockEntity(position);
                    if (be instanceof IGridConnectedBlockEntity host) {
                        gridNodes.add(host.getMainNode().getNode());
                    } else if (be instanceof CableBusBlockEntity cableBus) {
                        var centerPart = cableBus.getCableBus().getPart(null);
                        if (centerPart != null) {
                            gridNodes.add(centerPart.getGridNode());
                        } else {
                            return; // Stop -> not eligible
                        }
                    } else {
                        return; // Stop -> not eligible
                    }
                }

                if (gridNodes.stream().anyMatch(Objects::isNull)
                        || waitForActive && !gridNodes.stream().allMatch(IGridNode::isActive)) {
                    if (--attempts > 0) {
                        TickHandler.instance().addCallable(level, this);
                    } else {
                        throw new IllegalStateException("Couldn't access grid nodes @ " + positions);
                    }
                } else {
                    consumer.accept(gridNodes.getFirst().getGrid(), gridNodes.getFirst());
                }
            }
        };
        TickHandler.instance().addCallable(level, delayedAction);
    }

    static void doAfterGridInit(ServerLevel level, List<BlockEntity> blockEntities, boolean waitForActive,
            Runnable callback) {
        Runnable delayedAction = new Runnable() {
            private int attempts = 120;

            @Override
            public void run() {
                var notInitialized = new ArrayList<BlockEntity>();
                var notActive = new ArrayList<BlockEntity>();

                for (var be : blockEntities) {
                    if (be.isRemoved()) {
                        return;
                    }

                    if (be instanceof IGridConnectedBlockEntity host) {
                        var mainNode = host.getMainNode();
                        if (mainNode == null) {
                            notInitialized.add(be);
                            notActive.add(be);
                            break;
                        } else if (!mainNode.isActive()) {
                            notActive.add(be);
                        }
                    } else if (be instanceof CableBusBlockEntity cableBus) {
                        var centerPart = cableBus.getCableBus().getPart(null);
                        if (centerPart != null) {
                            var mainNode = centerPart.getGridNode();
                            if (mainNode == null) {
                                notInitialized.add(be);
                                notActive.add(be);
                                break;
                            } else if (!mainNode.isActive()) {
                                notActive.add(be);
                            }
                        }
                    }
                }

                if (!notInitialized.isEmpty() || waitForActive && !notActive.isEmpty()) {
                    if (--attempts > 0) {
                        TickHandler.instance().addCallable(level, this);
                    } else {
                        throw new IllegalStateException("Couldn't wait for grid to initialize. Not initialized: "
                                + notInitialized + ". Not active: " + notActive);
                    }
                } else {
                    callback.run();
                }
            }
        };
        TickHandler.instance().addCallable(level, delayedAction);
    }
}
