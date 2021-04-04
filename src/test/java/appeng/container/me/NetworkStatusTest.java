package appeng.container.me;

import appeng.MinecraftTest;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingCallback;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.Api;
import appeng.core.worlddata.WorldData;
import appeng.crafting.CraftingJob;
import appeng.hooks.ticking.TickHandler;
import appeng.me.helpers.MachineSource;
import appeng.tile.misc.InterfaceTileEntity;
import appeng.tile.networking.CreativeEnergyCellTileEntity;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import appeng.util.item.ItemList;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
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
        platformMock = Mockito.mockStatic(Platform.class, CALLS_REAL_METHODS);
        platformMock.when(Platform::isServer).thenReturn(true);
        platformMock.when(Platform::isClient).thenReturn(false);
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

        overworld.setBlockState(
                BlockPos.ZERO,
                Api.instance().definitions().blocks().iface().block().getDefaultState()
        );
        overworld.setBlockState(
                BlockPos.ZERO.add(1, 0, 0),
                Api.instance().definitions().blocks().energyCellCreative().block().getDefaultState()
        );
        server.tick();

        InterfaceTileEntity ifaceTe = (InterfaceTileEntity) overworld.getTileEntity(BlockPos.ZERO);
        CreativeEnergyCellTileEntity cell = (CreativeEnergyCellTileEntity) overworld.getTileEntity(BlockPos.ZERO.add(1, 0, 0));

        ICraftingRecipe recipe = (ICraftingRecipe) server.getRecipeManager().getRecipe(new ResourceLocation("minecraft:hopper"))
                .orElseThrow(() -> new RuntimeException("Missing hopper recipe"));
        ItemStack encodedPattern = Api.instance().crafting().encodeCraftingPattern(null, recipe, new ItemStack[]{
                new ItemStack(Items.IRON_INGOT), ItemStack.EMPTY, new ItemStack(Items.IRON_INGOT),
                new ItemStack(Items.IRON_INGOT), new ItemStack(Items.CHEST), new ItemStack(Items.IRON_INGOT),
                ItemStack.EMPTY, new ItemStack(Items.IRON_INGOT), ItemStack.EMPTY
        }, new ItemStack(Items.HOPPER), false);
        assertTrue(ifaceTe.getInventoryByName("patterns").insertItem(0, encodedPattern, false).isEmpty());
        server.tick();

        IActionSource actionSrc = new MachineSource(ifaceTe);
        IGrid grid = ifaceTe.getProxy().getGrid();

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

}