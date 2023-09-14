package appeng.me;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import net.minecraft.server.level.ServerLevel;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.energy.IAEPowerStorage;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.me.service.EnergyService;
import appeng.util.BootstrapMinecraft;
import appeng.util.Platform;

@MockitoSettings(strictness = Strictness.LENIENT)
@BootstrapMinecraft
public abstract class AbstractGridNodeTest {
    @Mock
    protected ServerLevel level;

    protected Object owner = new Object();

    @Mock
    protected IGridNodeListener<Object> listener;

    @Mock
    protected MockedStatic<Platform> platform;

    @BeforeEach
    public void setupMocks() {
        platform.when(Platform::isServer).thenReturn(true);
    }

    protected GridNode makeNode(GridFlags... flags) {
        return new GridNode(level, owner, listener, Set.of(flags));
    }

    protected GridNode makeReadyNode(GridFlags... flags) {
        var node = makeNode(flags);
        node.markReady();
        return node;
    }

    protected GridNode makePoweredNode(GridFlags... flags) {
        var node = makeNode(flags);
        node.addService(IAEPowerStorage.class, new InfinitePowerStorage());
        var grid = node.getInternalGrid();
        ((EnergyService) grid.getEnergyService()).onServerEndTick();
        assertTrue(node.isPowered());
        node.markReady();
        return node;
    }

    protected GridNode makeTickingNode(TickingRequest request, NodeTicker ticker, GridFlags... flags) {
        var node = makeNode(flags);
        node.addService(IGridTickable.class, new IGridTickable() {
            @Override
            public TickingRequest getTickingRequest(IGridNode node) {
                return request;
            }

            @Override
            public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
                return ticker.tick(node, ticksSinceLastCall);
            }
        });
        node.markReady();
        return node;
    }

    @FunctionalInterface
    interface NodeTicker {
        TickRateModulation tick(IGridNode node, int ticksSinceLastTick);
    }

    protected final void runTick(IGrid grid, int count) {
        for (int i = 0; i < count; i++) {
            runTick(grid);
        }
    }

    protected final void runTick(IGrid grid) {
        var internalGrid = (Grid) grid;
        internalGrid.onServerStartTick();
        internalGrid.onLevelStartTick(level);
        internalGrid.onLevelEndTick(level);
        internalGrid.onServerEndTick();
    }
}
