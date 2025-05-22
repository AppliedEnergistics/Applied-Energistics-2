package appeng.server.testplots;

import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import appeng.block.qnb.QuantumBaseBlock;
import appeng.blockentity.qnb.QuantumBridgeBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.server.testworld.PlotBuilder;
import appeng.server.testworld.PlotTestHelper;

public final class QnbTestPlots {
    private QnbTestPlots() {
    }

    @TestPlot("simple_qnb_link")
    public static void simpleQnbLink(PlotBuilder plot) {
        var qnbA = BlockPos.ZERO.above();
        var qnbB = qnbA.east(4);

        qnbRing(plot, qnbA);
        qnbRing(plot, qnbB);

        // Woosh!
        plot.test(helper -> {
            helper.startSequence()
                    // Wait until both QNBs have formed
                    .thenWaitUntil(() -> {
                        ringAround(qnbA, pos -> helper.assertBlockProperty(pos, QuantumBaseBlock.FORMED, true));
                        helper.assertBlockProperty(qnbA, QuantumBaseBlock.FORMED, true);
                        ringAround(qnbB, pos -> helper.assertBlockProperty(pos, QuantumBaseBlock.FORMED, true));
                        helper.assertBlockProperty(qnbB, QuantumBaseBlock.FORMED, true);
                    })
                    // Place singularities
                    .thenExecute(() -> {
                        var singularities = AEItems.QUANTUM_ENTANGLED_SINGULARITY.stack();
                        QuantumBridgeBlockEntity.assignFrequency(singularities);

                        var coreA = getCore(helper, qnbA);
                        helper.check(
                                coreA.getExposedInventoryForSide(Direction.SOUTH).addItems(singularities.copy())
                                        .isEmpty(),
                                "failed to add singularity",
                                qnbA);

                        var coreB = getCore(helper, qnbB);
                        helper.check(
                                coreB.getExposedInventoryForSide(Direction.SOUTH).addItems(singularities.copy())
                                        .isEmpty(),
                                "failed to add singularity",
                                qnbB);
                    })
                    // Wait until linked (-> same grid). This should now happen without power!
                    .thenWaitUntil(() -> {
                        var gridA = helper.getGrid(qnbA);
                        var gridB = helper.getGrid(qnbB);
                        if (gridA != gridB) {
                            helper.fail("not same grid", qnbA);
                            helper.fail("not same grid", qnbB);
                        }
                    })
                    // Wait until linked (-> same grid). This should now happen without power!
                    .thenWaitUntil(() -> {
                        var gridA = helper.getGrid(qnbA);
                        var gridB = helper.getGrid(qnbB);
                        if (gridA != gridB) {
                            helper.fail("not same grid", qnbA);
                            helper.fail("not same grid", qnbB);
                        }
                    })
                    // Remove singularity on one end
                    .thenExecute(() -> getCore(helper, qnbA).clearContent())
                    // Wait until unlinked
                    .thenWaitUntil(() -> {
                        var coreA = getCore(helper, qnbA);
                        helper.check(!coreA.hasQES(), "still has singularity", qnbA);

                        var gridA = helper.getGrid(qnbA);
                        var gridB = helper.getGrid(qnbB);
                        helper.check(gridA != gridB, "still same grid", qnbA);
                    })
                    .thenSucceed();
        });
    }

    private static QuantumBridgeBlockEntity getCore(PlotTestHelper helper, BlockPos pos) {
        var be = helper.getBlockEntity(pos);
        helper.check(be instanceof QuantumBridgeBlockEntity, "is not a QNB", pos);
        var qnb = (QuantumBridgeBlockEntity) be;
        helper.check(qnb.isFormed(), "not formed", pos);
        helper.check(!qnb.isCorner(), "is corner", pos);
        return qnb;
    }

    private static void qnbRing(PlotBuilder plot, BlockPos origin) {
        plot.block(origin, AEBlocks.QUANTUM_LINK);
        ringAround(origin, pos -> {
            plot.block(pos, AEBlocks.QUANTUM_RING);
        });
    }

    private static void ringAround(BlockPos origin, Consumer<BlockPos> consumer) {
        for (var x = -1; x <= 1; x++) {
            for (var y = -1; y <= 1; y++) {
                var pos = origin.offset(x, y, 0);
                if (x != 0 || y != 0) {
                    consumer.accept(pos);
                }
            }
        }
    }
}
