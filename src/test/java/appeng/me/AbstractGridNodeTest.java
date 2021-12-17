package appeng.me;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import net.minecraft.server.level.ServerLevel;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.energy.IAEPowerStorage;
import appeng.core.worlddata.GridStorageSaveData;
import appeng.core.worlddata.IGridStorageSaveData;
import appeng.me.service.EnergyService;
import appeng.util.BootstrapMinecraft;
import appeng.util.Platform;

@MockitoSettings(strictness = Strictness.LENIENT)
@BootstrapMinecraft
abstract class AbstractGridNodeTest {
    @Mock
    ServerLevel level;

    Object owner = new Object();

    @Mock
    IGridNodeListener<Object> listener;

    @Mock
    MockedStatic<Platform> platform;

    @Mock
    MockedStatic<IGridStorageSaveData> gridStorageSaveData;

    @BeforeEach
    public void setupStaticMocks() {
        platform.when(Platform::isServer).thenReturn(true);
        platform.when(() -> Platform.securityCheck(any(), any())).thenReturn(true);
        gridStorageSaveData.when(() -> IGridStorageSaveData.get(any())).thenReturn(new GridStorageSaveData());
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
