package appeng.container.me;

import appeng.MinecraftTest;
import appeng.api.networking.GridFlags;
import appeng.api.networking.GridNotification;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingCallback;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.core.Api;
import appeng.core.worlddata.WorldData;
import appeng.crafting.CraftingJob;
import appeng.hooks.ticking.TickHandler;
import appeng.me.Grid;
import appeng.me.GridNode;
import appeng.me.helpers.MachineSource;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import appeng.util.item.ItemList;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.Mockito.mock;

@MinecraftTest
@MockitoSettings
class NetworkStatusTest {

    @TempDir
    File tempDir;

    private static MockedStatic<Platform> platformMock;

    private TestServer server;
    private ServerWorld overworld;

    @BeforeEach
    void setupPlatform() throws Exception {
        platformMock = Mockito.mockStatic(Platform.class);
        platformMock.when(Platform::isServer).thenReturn(true);
        platformMock.when(Platform::assertServerThread).then(invocation -> null);
//        platformMock.when(Platform::hasClientClasses).thenReturn(true);

        server = TestServer.create(tempDir.toPath());
        overworld = server.getWorld(ServerWorld.OVERWORLD);

        TickHandler.instance();
        WorldData.onServerStarting(server);

    }

    @AfterEach
    void cleanupPlatform() {
        platformMock.close();
    }

    @Test
    void testCreateStatus() throws Throwable {

        FakeMachine machine = new FakeMachine();
        Grid grid = Grid.create(new GridNode(machine));

        Block iface = Api.instance().definitions().blocks().iface().block();
        overworld.setBlockState(BlockPos.ZERO, iface.getDefaultState());
        server.tick();

        TileEntity ifaceTe = overworld.getTileEntity(BlockPos.ZERO);

        IActionSource actionSrc = new MachineSource(machine);

        ICraftingCallback callback = mock(ICraftingCallback.class);
        IAEItemStack what = AEItemStack.fromItemStack(new ItemStack(Items.HOPPER));
        CraftingJob job = new CraftingJob(overworld, grid, actionSrc, what, callback);
        AtomicReference<Throwable> jobException = new AtomicReference<>();
        Thread craftingThread = new Thread(job);
        craftingThread.setUncaughtExceptionHandler((t, e) -> {
            jobException.set(e);
        });
        craftingThread.start();
        while (job.simulateFor(50)) {
            Thread.yield();
        }
        if (jobException.get() != null) {
            throw jobException.get();
        }

        ItemList il = new ItemList();
        job.populatePlan(il);
        System.out.println();

    }

    static class FakeMachine implements IGridBlock, IGridHost, IActionHost {

        @Override
        public double getIdlePowerUsage() {
            return 0;
        }

        @Nonnull
        @Override
        public EnumSet<GridFlags> getFlags() {
            return EnumSet.noneOf(GridFlags.class);
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
            return AEColor.TRANSPARENT;
        }

        @Override
        public void onGridNotification(@Nonnull GridNotification notification) {

        }

        @Nonnull
        @Override
        public EnumSet<Direction> getConnectableSides() {
            return EnumSet.noneOf(Direction.class);
        }

        @Nonnull
        @Override
        public IGridHost getMachine() {
            return this;
        }

        @Override
        public void gridChanged() {
        }

        @Nonnull
        @Override
        public ItemStack getMachineRepresentation() {
            return new ItemStack(Items.ACACIA_BOAT);
        }

        @Nullable
        @Override
        public IGridNode getGridNode(@NotNull AEPartLocation dir) {
            return null;
        }

        @NotNull
        @Override
        public AECableType getCableConnectionType(@NotNull AEPartLocation dir) {
            return AECableType.NONE;
        }

        @Override
        public void securityBreak() {
        }

        @Nullable
        @Override
        public IGridNode getActionableNode() {
            return getGridNode(AEPartLocation.INTERNAL);
        }
    }


}