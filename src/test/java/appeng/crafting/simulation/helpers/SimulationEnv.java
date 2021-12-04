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
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.Level;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.*;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.AEKeyFilter;
import appeng.api.storage.IMEMonitorListener;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.MEMonitorStorage;
import appeng.crafting.CraftingCalculation;
import appeng.crafting.CraftingPlan;
import appeng.me.helpers.BaseActionSource;

public class SimulationEnv {
    private final Map<AEKey, List<IPatternDetails>> patterns = new HashMap<>();
    private final KeyCounter craftableItemsList = new KeyCounter();
    private final Set<AEKey> emitableItems = new HashSet<>();
    private final KeyCounter networkStorage = new KeyCounter();

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
            public AEKey getFuzzyCraftable(AEKey whatToCraft, AEKeyFilter filter) {
                for (var fuzzy : craftableItemsList.findFuzzy(whatToCraft, FuzzyMode.IGNORE_ALL)) {
                    if (filter.matches(fuzzy.getKey())) {
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
            public Set<AEKey> getCraftables(AEKeyFilter filter) {
                return craftableItemsList.keySet().stream().filter(filter::matches).collect(Collectors.toSet());
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
        MEMonitorStorage monitor = createMonitorMock();
        return new IStorageService() {
            @Override
            public void postAlterationOfStoredItems(Iterable<AEKey> input, IActionSource src) {
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
            public MEMonitorStorage getInventory() {
                return monitor;
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

    private MEMonitorStorage createMonitorMock() {
        return new MEMonitorStorage() {
            @Override
            public void getAvailableStacks(KeyCounter out) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Component getDescription() {
                return TextComponent.EMPTY;
            }

            @Override
            public KeyCounter getCachedAvailableStacks() {
                var result = new KeyCounter();
                for (var entry : networkStorage) {
                    result.add(entry.getKey(), entry.getLongValue());
                }
                return result;
            }

            @Override
            public void addListener(IMEMonitorListener l, Object verificationToken) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void removeListener(IMEMonitorListener l) {
                throw new UnsupportedOperationException();
            }

            @Override
            public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
                throw new UnsupportedOperationException();
            }

            @Override
            public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
                if (mode == Actionable.SIMULATE) {
                    var stored = networkStorage.get(what);
                    return Math.min(amount, stored);
                } else {
                    throw new UnsupportedOperationException();
                }
            }
        };
    }

    private IGridNode createNodeMock() {
        IGridNode mock = mock(IGridNode.class);
        when(mock.getGrid()).thenReturn(gridMock);
        return mock;
    }
}
