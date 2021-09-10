package appeng.crafting.simulation.helpers;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.*;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.crafting.CraftingCalculation;
import appeng.crafting.CraftingPlan;
import appeng.me.helpers.BaseActionSource;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

public class SimulationEnv {
	private final Map<IAEStack<?>, List<IPatternDetails>> patterns = new HashMap<>();
	private final Set<IAEStack<?>> emitableItems = new HashSet<>();
	private final IItemList<IAEItemStack> networkStorage = StorageChannels.items().createList();

	public IPatternDetails addPattern(IPatternDetails pattern) {
		patterns.computeIfAbsent(pattern.getPrimaryOutput(), s -> new ArrayList<>()).add(pattern);
		return pattern;
	}

	public void setEmitable(IAEStack<?> stack) {
		emitableItems.add(stack);
	}

	public void addStoredItem(IAEItemStack stack) {
		this.networkStorage.addStorage(stack);
	}

	public CraftingPlan doCrafting(IAEItemStack what) {
		var calculation = new CraftingCalculation(mock(Level.class), gridMock, new BaseActionSource(), what, null);
		try {
			var calculationFuture = Executors.newSingleThreadExecutor().submit(calculation::run);
			calculation.simulateFor(1000000000);
			return calculationFuture.get(1000, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private final IGrid gridMock = createGridMock();

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
			public ImmutableCollection<IPatternDetails> getCraftingFor(IAEItemStack whatToCraft, IPatternDetails details, int slot, Level level) {
				var patternList = patterns.get(whatToCraft);
				if (patternList == null) {
					return ImmutableList.of();
				} else {
					return ImmutableList.copyOf(patternList);
				}
			}

			@Override
			public Future<ICraftingPlan> beginCraftingJob(Level level, IActionSource actionSrc, IAEItemStack craftWhat, ICraftingCallback callback) {
				throw new UnsupportedOperationException();
			}

			@Override
			public ICraftingLink submitJob(ICraftingPlan job, ICraftingRequester requestingMachine, ICraftingCPU target, boolean prioritizePower, IActionSource src) {
				throw new UnsupportedOperationException();
			}

			@Override
			public ImmutableSet<ICraftingCPU> getCpus() {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean canEmitFor(IAEItemStack what) {
				return emitableItems.contains(what);
			}

			@Override
			public boolean isRequesting(IAEItemStack what) {
				throw new UnsupportedOperationException();
			}

			@Override
			public long requesting(IAEItemStack what) {
				throw new UnsupportedOperationException();
			}
		};
	}

	private IStorageService createStorageServiceMock() {
		IStorageService mock = mock(IStorageService.class);
		IMEMonitor<IAEItemStack> monitor = new IMEMonitor<IAEItemStack>() {
			@Override
			public IItemList<IAEItemStack> getAvailableItems(IItemList<IAEItemStack> out) {
				throw new UnsupportedOperationException();
			}

			@Override
			public IItemList<IAEItemStack> getStorageList() {
				return networkStorage;
			}

			@Override
			public void addListener(IMEMonitorHandlerReceiver<IAEItemStack> l, Object verificationToken) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void removeListener(IMEMonitorHandlerReceiver<IAEItemStack> l) {
				throw new UnsupportedOperationException();
			}

			@Override
			public IAEItemStack injectItems(IAEItemStack input, Actionable type, IActionSource src) {
				throw new UnsupportedOperationException();
			}

			@Override
			public IAEItemStack extractItems(IAEItemStack request, Actionable mode, IActionSource src) {
				if (mode == Actionable.SIMULATE) {
					IAEItemStack precise = networkStorage.findPrecise(request);
					if (precise == null) return null;
					return precise.copyWithStackSize(Math.min(precise.getStackSize(), request.getStackSize()));
				} else {
					throw new UnsupportedOperationException();
				}
			}

			@Override
			public IStorageChannel<IAEItemStack> getChannel() {
				return StorageChannels.items();
			}
		};
		when(mock.getInventory(StorageChannels.items())).thenReturn(monitor);
		return mock;
	}
}
