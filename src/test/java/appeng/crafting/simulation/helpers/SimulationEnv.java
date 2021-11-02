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
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorListener;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.cells.ICellProvider;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackList;
import appeng.api.storage.data.MixedStackList;
import appeng.crafting.CraftingCalculation;
import appeng.crafting.CraftingPlan;
import appeng.me.helpers.BaseActionSource;

public class SimulationEnv {
    private final Map<IAEStack, List<IPatternDetails>> patterns = new HashMap<>();
    private final MixedStackList craftableItemsList = new MixedStackList();
    private final Set<IAEStack> emitableItems = new HashSet<>();
    private final MixedStackList networkStorage = new MixedStackList();

    public IPatternDetails addPattern(IPatternDetails pattern) {
        var output = pattern.getPrimaryOutput();
        patterns.computeIfAbsent(output, s -> new ArrayList<>()).add(pattern);
        craftableItemsList.add(output);
        return pattern;
    }

    public void addEmitable(IAEStack stack) {
        emitableItems.add(stack);
    }

    public void addStoredItem(IAEStack stack) {
        this.networkStorage.addStorage(stack);
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
            copy.addStoredItem(stack);
        }
        return copy;
    }

    public CraftingPlan runSimulation(IAEStack what) {
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
            public ImmutableCollection<IPatternDetails> getCraftingFor(final IAEStack whatToCraft) {
                var list = patterns.get(whatToCraft);
                if (list == null) {
                    return ImmutableList.of();
                }
                return ImmutableList.copyOf(list);
            }

            @Nullable
            @Override
            public IAEStack getFuzzyCraftable(IAEStack whatToCraft, Predicate<IAEStack> filter) {
                for (var fuzzy : craftableItemsList.findFuzzy(whatToCraft, FuzzyMode.IGNORE_ALL)) {
                    if (filter.test(fuzzy)) {
                        return IAEStack.copy(fuzzy, whatToCraft.getStackSize());
                    }
                }
                return null;
            }

            @Override
            public Future<ICraftingPlan> beginCraftingCalculation(Level level,
                    ICraftingSimulationRequester simRequester, IAEStack craftWhat) {
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
            public boolean canEmitFor(IAEStack what) {
                return emitableItems.contains(what);
            }

            @Override
            public boolean isRequesting(IAEStack what) {
                throw new UnsupportedOperationException();
            }

            @Override
            public long requesting(IAEStack what) {
                throw new UnsupportedOperationException();
            }
        };
    }

    private IStorageService createStorageServiceMock() {
        Map<IStorageChannel<?>, IMEMonitor<?>> monitors = new HashMap<>();
        return new IStorageService() {
            @Override
            public <T extends IAEStack> void postAlterationOfStoredItems(IStorageChannel<T> chan,
                    Iterable<T> input, IActionSource src) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void registerAdditionalCellProvider(ICellProvider cc) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void unregisterAdditionalCellProvider(ICellProvider cc) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <T extends IAEStack> IMEMonitor<T> getInventory(IStorageChannel<T> channel) {
                return monitors.computeIfAbsent(channel, chan -> createMonitorMock(chan)).cast(channel);
            }
        };
    }

    private <T extends IAEStack> IMEMonitor<T> createMonitorMock(IStorageChannel<T> channel) {
        return new IMEMonitor<>() {
            @Override
            public IAEStackList<T> getAvailableStacks(IAEStackList<T> out) {
                throw new UnsupportedOperationException();
            }

            @Override
            public IAEStackList<T> getCachedAvailableStacks() {
                return networkStorage.getList(channel);
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
            public T injectItems(T input, Actionable type, IActionSource src) {
                throw new UnsupportedOperationException();
            }

            @Override
            public T extractItems(T request, Actionable mode, IActionSource src) {
                if (mode == Actionable.SIMULATE) {
                    T precise = networkStorage.getList(channel).findPrecise(request);
                    if (precise == null)
                        return null;
                    return IAEStack.copy(precise, Math.min(precise.getStackSize(), request.getStackSize()));
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
