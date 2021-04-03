package appeng.container.me;

import java.util.EnumSet;

import javax.annotation.Nonnull;

import appeng.BootstrapMinecraft;
import appeng.core.Api;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.registry.Bootstrap;

import appeng.api.networking.GridFlags;
import appeng.api.networking.GridNotification;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridHost;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import appeng.me.Grid;
import appeng.me.GridNode;
import appeng.util.Platform;

@MockitoSettings
@BootstrapMinecraft
class NetworkStatusTest {

    @Mock
    IGrid grid;

    @Mock
    IEnergyGrid energyGrid;

    private static MockedStatic<Platform> platformMock;

    @BeforeEach
    void setupPlatform() {
        platformMock = Mockito.mockStatic(Platform.class);
        platformMock.when(Platform::isServer).thenReturn(true);
        platformMock.when(Platform::assertServerThread).then(invocation -> null);
//        platformMock.when(Platform::hasClientClasses).thenReturn(true);
    }

    @AfterEach
    void cleanupPlatform() {
        platformMock.close();
    }

    @Test
    void testCreateStatus() {
        Grid grid = Grid.create(new GridNode(new FakeMachine()));

//        when(grid.getCache(IEnergyGrid.class)).thenReturn(energyGrid);
//        when(grid.getMachinesClasses()).thenReturn(new ReadOnlyCollection<>(Lists.newArrayList(InterfaceTileEntity.class, InterfacePart.class)));
//        MachineSet interfaceBlockSet = new MachineSet(InterfaceTileEntity.class);
//        when(grid.getMachines(InterfacePart.class)).thenReturn(interfaceBlockSet);
//        when(grid.getMachines(InterfaceTileEntity.class)).thenReturn(interfacePartSet);

//        NetworkStatus.fromGrid(grid);

    }

    class FakeMachine implements IGridBlock {

        @Override
        public double getIdlePowerUsage() {
            return 0;
        }

        @Nonnull
        @Override
        public EnumSet<GridFlags> getFlags() {
            return null;
        }

        @Override
        public boolean isWorldAccessible() {
            return false;
        }

        @Nonnull
        @Override
        public DimensionalCoord getLocation() {
            return null;
        }

        @Nonnull
        @Override
        public AEColor getGridColor() {
            return null;
        }

        @Override
        public void onGridNotification(@Nonnull GridNotification notification) {

        }

        @Nonnull
        @Override
        public EnumSet<Direction> getConnectableSides() {
            return null;
        }

        @Nonnull
        @Override
        public IGridHost getMachine() {
            return null;
        }

        @Override
        public void gridChanged() {

        }

        @Nonnull
        @Override
        public ItemStack getMachineRepresentation() {
            return null;
        }
    }

}