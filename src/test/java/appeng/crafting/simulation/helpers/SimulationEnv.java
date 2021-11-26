package appeng.crafting.simulation.helpers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import net.minecraft.world.level.Level;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.*;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.storage.GenericStack;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorListener;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.data.AEKey;
import appeng.api.storage.data.KeyCounter;
import appeng.crafting.CraftingCalculation;
import appeng.crafting.CraftingPlan;
import appeng.me.helpers.BaseActionSource;

public class SimulationEnv {
    private final Map<AEKey, List<IPatternDetails>> patterns = new HashMap<>();
    private final KeyCounter<AEKey> craftableItemsList = new KeyCounter<>();
    private final Set<AEKey> emitableItems = new HashSet<>();
    private final KeyCounter<AEKey> networkStorage = new KeyCounter<>();

    public IPatternDetails addPattern(IPatternDetails pattern) {
        var output = pattern.getPrimaryOutput();
        patterns.computeIfAbsent(output.what(), s -> new ArrayList<>()).add(pattern);
        craftableItemsList.add(output.what(), 1);
        return pattern;
    }

    public void addEmitable(AEKey stack) {
        emitableItems.add(stack);
    }

    public void addStoredItem(AEKey key, long amount) {
        this.networkStorage.add(key, amount);
    }

    public void addStoredItem(GenericStack stack) {
        this.networkStorage.add(stack.what(), stack.amount());
    }

    public SimulationEnv copy() {
        var copy = new SimulationEnv();
        for (var entry : patterns.entrySet()) {
            for (var pattern : entry.getValue()) {
                copy.addPattern(pattern);
            }
        }
        for (var emitable : emitableItems) {
            copy.addEmitable(emitable);
        }
        for (var stack : networkStorage) {
            copy.addStoredItem(stack.getKey(), stack.getLongValue());
        }
        return copy;
    }

    public CraftingPlan runSimulation(GenericStack what) {
        var calculation = new CraftingCalculation(mock(Level.class), gridMock, simulationRequester, what);
        try {
            var calculationFuture = Executors.newSingleThreadExecutor().submit(calculation::run);
            calculation.simulateFor(1000000000);
            return calculationFuture.get(1000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final IGrid gridMock = createGridMock();
    private final IGridNode nodeMock = createNodeMock();
    private final ICraftingSimulationRequester simulationRequester = new ICraftingSimulationRequester() {
        @Override
        public IActionSource getActionSource() {
            return new BaseActionSource();
        }

        @Override
        public IGridNode getGridNode() {
            return nodeMock;
        }
    };

    private IGrid createGridMock() {
        IGrid mock = mock(IGrid.class);
        ICraftingService craftingService = createCraftingServiceMock();
        IStorageService storageService = createStorageServiceMock();
        when(mock.getCraftingService()).thenReturn(craftingService);
        when(mock.getStorageService()).thenReturn(storageService);
        return mock;
    }

    private ICraftingService createCraftingServiceMock() {
        return new ICraftingService() {
            @Override
            public ImmutableCollection<IPatternDetails> getCraftingFor(AEKey whatToCraft) {
                var list = patterns.get(whatToCraft);
                if (list == null) {
                    return ImmutableList.of();
                }
                return ImmutableList.copyOf(list);
            }

            @Nullable
            @Override
            public AEKey getFuzzyCraftable(AEKey whatToCraft, Predicate<AEKey> filter) {
                for (var fuzzy : craftableItemsList.findFuzzy(whatToCraft, FuzzyMode.IGNORE_ALL)) {
                    if (filter.test(fuzzy.getKey())) {
                        return fuzzy.getKey();
                    }
                }
                return null;
            }

            @Override
            public Future<ICraftingPlan> beginCraftingCalculation(Level level,
                    ICraftingSimulationRequester simRequester, AEKey craftWhat, long amount) {
                throw new UnsupportedOperationException();
            }

            @Override
            public ICraftingLink submitJob(ICraftingPlan job, ICraftingRequester requestingMachine, ICraftingCPU target,
                    boolean prioritizePower, IActionSource src) {
                throw new UnsupportedOperationException();
            }

            @Override
            public ImmutableSet<ICraftingCPU> getCpus() {
                throw new UnsupportedOperationException();
            }

            @Override
            public <T extends AEKey> Set<T> getCraftables(IStorageChannel<T> channel) {
                return craftableItemsList.keySet()
                        .stream()
                        .filter(k -> k.getChannel() == channel)
                        .map(k -> k.cast(channel))
                        .collect(Collectors.toSet());
            }

            @Override
            public boolean canEmitFor(AEKey what) {
                return emitableItems.contains(what);
            }

            @Override
            public boolean isRequesting(AEKey what) {
                throw new UnsupportedOperationException();
            }

            @Override
            public long requesting(AEKey what) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void refreshNodeCraftingProvider(IGridNode node) {
                throw new UnsupportedOperationException();
            }
        };
    }

    private IStorageService createStorageServiceMock() {
        Map<IStorageChannel<?>, IMEMonitor<?>> monitors = new HashMap<>();
        return new IStorageService() {
            @Override
            public <T extends AEKey> void postAlterationOfStoredItems(IStorageChannel<T> chan,
                    Iterable<T> input, IActionSource src) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void addGlobalStorageProvider(IStorageProvider cc) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void removeGlobalStorageProvider(IStorageProvider cc) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <T extends AEKey> IMEMonitor<T> getInventory(IStorageChannel<T> channel) {
                return monitors.computeIfAbsent(channel, chan -> createMonitorMock(chan)).cast(channel);
            }

            @Override
            public void refreshNodeStorageProvider(IGridNode node) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void refreshGlobalStorageProvider(IStorageProvider provider) {
                throw new UnsupportedOperationException();
            }
        };
    }

    private <T extends AEKey> IMEMonitor<T> createMonitorMock(IStorageChannel<T> channel) {
        return new IMEMonitor<>() {
            @Override
            public void getAvailableStacks(KeyCounter<T> out) {
                throw new UnsupportedOperationException();
            }

            @Override
            public KeyCounter<T> getCachedAvailableStacks() {
                var result = new KeyCounter<T>();
                for (var entry : networkStorage) {
                    if (entry.getKey().getChannel() == channel) {
                        result.add(entry.getKey().cast(channel), entry.getLongValue());
                    }
                }
                return result;
            }

            @Override
            public void addListener(IMEMonitorListener<T> l, Object verificationToken) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void removeListener(IMEMonitorListener<T> l) {
                throw new UnsupportedOperationException();
            }

            @Override
            public long insert(T what, long amount, Actionable mode, IActionSource source) {
                throw new UnsupportedOperationException();
            }

            @Override
            public long extract(T what, long amount, Actionable mode, IActionSource source) {
                if (mode == Actionable.SIMULATE) {
                    var stored = networkStorage.get(what);
                    return Math.min(amount, stored);
                } else {
                    throw new UnsupportedOperationException();
                }
            }

            @Override
            public IStorageChannel<T> getChannel() {
                return channel;
            }
        };
    }

    private IGridNode createNodeMock() {
        IGridNode mock = mock(IGridNode.class);
        when(mock.getGrid()).thenReturn(gridMock);
        return mock;
    }
}
