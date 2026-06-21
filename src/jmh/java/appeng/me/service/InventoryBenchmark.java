package appeng.me.service;

import java.util.Random;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import net.minecraft.world.item.Items;

import appeng.api.config.IncludeExclude;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.KeyCounter;
import appeng.core.definitions.AEItems;
import appeng.me.cells.BasicCellInventory;
import appeng.me.storage.MEInventoryHandler;
import appeng.me.storage.NetworkStorage;
import appeng.util.BootstrapMinecraftExtension;
import appeng.util.prioritylist.PrecisePriorityList;

@State(Scope.Thread)
public class InventoryBenchmark {

    private BasicCellInventory emptyCell;
    private BasicCellInventory fullCell;
    private NetworkStorage networkStorage;
    private MEInventoryHandler inventoryHandler;
    private StorageService service;

    @Setup
    public void setup() throws Exception {
        new BootstrapMinecraftExtension().beforeAll(null);
        InventoryGenerator generator = new InventoryGenerator(new Random(0L));
        // A single empty cell
        emptyCell = BasicCellInventory.createInventory(AEItems.ITEM_CELL_1K.stack(), this::onContainerUpdate);
        // A single full cell
        fullCell = BasicCellInventory.createInventory(AEItems.ITEM_CELL_1K.stack(), this::onContainerUpdate);
        generator.fillInventory(fullCell, 63);

        // A subnet with cells
        networkStorage = new NetworkStorage();
        for (int i = 0; i < 100; i++) {
            networkStorage.mount(0,
                    BasicCellInventory.createInventory(AEItems.ITEM_CELL_1K.stack(), this::onContainerUpdate));
        }
        generator.fillInventory(networkStorage, 6300);

        inventoryHandler = new MEInventoryHandler(networkStorage);
        inventoryHandler.setWhitelist(IncludeExclude.BLACKLIST);
        inventoryHandler.setExtractFiltering(true, true);
        KeyCounter filter = new KeyCounter();
        filter.add(AEItemKey.of(Items.COBBLESTONE), 1);
        inventoryHandler.setPartitionList(new PrecisePriorityList(filter));

        // A network with subnets containing cells
        service = new StorageService();
        NetworkStorage serviceNetwork = (NetworkStorage) service.getInventory();
        for (int j = 0; j < 10; j++) {
            NetworkStorage bank = new NetworkStorage();
            for (int i = 0; i < 100; i++) {
                bank.mount(0,
                        BasicCellInventory.createInventory(AEItems.ITEM_CELL_1K.stack(), this::onContainerUpdate));
            }
            MEInventoryHandler bankHandler = new MEInventoryHandler(bank);
            bankHandler.setWhitelist(IncludeExclude.BLACKLIST);
            bankHandler.setExtractFiltering(true, true);
            KeyCounter bankFilter = new KeyCounter();
            bankFilter.add(AEItemKey.of(Items.COBBLESTONE), 1);
            bankHandler.setPartitionList(new PrecisePriorityList(bankFilter));
            serviceNetwork.mount(0, bankHandler);
        }
        generator.fillInventory(serviceNetwork, 63000);
    }

    @Benchmark
    public void getAvailableStacks_emptyCell(Blackhole bh) {
        bh.consume(emptyCell.getAvailableStacks());
    }

    @Benchmark
    public void getAvailableStacks_fullCell(Blackhole bh) {
        bh.consume(fullCell.getAvailableStacks());
    }

    @Benchmark
    public void getAvailableStacks_networkStorage(Blackhole bh) {
        bh.consume(networkStorage.getAvailableStacks());
    }

    @Benchmark
    public void getAvailableStacks_inventoryHandler(Blackhole bh) {
        bh.consume(inventoryHandler.getAvailableStacks());
    }

    @Benchmark
    public void getAvailableStacks_service(Blackhole bh) {
        service.invalidateCache();
        bh.consume(service.getCachedInventory());
    }

    public void onContainerUpdate() {
    }

}
