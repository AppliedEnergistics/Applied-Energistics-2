package appeng.container.me;

import appeng.MinecraftTest;
import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingCallback;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.Api;
import appeng.crafting.CraftingJob;
import appeng.me.helpers.MachineSource;
import appeng.tile.misc.InterfaceTileEntity;
import appeng.tile.networking.CreativeEnergyCellTileEntity;
import appeng.tile.storage.DriveTileEntity;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import appeng.util.item.ItemList;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.items.CapabilityItemHandler;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        // Since we're not in the "server thread group" that Forge creates,
        // we need to fake a few Platform methods that deal with the logical side.
        platformMock = Mockito.mockStatic(Platform.class, CALLS_REAL_METHODS);
        platformMock.when(Platform::isServer).thenReturn(true);
        platformMock.when(Platform::isClient).thenReturn(false);
        platformMock.when(Platform::assertServerThread).then(invocation -> null);
//        platformMock.when(Platform::hasClientClasses).thenReturn(true);

        server = TestServer.create(tempDir.toPath());
        overworld = server.getWorld(ServerWorld.OVERWORLD);
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
                BlockPos.ZERO.add(0, 1, 0),
                Api.instance().definitions().blocks().molecularAssembler().block().getDefaultState()
        );
        overworld.setBlockState(
                BlockPos.ZERO.add(1, 1, 0),
                Api.instance().definitions().blocks().energyCellCreative().block().getDefaultState()
        );
        overworld.setBlockState(
                BlockPos.ZERO.add(1, 0, 0),
                Api.instance().definitions().blocks().energyCellCreative().block().getDefaultState()
        );
        overworld.setBlockState(
                BlockPos.ZERO.add(2, 0, 0),
                Api.instance().definitions().blocks().craftingStorage64k().block().getDefaultState()
        );
        overworld.setBlockState(
                BlockPos.ZERO.add(3, 0, 0),
                Api.instance().definitions().blocks().drive().block().getDefaultState()
        );
        // Tick to run tile initialization
        server.tick();

        InterfaceTileEntity ifaceTe = (InterfaceTileEntity) overworld.getTileEntity(BlockPos.ZERO);
        CreativeEnergyCellTileEntity cell = (CreativeEnergyCellTileEntity) overworld.getTileEntity(BlockPos.ZERO.add(1, 0, 0));
        DriveTileEntity drive = (DriveTileEntity) overworld.getTileEntity(BlockPos.ZERO.add(3, 0, 0));
        IGrid grid = ifaceTe.getProxy().getGrid();

        // Insert storage cell into drive
        assertTrue(drive.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                .orElseThrow(RuntimeException::new)
                .insertItem(0, Api.instance().definitions().items().cell1k().stack(1), false).isEmpty());

        // Inject required items for crafting into network
        IMEMonitor<IAEItemStack> itemInv = ifaceTe.getProxy().getStorage().getInventory(Api.instance().storage().getStorageChannel(IItemStorageChannel.class));
        for (ItemStack is : new ItemStack[]{
                new ItemStack(Items.IRON_INGOT, 5),
                new ItemStack(Items.CHEST, 1)
        }) {
            assertNull(itemInv.injectItems(AEItemStack.fromItemStack(is), Actionable.MODULATE, new MachineSource(ifaceTe)));
        }

        // Insert pattern for hopper into interface
        ICraftingRecipe recipe = (ICraftingRecipe) server.getRecipeManager().getRecipe(new ResourceLocation("minecraft:hopper"))
                .orElseThrow(() -> new RuntimeException("Missing hopper recipe"));
        ItemStack encodedPattern = Api.instance().crafting().encodeCraftingPattern(null, recipe, new ItemStack[]{
                new ItemStack(Items.IRON_INGOT), ItemStack.EMPTY, new ItemStack(Items.IRON_INGOT),
                new ItemStack(Items.IRON_INGOT), new ItemStack(Items.CHEST), new ItemStack(Items.IRON_INGOT),
                ItemStack.EMPTY, new ItemStack(Items.IRON_INGOT), ItemStack.EMPTY
        }, new ItemStack(Items.HOPPER), false);
        assertTrue(ifaceTe.getInventoryByName("patterns").insertItem(0, encodedPattern, false).isEmpty());
        server.tick();

        assertThat(getItemReport(grid))
                .containsOnly("chest stored=1", "hopper craft", "iron_ingot stored=5");

        IActionSource actionSrc = new MachineSource(ifaceTe);

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

        assertThat(getPlanSummary(job))
                .containsExactly(
                        "[I] chest x 1",
                        "[I] iron_ingot x 5",
                        "[O] hopper x 1"
                );

        ICraftingGrid cc = grid.getCache(ICraftingGrid.class);
        ICraftingLink g = cc.submitJob(job, null, null, true, actionSrc);

        ICraftingCPU cpu = cc.getCpus().iterator().next();

        long ticks = 500;
        while (!g.isDone() && ticks-- > 0) {
            server.tick();
        }
        assertThat(g.isDone()).describedAs("crafting operation completed in 500 ticks").isTrue();

        assertThat(getItemReport(grid))
                .containsOnly("chest stored=1", "hopper craft", "iron_ingot stored=5");
    }

    private List<String> getItemReport(IGrid grid) {
        IStorageGrid storageGrid = grid.getCache(IStorageGrid.class);
        IMEMonitor<IAEItemStack> inventory = storageGrid.getInventory(Api.instance().storage().getStorageChannel(IItemStorageChannel.class));
        List<String> result = new ArrayList<>();
        for (IAEItemStack item : inventory.getStorageList()) {
            result.add(getItemReport(item));
        }
        result.sort(String::compareTo);
        return result;
    }

    @NotNull
    private String getItemReport(IAEItemStack item) {
        StringBuilder line = new StringBuilder();
        line.append(item.getItem());
        if (item.getStackSize() > 0) {
            line.append(" stored=").append(item.getStackSize());
        }
        if (item.getCountRequestable() > 0) {
            line.append(" req=").append(item.getCountRequestable());
        }
        if (item.isCraftable()) {
            line.append(" craft");
        }
        return line.toString();
    }

    private List<String> getPlanSummary(CraftingJob job) {
        ItemList il = new ItemList();
        job.populatePlan(il);

        List<String> result = new ArrayList<>();

        for (IAEItemStack item : il) {
            StringBuilder line = new StringBuilder();
            long count;
            if (item.getStackSize() > 0) {
                line.append("[I]");
                count = item.getStackSize();
            } else if (item.getCountRequestable() > 0) {
                line.append("[O]");
                count = item.getCountRequestable();
            } else {
                line.append("[M]");
                count = item.getStackSize();
            }
            line.append(" ").append(item.getItem().toString()).append(" x ").append(count);
            result.add(line.toString());
        }

        result.sort(String::compareTo);

        return result;
    }

}