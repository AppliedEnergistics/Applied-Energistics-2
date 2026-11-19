package appeng.me.storage;

import java.util.Map;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.storage.MEStorage;
import appeng.me.helpers.BaseActionSource;

/**
 * Network storage made of storage buses on barrel-sized containers, as set up by {@code StorageBusPart}.
 */
@State(Scope.Thread)
public class StorageBusExtractBenchmark {

    @Param({ "10", "100", "225" })
    public int storageBuses;

    @Param({ "0", "9" })
    public int itemTypesPerBus;

    private final BaseActionSource source = new BaseActionSource();
    private NetworkStorage networkStorage;
    private AEItemKey absentKey;
    private AEItemKey presentKey;

    @Setup
    public void setup() {
        EphemeralTestServerProvider.grabServer();
        var items = BuiltInRegistries.ITEM.stream().filter(item -> item != Items.AIR && item != Items.OAK_PLANKS
                && item != Items.DIAMOND && item.getDefaultMaxStackSize() == 64).limit(1000).toList();
        absentKey = AEItemKey.of(Items.OAK_PLANKS);
        presentKey = AEItemKey.of(Items.DIAMOND);

        networkStorage = new NetworkStorage();
        for (int i = 0; i < storageBuses; i++) {
            var container = new SimpleContainer(27);
            for (int slot = 0; slot < itemTypesPerBus; slot++) {
                container.setItem(slot, new ItemStack(items.get((i * 7 + slot) % items.size()), 32));
            }
            if (i == storageBuses - 1) {
                container.setItem(26, new ItemStack(Items.DIAMOND, 64));
            }
            var facade = ExternalStorageFacade.ofItemHandler(VanillaContainerWrapper.of(container));
            facade.setExtractableOnly(true);
            var compositeStorage = new CompositeStorage(Map.<AEKeyType, MEStorage>of(AEKeyType.items(), facade));
            compositeStorage.onTick();
            networkStorage.mount(0, new MEInventoryHandler(compositeStorage));
        }
    }

    @Benchmark
    public long extractAbsentKey() {
        return networkStorage.extract(absentKey, 64, Actionable.SIMULATE, source);
    }

    @Benchmark
    public long extractKeyInLastBus() {
        return networkStorage.extract(presentKey, 64, Actionable.SIMULATE, source);
    }

    @Benchmark
    public long insertAbsentKey() {
        return networkStorage.insert(absentKey, 1, Actionable.SIMULATE, source);
    }
}
