package appeng.me;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import appeng.api.networking.IGridNodeListener;
import appeng.me.service.PathingService;

class GridNodeTest extends AbstractGridNodeTest {
    /**
     * Tests that:
     * <ul>
     * <li>{@link appeng.integration.modules.waila.GridNodeState#NETWORK_BOOTING} is only sent after booting is
     * complete, and that the node is active after that event.</li>
     * <li>No event is sent when booting starts.</li>
     * </ul>
     */
    @Test
    public void testRebootNotifications() {
        var node = makePoweredNode();
        assertTrue(node.hasGridBooted());
        reset(listener);
        var calls = new ArrayList<Boolean>();
        doAnswer(invocation -> {
            calls.add(node.hasGridBooted());
            return null;
        }).when(listener).onStateChanged(owner, node, IGridNodeListener.State.GRID_BOOT);
        var pathingService = (PathingService) node.getGrid().getPathingService();
        pathingService.repath();
        // First tick: should start rebooting, but not send any notification.
        runTick(node.getGrid());
        assertThat(calls).isEmpty();
        // After a few ticks: reboot should be complete, and node should be active in the event listener
        for (int i = 0; i < 10; ++i) {
            runTick(node.getGrid());
        }
        assertThat(calls).containsOnly(true);
    }

}
