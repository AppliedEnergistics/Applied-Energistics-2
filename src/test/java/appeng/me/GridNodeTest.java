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
     * Regression test for the {@link appeng.integration.modules.waila.GridNodeState#NETWORK_BOOTING} notification. It
     * was previously sent before the state actually changed, causing various problems.
     */
    @Test
    public void rebootNotificationIsPostedAfterGridBeginsBooting() {
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
        runTick(node.getGrid());
        assertThat(calls).containsExactly(false, true);
    }

}
