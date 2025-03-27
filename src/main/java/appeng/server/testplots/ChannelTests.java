package appeng.server.testplots;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import net.minecraft.core.BlockPos;

import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridConnectionVisitor;
import appeng.api.networking.IGridNode;
import appeng.core.definitions.AEBlocks;
import appeng.server.testworld.PlotBuilder;
import appeng.server.testworld.PlotTestHelper;

@TestPlotClass
public class ChannelTests {
    @TestPlot("channel_assignment_test")
    public static void channelAssignmentTest(PlotBuilder plot) {
        plot.block("[-1,1] 0 0", AEBlocks.CONTROLLER);
        plot.creativeEnergyCell("0 -1 0");
        plot.block("0 1 0", AEBlocks.ME_CHEST);
        plot.denseCable("0 0 [1,3]");
        plot.block("0 0 4", AEBlocks.PATTERN_PROVIDER);
        plot.cable("[1,2] 0 3");
        plot.block("3 [0,1] [3,4]", AEBlocks.CRAFTING_STORAGE_4K);
        plot.cable("[4,9] 0 3");
        plot.block("5 1 3", AEBlocks.INTERFACE);
        plot.block("5 -1 3", AEBlocks.INTERFACE);
        plot.block("5 0 2", AEBlocks.INTERFACE);
        plot.block("5 0 4", AEBlocks.INTERFACE);
        plot.block("7 -1 3", AEBlocks.INTERFACE);
        plot.block("7 0 2", AEBlocks.INTERFACE);
        plot.block("7 0 4", AEBlocks.INTERFACE);
        plot.block("10 0 3", AEBlocks.CRAFTING_STORAGE_256K);

        plot.test(helper -> {
            helper.startSequence()
                    .thenWaitUntil(() -> {
                        var grid = helper.getGrid(BlockPos.ZERO);
                        helper.check(!grid.getPathingService().isNetworkBooting(), "Network is still booting");
                    })
                    .thenExecute(() -> {
                        var checker = new ChannelChecker(plot, helper);

                        // No channels through controllers
                        checker.node("[-1,1] 0 0", 0);
                        checker.connection("-1 0 0", "0 0 0", 0);
                        checker.connection("0 0 0", "1 0 0", 0);

                        checker.leafNode("0 -1 0", 0);
                        checker.leafNode("0 1 0", 1);

                        checker.node("0 0 [1,3]", 9);
                        checker.connection("0 0 0", "0 0 1", 9);
                        checker.connection("0 0 1", "0 0 2", 9);
                        checker.connection("0 0 2", "0 0 3", 9);

                        checker.leafNode("0 0 4", 1);

                        checker.node("[1,2] 0 3", 8);
                        checker.connection("0 0 3", "1 0 3", 8);
                        checker.connection("1 0 3", "2 0 3", 8);
                        checker.connection("2 0 3", "3 0 3", 8);

                        // Multiblocks are a bit special: each node gets +1 channel
                        checker.node("3 0 3", 8);
                        checker.node("3 1 3", 1);
                        checker.node("3 [0,1] 4", 1);
                        checker.connection("3 0 [3,4]", "3 1 [3,4]", 0);
                        checker.connection("3 [0,1] 3", "3 [0,1] 4", 0);

                        checker.connection("3 0 3", "4 0 3", 7);
                        checker.node("4 0 3", 7);
                        checker.connection("4 0 3", "5 0 3", 7);
                        checker.node("5 0 3", 7);
                        checker.connection("5 0 3", "6 0 3", 3);
                        checker.node("6 0 3", 3);
                        checker.connection("6 0 3", "7 0 3", 3);
                        checker.node("7 0 3", 3);
                        checker.connection("7 0 3", "8 0 3", 0);
                        checker.node("8 0 3", 0);
                        checker.connection("8 0 3", "9 0 3", 0);
                        checker.node("9 0 3", 0);

                        checker.leafNode("5 1 3", 1);
                        checker.leafNode("5 -1 3", 1);
                        checker.leafNode("5 0 2", 1);
                        checker.leafNode("5 0 4", 1);
                        checker.leafNode("7 -1 3", 1);
                        checker.leafNode("7 0 2", 1);
                        checker.leafNode("7 0 4", 1);

                        checker.leafNode("10 0 3", 0);

                        checker.ensureEverythingWasChecked();
                    })
                    .thenSucceed();
        });
    }

    private static class ChannelChecker {
        private final PlotBuilder plot;
        private final PlotTestHelper helper;
        private final Set<IGridNode> nodes = new HashSet<>();
        private final Set<IGridConnection> connections = new HashSet<>();

        private ChannelChecker(PlotBuilder plot, PlotTestHelper helper) {
            this.plot = plot;
            this.helper = helper;
            helper.getGrid(BlockPos.ZERO).getPivot().beginVisit(new IGridConnectionVisitor() {
                @Override
                public void visitConnection(IGridConnection n) {
                    connections.add(n);
                }

                @Override
                public boolean visitNode(IGridNode n) {
                    nodes.add(n);
                    return true;
                }
            });
        }

        private void forEachInBb(String bb, Consumer<BlockPos> action) {
            BlockPos.betweenClosedStream(plot.bb(bb)).forEach(action);
        }

        private void checkNode(BlockPos pos, IGridNode node, int expectedChannelCount) {
            if (nodes.contains(node)) {
                if (node.getUsedChannels() != expectedChannelCount) {
                    throw helper.assertionException(pos,
                            "Node has wrong channel count. Expected %d. Got %d.".formatted(expectedChannelCount,
                                    node.getUsedChannels()));
                }
                nodes.remove(node);
            } else {
                throw helper.assertionException(pos, "Node is not in the grid or it was already checked");
            }
        }

        private void checkConnection(BlockPos pos, IGridConnection connection, int expectedChannelCount) {
            if (connections.contains(connection)) {
                if (connection.getUsedChannels() != expectedChannelCount) {
                    throw helper.assertionException(pos, "Connection has wrong channel count. Expected %d. Got %d."
                            .formatted(expectedChannelCount, connection.getUsedChannels()));
                }
                connections.remove(connection);
            } else {
                throw helper.assertionException(pos, "Connection is not in the grid or it was already checked");
            }
        }

        public void node(String bb, int expectedChannelCount) {
            forEachInBb(bb, pos -> {
                var node = helper.getGridNode(pos);
                checkNode(pos, node, expectedChannelCount);
            });
        }

        /**
         * Checks both the node and its only connection.
         */
        public void leafNode(String bb, int expectedChannelCount) {
            forEachInBb(bb, pos -> {
                var node = helper.getGridNode(pos);
                checkNode(pos, node, expectedChannelCount);

                helper.check(node.getConnections().size() == 1, "Node does not have exactly one connection", pos);
                checkConnection(pos, node.getConnections().getFirst(), expectedChannelCount);
            });
        }

        public void connection(String bb, String bb2, int expectedChannelCount) {
            AtomicBoolean foundAny = new AtomicBoolean();

            forEachInBb(bb, pos -> {
                var node = helper.getGridNode(pos);
                forEachInBb(bb2, pos2 -> {
                    var node2 = helper.getGridNode(pos2);
                    for (var connection : node.getConnections()) {
                        if (connection.getOtherSide(node) == node2) {
                            checkConnection(pos, connection, expectedChannelCount);
                            foundAny.setPlain(true);
                        }
                    }
                });
            });

            if (!foundAny.getPlain()) {
                throw helper
                        .assertionException("Connection spec " + bb + " and " + bb2 + " did not find any connections.");
            }
        }

        public void ensureEverythingWasChecked() {
            helper.check(nodes.isEmpty(), "Not all nodes were checked: " + nodes);
            helper.check(connections.isEmpty(), "Not all connections were checked: " + connections);
        }
    }
}
