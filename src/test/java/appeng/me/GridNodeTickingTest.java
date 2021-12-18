package appeng.me;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import appeng.api.exceptions.FailedConnectionException;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.me.service.TickManagerService;

public class GridNodeTickingTest extends AbstractGridNodeTest {

    /**
     * A device that would normally tick in 10 ticks will tick on the next tick if it is alerted.
     */
    @Test
    void testAlertDevice() {
        var timesSinceLastTick = new ArrayList<Integer>();
        var node = makeTickingNode(
                new TickingRequest(10, 10, true, true),
                (tickingNode, ticksSinceLastCall) -> {
                    timesSinceLastTick.add(ticksSinceLastCall);
                    return TickRateModulation.SAME;
                });

        // Since it's asleep, nothing happens
        runTick(node.getGrid(), 10);
        assertThat(timesSinceLastTick).isEmpty();
        assertNodeIsAsleep(node);

        // Alerting it will also wake it up
        node.getGrid().getTickManager().alertDevice(node);
        runTick(node.getGrid(), 1);
        assertThat(timesSinceLastTick).containsExactly(11);
        assertNodeIsAwake(node);

        // It will then continue to tick at its minimal rate
        runTick(node.getGrid(), 10);
        assertThat(timesSinceLastTick).containsExactly(11, 10);

        // But alerting it will then tick it on the next tick
        node.getGrid().getTickManager().alertDevice(node);
        runTick(node.getGrid(), 1);
        assertThat(timesSinceLastTick).containsExactly(11, 10, 1);
    }

    /**
     * Tests the sleeping behavior of nodes.
     */
    @Nested
    class SleepingTest {
        @Test
        void testNodesThatStartOutSleeping() {
            var timesSinceLastTick = new ArrayList<Integer>();
            var node = makeTickingNode(
                    new TickingRequest(1, 1, true, false),
                    (tickingNode, ticksSinceLastCall) -> {
                        timesSinceLastTick.add(ticksSinceLastCall);
                        return TickRateModulation.SAME;
                    });
            int timesTicked = 0;
            while (timesSinceLastTick.size() < 5 && timesTicked++ < 1000) {
                runTick(node.getGrid());
            }
            // should never have ticked
            assertThat(timesSinceLastTick).isEmpty();

            // wake it up
            node.getMyGrid().getTickManager().wakeDevice(node);
            timesTicked = 0;
            while (timesSinceLastTick.size() < 5 && timesTicked++ < 1000) {
                runTick(node.getGrid());
            }

            assertThat(timesSinceLastTick).containsExactly(1001, 1, 1, 1, 1);
        }

        @Test
        void testNodesPuttingThemselvesToSleep() {
            var timesSinceLastTick = new ArrayList<Integer>();
            var node = makeTickingNode(
                    new TickingRequest(1, 1, false, false),
                    (tickingNode, ticksSinceLastCall) -> {
                        timesSinceLastTick.add(ticksSinceLastCall);
                        return TickRateModulation.SLEEP;
                    });
            int timesTicked = 0;
            while (timesSinceLastTick.size() < 5 && timesTicked++ < 1000) {
                runTick(node.getGrid());
            }
            // should have ticked once
            assertThat(timesSinceLastTick).containsExactly(1);

            // wake it up
            node.getMyGrid().getTickManager().wakeDevice(node);
            timesTicked = 0;
            while (timesSinceLastTick.size() < 5 && timesTicked++ < 1000) {
                runTick(node.getGrid());
            }

            // should have ticked twice
            assertThat(timesSinceLastTick).containsExactly(1, 1000);
        }

        @Test
        void testNodeBeingPutToSleepExternally() {
            var timesSinceLastTick = new ArrayList<Integer>();
            var node = makeTickingNode(
                    new TickingRequest(5, 5, false, false),
                    (tickingNode, ticksSinceLastCall) -> {
                        timesSinceLastTick.add(ticksSinceLastCall);
                        return TickRateModulation.SAME;
                    });

            // Run ticks until the node ticked once
            int timesTicked = 0;
            while (timesSinceLastTick.size() < 1 && timesTicked++ < 1000) {
                runTick(node.getGrid());
            }
            assertThat(timesSinceLastTick).containsExactly(5); // should not have ticked again

            // Put the node to sleep
            node.getGrid().getTickManager().sleepDevice(node);

            // Run ticks, it should not tick again
            timesTicked = 0;
            while (timesSinceLastTick.size() < 2 && timesTicked++ < 1000) {
                runTick(node.getGrid());
            }

            assertThat(timesSinceLastTick).containsExactly(5); // should not have ticked again
        }

        @Test
        void testAwakeNodeThatIsNotAsleep() {
            var timesSinceLastTick = new ArrayList<Integer>();
            var node = makeTickingNode(
                    new TickingRequest(2, 2, false, false),
                    (tickingNode, ticksSinceLastCall) -> {
                        timesSinceLastTick.add(ticksSinceLastCall);
                        return TickRateModulation.SAME;
                    });

            // Run one tick, the node would tick next tick
            runTick(node.getGrid());
            assertThat(node.getGrid().getTickManager().wakeDevice(node)).isFalse();
            runTick(node.getGrid());

            // The node should have ticked on the second tick regardless of being woken up
            assertThat(timesSinceLastTick).containsExactly(2);
        }

        /**
         * Regression test that checks nodes calling tick manager functions for themselves while they are ticking
         * doesn't screw up the tick queue.
         */
        @ParameterizedTest
        @ValueSource(strings = { "wake", "alert", "sleep" })
        void testNodesCallingTickManagerFunctionsWhileTheyAreTicking(String operation)
                throws FailedConnectionException {
            var timesSinceLastTick = new ArrayList<Integer>();
            var node = makeTickingNode(new TickingRequest(1, 10, false, true), (tickingNode, ticksSinceLastCall) -> {
                timesSinceLastTick.add(ticksSinceLastCall);
                var result = switch (operation) {
                    case "wake" -> tickingNode.getGrid().getTickManager().wakeDevice(tickingNode);
                    case "alert" -> tickingNode.getGrid().getTickManager().alertDevice(tickingNode);
                    case "sleep" -> tickingNode.getGrid().getTickManager().sleepDevice(tickingNode);
                    default -> throw new IllegalArgumentException();
                };
                assertThat(result).isFalse();
                return TickRateModulation.IDLE;
            });
            // Set up a second node to ensure it does get ticked. In the regression, alertDevice would put
            // the other node at the front of the queue constantly, preventing this node from ticking
            var timesSinceLastTick2 = new ArrayList<Integer>();
            var secondNode = makeTickingNode(
                    new TickingRequest(6, 6, false, false),
                    (tickingNode, ticksSinceLastTick) -> {
                        timesSinceLastTick2.add(ticksSinceLastTick);
                        return TickRateModulation.SAME;
                    });
            GridConnection.create(node, secondNode, null);

            runTick(node.getGrid(), 25);
            assertThat(timesSinceLastTick).containsExactly(5, 10, 10);
            assertThat(timesSinceLastTick2).containsExactly(6, 6, 6, 6);
        }
    }

    /**
     * Tests the dynamic adjustment of tick rates via the return value of
     * {@link IGridTickable#tickingRequest(IGridNode, int)}.
     */
    @Nested
    class TickRateModulationTest {
        @Test
        void testSleep() {
            assertThat(runTicksWithModulation(TickRateModulation.SLEEP))
                    .containsExactly(5);
        }

        @Test
        void testIdle() {
            assertThat(runTicksWithModulation(TickRateModulation.IDLE))
                    .containsExactly(5, 10, 10, 10, 10);
        }

        @Test
        void testSlower() {
            assertThat(runTicksWithModulation(TickRateModulation.SLOWER))
                    .containsExactly(5, 6, 7, 8, 9);
        }

        @Test
        void testSame() {
            assertThat(runTicksWithModulation(TickRateModulation.SAME))
                    .containsExactly(5, 5, 5, 5, 5);
        }

        @Test
        void testFaster() {
            assertThat(runTicksWithModulation(TickRateModulation.FASTER))
                    .containsExactly(5, 3, 1, 1, 1);
        }

        @Test
        void testUrgent() {
            assertThat(runTicksWithModulation(TickRateModulation.URGENT))
                    .containsExactly(5, 1, 1, 1, 1);
        }
    }

    /**
     * Tests that the tick rate remains the same when SAME is returned.
     */
    private List<Integer> runTicksWithModulation(TickRateModulation modulation) {
        var request = new TickingRequest(1, 10, false, false);
        return runTicks(request, modulation);
    }

    private ArrayList<Integer> runTicks(TickingRequest request, TickRateModulation modulation) {
        var timesSinceLastTick = new ArrayList<Integer>();

        var node = makeTickingNode(request, (tickingNode, ticksSinceLastCall) -> {
            timesSinceLastTick.add(ticksSinceLastCall);
            return modulation;
        });

        int timesTicked = 0;
        while (timesSinceLastTick.size() < 5 && timesTicked++ < 1000) {
            runTick(node.getGrid());
        }
        return timesSinceLastTick;
    }

    private TickManagerService.NodeStatus assertNodeIsAsleep(GridNode node) {
        var status = getNodeStatus(node);
        assertThat(status.awake()).isFalse();
        assertThat(status.sleeping()).isTrue();
        assertThat(status.queued()).isFalse();
        var ticker = node.getService(IGridTickable.class);
        assertThat(status.currentRate()).isEqualTo(ticker.getTickingRequest(node).maxTickRate());
        return status;
    }

    private TickManagerService.NodeStatus assertNodeIsAwake(GridNode node) {
        var status = getNodeStatus(node);
        assertThat(status.awake()).isTrue();
        assertThat(status.sleeping()).isFalse();
        assertThat(status.queued()).isTrue();
        return status;
    }

    private TickManagerService.NodeStatus getNodeStatus(GridNode node) {
        return ((TickManagerService) node.getGrid().getTickManager()).getStatus(node);
    }
}
