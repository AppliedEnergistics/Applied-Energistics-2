/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.RangedWrapper;

import appeng.api.config.Actionable;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.config.YesNo;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.implementations.blockentities.ICraftingMachine;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.events.GridCraftingPatternChange;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPart;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.IStorageMonitorableAccessor;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalBlockPos;
import appeng.api.util.IConfigManager;
import appeng.capabilities.Capabilities;
import appeng.core.Api;
import appeng.core.settings.TickRates;
import appeng.me.helpers.MachineSource;
import appeng.me.storage.MEMonitorIInventory;
import appeng.me.storage.MEMonitorPassThrough;
import appeng.me.storage.NullInventory;
import appeng.parts.automation.StackUpgradeInventory;
import appeng.parts.automation.UpgradeInventory;
import appeng.blockentity.inventory.AppEngInternalAEInventory;
import appeng.blockentity.inventory.AppEngInternalInventory;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.AdaptorItemHandler;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.IInventoryDestination;
import appeng.util.inv.InvOperation;
import appeng.util.item.AEItemStack;

public class DualityItemInterface
        implements IGridTickable, IStorageMonitorable, IInventoryDestination, IAEAppEngInventory,
        IConfigManagerHost, ICraftingProvider, ICraftingRequester, IUpgradeableHost {

    public static final int NUMBER_OF_STORAGE_SLOTS = 9;
    public static final int NUMBER_OF_CONFIG_SLOTS = 9;
    public static final int NUMBER_OF_PATTERN_SLOTS = 9;

    private static final Collection<Block> BAD_BLOCKS = new HashSet<>(100);
    private final IAEItemStack[] requireWork = { null, null, null, null, null, null, null, null, null };
    private final MultiCraftingTracker craftingTracker;
    private final IManagedGridNode gridProxy;
    private final IInterfaceHost iHost;
    private final IActionSource mySource;
    private final IActionSource interfaceRequestSource;
    private final ConfigManager cm = new ConfigManager(this);
    private final AppEngInternalAEInventory config = new AppEngInternalAEInventory(this, NUMBER_OF_CONFIG_SLOTS);
    private final AppEngInternalInventory storage = new AppEngInternalInventory(this, NUMBER_OF_STORAGE_SLOTS);
    private final AppEngInternalInventory patterns = new AppEngInternalInventory(this, NUMBER_OF_PATTERN_SLOTS);
    private final MEMonitorPassThrough<IAEItemStack> items = new MEMonitorPassThrough<>(
            new NullInventory<IAEItemStack>(), Api.instance().storage().getStorageChannel(IItemStorageChannel.class));
    private final MEMonitorPassThrough<IAEFluidStack> fluids = new MEMonitorPassThrough<>(
            new NullInventory<IAEFluidStack>(), Api.instance().storage().getStorageChannel(IFluidStorageChannel.class));
    private final UpgradeInventory upgrades;
    private boolean hasConfig = false;
    private int priority;
    private List<ICraftingPatternDetails> craftingList = null;
    private List<ItemStack> waitingToSend = null;
    private IMEInventory<IAEItemStack> destination;
    private int isWorking = -1;
    private final Accessor accessor = new Accessor();

    public DualityItemInterface(final IManagedGridNode gridNode, final IInterfaceHost ih, ItemStack is) {
        this.gridProxy = gridNode
                .setFlags(GridFlags.REQUIRE_CHANNEL)
                .addService(IGridTickable.class, this)
                .addService(ICraftingProvider.class, this)
                .addService(ICraftingRequester.class, this);

        this.upgrades = new StackUpgradeInventory(is, this, 1);
        this.cm.registerSetting(Settings.BLOCK, YesNo.NO);
        this.cm.registerSetting(Settings.INTERFACE_TERMINAL, YesNo.YES);

        this.iHost = ih;
        this.craftingTracker = new MultiCraftingTracker(this, 9);

        this.mySource = new MachineSource(this);
        this.fluids.setChangeSource(this.mySource);
        this.items.setChangeSource(this.mySource);

        this.interfaceRequestSource = new InterfaceRequestSource(this);
    }

    @Override
    public void saveChanges() {
        this.iHost.saveChanges();
    }

    @Override
    public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc,
            final ItemStack removed, final ItemStack added) {
        if (this.isWorking == slot) {
            return;
        }

        if (inv == this.config) {
            this.readConfig();
        } else if (inv == this.patterns && (!removed.isEmpty() || !added.isEmpty())) {
            this.updateCraftingList();
        } else if (inv == this.storage && slot >= 0) {
            final boolean had = this.hasWorkToDo();

            this.updatePlan(slot);

            final boolean now = this.hasWorkToDo();

            if (had != now) {
                gridProxy.ifPresent((grid, node) -> {
                    if (now) {
                        grid.getTickManager().alertDevice(node);
                    } else {
                        grid.getTickManager().sleepDevice(node);
                    }
                });
            }
        }
    }

    @Override
    public boolean isRemote() {
        Level level = this.iHost.getBlockEntity().getLevel();
        return level == null || level.isClientSide();
    }

    public void writeToNBT(final CompoundTag data) {
        this.config.writeToNBT(data, "config");
        this.patterns.writeToNBT(data, "patterns");
        this.storage.writeToNBT(data, "storage");
        this.upgrades.writeToNBT(data, "upgrades");
        this.cm.writeToNBT(data);
        this.craftingTracker.writeToNBT(data);
        data.putInt("priority", this.priority);

        final ListTag waitingToSend = new ListTag();
        if (this.waitingToSend != null) {
            for (final ItemStack is : this.waitingToSend) {
                final CompoundTag item = new CompoundTag();
                is.save(item);
                waitingToSend.add(item);
            }
        }
        data.put("waitingToSend", waitingToSend);
    }

    public void readFromNBT(final CompoundTag data) {
        this.waitingToSend = null;
        final ListTag waitingList = data.getList("waitingToSend", 10);
        if (waitingList != null) {
            for (int x = 0; x < waitingList.size(); x++) {
                final CompoundTag c = waitingList.getCompound(x);
                if (c != null) {
                    final ItemStack is = ItemStack.of(c);
                    this.addToSendList(is);
                }
            }
        }

        this.craftingTracker.readFromNBT(data);
        this.upgrades.readFromNBT(data, "upgrades");
        this.config.readFromNBT(data, "config");
        this.patterns.readFromNBT(data, "patterns");
        this.storage.readFromNBT(data, "storage");
        this.priority = data.getInt("priority");
        this.cm.readFromNBT(data);
        this.readConfig();
        this.updateCraftingList();
    }

    private void addToSendList(final ItemStack is) {
        if (is.isEmpty()) {
            return;
        }

        if (this.waitingToSend == null) {
            this.waitingToSend = new ArrayList<>();
        }

        this.waitingToSend.add(is);

        gridProxy.ifPresent((grid, node) -> {
            grid.getTickManager().wakeDevice(node);
        });
    }

    private void readConfig() {
        this.hasConfig = false;

        for (final ItemStack p : this.config) {
            if (!p.isEmpty()) {
                this.hasConfig = true;
                break;
            }
        }

        final boolean had = this.hasWorkToDo();

        for (int x = 0; x < NUMBER_OF_CONFIG_SLOTS; x++) {
            this.updatePlan(x);
        }

        final boolean has = this.hasWorkToDo();

        if (had != has) {
            gridProxy.ifPresent((grid, node) -> {
                if (has) {
                    grid.getTickManager().alertDevice(node);
                } else {
                    grid.getTickManager().sleepDevice(node);
                }
            });
        }

        this.notifyNeighbors();
    }

    private void updateCraftingList() {
        final Boolean[] accountedFor = { false, false, false, false, false, false, false, false, false }; // 9...

        assert accountedFor.length == this.patterns.getSlots();

        if (!this.gridProxy.isReady()) {
            return;
        }

        if (this.craftingList != null) {
            final Iterator<ICraftingPatternDetails> i = this.craftingList.iterator();
            while (i.hasNext()) {
                final ICraftingPatternDetails details = i.next();
                boolean found = false;

                for (int x = 0; x < accountedFor.length; x++) {
                    final ItemStack is = this.patterns.getStackInSlot(x);
                    if (details.getPattern() == is) {
                        accountedFor[x] = found = true;
                    }
                }

                if (!found) {
                    i.remove();
                }
            }
        }

        for (int x = 0; x < accountedFor.length; x++) {
            if (!accountedFor[x]) {
                this.addToCraftingList(this.patterns.getStackInSlot(x));
            }
        }

        this.gridProxy.ifPresent((grid, node) -> grid.postEvent(new GridCraftingPatternChange(this, node)));
    }

    private boolean hasWorkToDo() {
        if (this.hasItemsToSend()) {
            return true;
        } else {
            for (final IAEItemStack requiredWork : this.requireWork) {
                if (requiredWork != null) {
                    return true;
                }
            }

            return false;
        }
    }

    private void updatePlan(final int slot) {
        IAEItemStack req = this.config.getAEStackInSlot(slot);
        if (req != null && req.getStackSize() <= 0) {
            this.config.setStackInSlot(slot, ItemStack.EMPTY);
            req = null;
        }

        final ItemStack stored = this.storage.getStackInSlot(slot);

        if (req == null && !stored.isEmpty()) {
            final IAEItemStack work = Api.instance().storage().getStorageChannel(IItemStorageChannel.class)
                    .createStack(stored);
            this.requireWork[slot] = work.setStackSize(-work.getStackSize());
            return;
        } else if (req != null) {
            if (stored.isEmpty()) // need to add stuff!
            {
                this.requireWork[slot] = req.copy();
                return;
            } else if (req.isSameType(stored)) // same type ( qty different? )!
            {
                if (req.getStackSize() != stored.getCount()) {
                    this.requireWork[slot] = req.copy();
                    this.requireWork[slot].setStackSize(req.getStackSize() - stored.getCount());
                    return;
                }
            } else
            // Stored != null; dispose!
            {
                final IAEItemStack work = Api.instance().storage().getStorageChannel(IItemStorageChannel.class)
                        .createStack(stored);
                this.requireWork[slot] = work.setStackSize(-work.getStackSize());
                return;
            }
        }

        // else

        this.requireWork[slot] = null;
    }

    public void notifyNeighbors() {
        if (this.gridProxy.isActive()) {
            this.gridProxy.ifPresent((grid, node) -> {
                grid.postEvent(new GridCraftingPatternChange(this, node));
                grid.getTickManager().wakeDevice(node);
            });
        }

        final BlockEntity te = this.iHost.getBlockEntity();
        if (te != null && te.getLevel() != null) {
            Platform.notifyBlocksOfNeighbors(te.getLevel(), te.getBlockPos());
        }
    }

    private void addToCraftingList(final ItemStack is) {
        final ICraftingPatternDetails details = Api.instance().crafting().decodePattern(is,
                this.iHost.getBlockEntity().getLevel());

        if (details != null) {
            if (this.craftingList == null) {
                this.craftingList = new ArrayList<>();
            }

            this.craftingList.add(details);
        }
    }

    private boolean hasItemsToSend() {
        return this.waitingToSend != null && !this.waitingToSend.isEmpty();
    }

    @Override
    public boolean canInsert(final ItemStack stack) {
        final IAEItemStack out = this.destination.injectItems(
                Api.instance().storage().getStorageChannel(IItemStorageChannel.class).createStack(stack),
                Actionable.SIMULATE, null);
        if (out == null) {
            return true;
        }
        return out.getStackSize() != stack.getCount();
        // ItemStack after = adaptor.simulateAdd( stack );
        // if ( after == null )
        // return true;
        // return after.stackSize != stack.stackSize;
    }

    public IItemHandler getConfig() {
        return this.config;
    }

    public IItemHandler getPatterns() {
        return this.patterns;
    }

    public void gridChanged() {
        var grid = gridProxy.getGrid();
        if (grid != null) {
            this.items.setInternal(grid.getStorageService()
                    .getInventory(Api.instance().storage().getStorageChannel(IItemStorageChannel.class)));
            this.fluids.setInternal(grid.getStorageService()
                    .getInventory(Api.instance().storage().getStorageChannel(IFluidStorageChannel.class)));
        } else {
            this.items.setInternal(new NullInventory<>());
            this.fluids.setInternal(new NullInventory<>());
        }

        this.notifyNeighbors();
    }

    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.SMART;
    }

    public DimensionalBlockPos getLocation() {
        return new DimensionalBlockPos(this.iHost.getBlockEntity());
    }

    public IItemHandler getInternalInventory() {
        return this.storage;
    }

    @Override
    public TickingRequest getTickingRequest(final IGridNode node) {
        return new TickingRequest(TickRates.Interface.getMin(), TickRates.Interface.getMax(), !this.hasWorkToDo(),
                true);
    }

    @Override
    public TickRateModulation tickingRequest(final IGridNode node, final int ticksSinceLastCall) {
        if (!this.gridProxy.isActive()) {
            return TickRateModulation.SLEEP;
        }

        if (this.hasItemsToSend()) {
            this.pushItemsOut(this.iHost.getTargets());
        }

        final boolean couldDoWork = this.updateStorage();
        return this.hasWorkToDo() ? couldDoWork ? TickRateModulation.URGENT : TickRateModulation.SLOWER
                : TickRateModulation.SLEEP;
    }

    private void pushItemsOut(final EnumSet<Direction> possibleDirections) {
        if (!this.hasItemsToSend()) {
            return;
        }

        final BlockEntity blockEntity = this.iHost.getBlockEntity();
        final Level w = blockEntity.getLevel();

        final Iterator<ItemStack> i = this.waitingToSend.iterator();
        while (i.hasNext()) {
            ItemStack whatToSend = i.next();

            for (final Direction s : possibleDirections) {
                final BlockEntity te = w.getBlockEntity(blockEntity.getBlockPos().relative(s));
                if (te == null) {
                    continue;
                }

                final InventoryAdaptor ad = InventoryAdaptor.getAdaptor(te, s.getOpposite());
                if (ad != null) {
                    final ItemStack result = ad.addItems(whatToSend);

                    if (result.isEmpty()) {
                        whatToSend = ItemStack.EMPTY;
                    } else {
                        whatToSend.setCount(whatToSend.getCount() - (whatToSend.getCount() - result.getCount()));
                    }

                    if (whatToSend.isEmpty()) {
                        break;
                    }
                }
            }

            if (whatToSend.isEmpty()) {
                i.remove();
            }
        }

        if (this.waitingToSend.isEmpty()) {
            this.waitingToSend = null;
        }
    }

    private boolean updateStorage() {
        boolean didSomething = false;

        for (int x = 0; x < NUMBER_OF_STORAGE_SLOTS; x++) {
            if (this.requireWork[x] != null) {
                didSomething = this.usePlan(x, this.requireWork[x]) || didSomething;
            }
        }

        return didSomething;
    }

    private boolean usePlan(final int x, final IAEItemStack itemStack) {
        final InventoryAdaptor adaptor = this.getAdaptor(x);
        this.isWorking = x;

        boolean changed = tryUsePlan(x, itemStack, adaptor);

        if (changed) {
            this.updatePlan(x);
        }

        this.isWorking = -1;
        return changed;
    }

    private boolean tryUsePlan(int slot, IAEItemStack itemStack, InventoryAdaptor adaptor) {
        var grid = gridProxy.getGrid();
        if (grid == null) {
            return false;
        }

        this.destination = grid.getStorageService()
                .getInventory(Api.instance().storage().getStorageChannel(IItemStorageChannel.class));
        var src = grid.getEnergyService();

        if (this.craftingTracker.isBusy(slot)) {
            return this.handleCrafting(slot, adaptor, itemStack);
        } else if (itemStack.getStackSize() > 0) {
            // make sure strange things didn't happen...
            if (!adaptor.simulateAdd(itemStack.createItemStack()).isEmpty()) {
                return true;
            }

            final IAEItemStack acquired = Platform.poweredExtraction(src, this.destination, itemStack,
                    this.interfaceRequestSource);
            if (acquired != null) {
                final ItemStack issue = adaptor.addItems(acquired.createItemStack());
                if (!issue.isEmpty()) {
                    throw new IllegalStateException("bad attempt at managing inventory. ( addItems )");
                }
                return true;
            } else {
                return this.handleCrafting(slot, adaptor, itemStack);
            }
        } else if (itemStack.getStackSize() < 0) {
            IAEItemStack toStore = itemStack.copy();
            toStore.setStackSize(-toStore.getStackSize());

            long diff = toStore.getStackSize();

            // make sure strange things didn't happen...
            // TODO: check if OK
            final ItemStack canExtract = adaptor.simulateRemove((int) diff, toStore.getDefinition(), null);
            if (canExtract.isEmpty() || canExtract.getCount() != diff) {
                return true;
            }

            toStore = Platform.poweredInsert(src, this.destination, toStore, this.interfaceRequestSource);

            if (toStore != null) {
                diff -= toStore.getStackSize();
            }

            if (diff != 0) {
                // extract items!
                final ItemStack removed = adaptor.removeItems((int) diff, ItemStack.EMPTY, null);
                if (removed.isEmpty() || removed.getCount() != diff) {
                    throw new IllegalStateException("bad attempt at managing inventory. ( removeItems )");
                }
                return true;
            }
        }

        // else wtf?
        return false;
    }

    private InventoryAdaptor getAdaptor(final int slot) {
        return new AdaptorItemHandler(new RangedWrapper(this.storage, slot, slot + 1));
    }

    private boolean handleCrafting(final int x, final InventoryAdaptor d, final IAEItemStack itemStack) {
        var grid = gridProxy.getGrid();
        if (grid != null && this.getInstalledUpgrades(Upgrades.CRAFTING) > 0 && itemStack != null) {
            return this.craftingTracker.handleCrafting(x, itemStack.getStackSize(), itemStack, d,
                    this.iHost.getBlockEntity().getLevel(), grid,
                    grid.getCraftingService(),
                    this.mySource);
        }

        return false;
    }

    @Override
    public int getInstalledUpgrades(final Upgrades u) {
        if (this.upgrades == null) {
            return 0;
        }
        return this.upgrades.getInstalledUpgrades(u);
    }

    @Override
    public BlockEntity getBlockEntity() {
        return (BlockEntity) (this.iHost instanceof BlockEntity ? this.iHost : null);
    }

    @Override
    public <T extends IAEStack<T>> IMEMonitor<T> getInventory(IStorageChannel<T> channel) {
        if (channel == Api.instance().storage().getStorageChannel(IItemStorageChannel.class)) {
            if (this.hasConfig()) {
                return (IMEMonitor<T>) new InterfaceInventory(this);
            }

            return (IMEMonitor<T>) this.items;
        } else if (channel == Api.instance().storage().getStorageChannel(IFluidStorageChannel.class)) {
            if (this.hasConfig()) {
                return null;
            }

            return (IMEMonitor<T>) this.fluids;
        }

        return null;
    }

    private boolean hasConfig() {
        return this.hasConfig;
    }

    @Override
    public IItemHandler getInventoryByName(final String name) {
        if (name.equals("storage")) {
            return this.storage;
        }

        if (name.equals("patterns")) {
            return this.patterns;
        }

        if (name.equals("config")) {
            return this.config;
        }

        if (name.equals("upgrades")) {
            return this.upgrades;
        }

        return null;
    }

    public IItemHandler getStorage() {
        return this.storage;
    }

    @Override
    public IConfigManager getConfigManager() {
        return this.cm;
    }

    @Override
    public void updateSetting(final IConfigManager manager, final Settings settingName, final Enum<?> newValue) {
        if (this.getInstalledUpgrades(Upgrades.CRAFTING) == 0) {
            this.cancelCrafting();
        }
        this.iHost.saveChanges();
    }

    private void cancelCrafting() {
        this.craftingTracker.cancel();
    }

    public IStorageMonitorable getMonitorable(final IActionSource src, final IStorageMonitorable myInterface) {
        if (Platform.canAccess(this.gridProxy, src)) {
            return myInterface;
        }

        final DualityItemInterface di = this;

        return new IStorageMonitorable() {

            @Override
            public <T extends IAEStack<T>> IMEMonitor<T> getInventory(IStorageChannel<T> channel) {
                if (channel == Api.instance().storage().getStorageChannel(IItemStorageChannel.class)) {
                    return (IMEMonitor<T>) new InterfaceInventory(di);
                }
                return null;
            }
        };
    }

    @Override
    public boolean pushPattern(final ICraftingPatternDetails patternDetails, final CraftingContainer table) {
        if (this.hasItemsToSend() || !this.gridProxy.isActive() || !this.craftingList.contains(patternDetails)) {
            return false;
        }

        final BlockEntity blockEntity = this.iHost.getBlockEntity();
        final Level w = blockEntity.getLevel();

        final EnumSet<Direction> possibleDirections = this.iHost.getTargets();
        for (final Direction s : possibleDirections) {
            var te = w.getBlockEntity(blockEntity.getBlockPos().relative(s));
            if (te instanceof IInterfaceHost interfaceHost) {
                if (interfaceHost.getInterfaceDuality().sameGrid(this.gridProxy.getGrid())) {
                    continue;
                }
            }

            if (te instanceof ICraftingMachine) {
                final ICraftingMachine cm = (ICraftingMachine) te;
                if (cm.acceptsPlans()) {
                    if (cm.pushPattern(patternDetails, table, s.getOpposite())) {
                        return true;
                    }
                    continue;
                }
            }

            final InventoryAdaptor ad = InventoryAdaptor.getAdaptor(te, s.getOpposite());
            if (ad != null) {
                if (this.isBlocking() && !ad.simulateRemove(1, ItemStack.EMPTY, null).isEmpty()) {
                    continue;
                }

                if (this.acceptsItems(ad, table)) {
                    for (int x = 0; x < table.getContainerSize(); x++) {
                        final ItemStack is = table.getItem(x);
                        if (!is.isEmpty()) {
                            final ItemStack added = ad.addItems(is);
                            this.addToSendList(added);
                        }
                    }
                    this.pushItemsOut(possibleDirections);
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean isBusy() {
        if (this.hasItemsToSend()) {
            return true;
        }

        boolean busy = false;

        if (this.isBlocking()) {
            final EnumSet<Direction> possibleDirections = this.iHost.getTargets();
            final BlockEntity blockEntity = this.iHost.getBlockEntity();
            final Level w = blockEntity.getLevel();

            boolean allAreBusy = true;

            for (final Direction s : possibleDirections) {
                final BlockEntity te = w.getBlockEntity(blockEntity.getBlockPos().relative(s));

                final InventoryAdaptor ad = InventoryAdaptor.getAdaptor(te, s.getOpposite());
                if (ad != null && ad.simulateRemove(1, ItemStack.EMPTY, null).isEmpty()) {
                    allAreBusy = false;
                    break;
                }
            }

            busy = allAreBusy;
        }

        return busy;
    }

    private boolean sameGrid(@Nullable IGrid grid) {
        return grid != null && grid == this.gridProxy.getGrid();
    }

    private boolean isBlocking() {
        return this.cm.getSetting(Settings.BLOCK) == YesNo.YES;
    }

    private boolean acceptsItems(final InventoryAdaptor ad, final CraftingContainer table) {
        for (int x = 0; x < table.getContainerSize(); x++) {
            final ItemStack is = table.getItem(x);
            if (is.isEmpty()) {
                continue;
            }

            if (!ad.simulateAdd(is.copy()).isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void provideCrafting(final ICraftingProviderHelper craftingTracker) {
        if (this.gridProxy.isActive() && this.craftingList != null) {
            for (final ICraftingPatternDetails details : this.craftingList) {
                details.setPriority(this.priority);
                craftingTracker.addCraftingOption(this, details);
            }
        }
    }

    public void addDrops(final List<ItemStack> drops) {
        if (this.waitingToSend != null) {
            for (final ItemStack is : this.waitingToSend) {
                if (!is.isEmpty()) {
                    drops.add(is);
                }
            }
        }

        for (final ItemStack is : this.upgrades) {
            if (!is.isEmpty()) {
                drops.add(is);
            }
        }

        for (final ItemStack is : this.storage) {
            if (!is.isEmpty()) {
                drops.add(is);
            }
        }

        for (final ItemStack is : this.patterns) {
            if (!is.isEmpty()) {
                drops.add(is);
            }
        }
    }

    public IUpgradeableHost getHost() {
        if (this.getPart() instanceof IUpgradeableHost) {
            return (IUpgradeableHost) this.getPart();
        }
        if (this.getBlockEntity() instanceof IUpgradeableHost) {
            return (IUpgradeableHost) this.getBlockEntity();
        }
        return null;
    }

    private IPart getPart() {
        return (IPart) (this.iHost instanceof IPart ? this.iHost : null);
    }

    @Override
    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        return this.craftingTracker.getRequestedJobs();
    }

    @Override
    public IAEItemStack injectCraftedItems(final ICraftingLink link, final IAEItemStack acquired,
            final Actionable mode) {
        final int slot = this.craftingTracker.getSlot(link);

        if (acquired != null && slot >= 0 && slot <= this.requireWork.length) {
            final InventoryAdaptor adaptor = this.getAdaptor(slot);

            if (mode == Actionable.SIMULATE) {
                return AEItemStack.fromItemStack(adaptor.simulateAdd(acquired.createItemStack()));
            } else {
                final IAEItemStack is = AEItemStack.fromItemStack(adaptor.addItems(acquired.createItemStack()));
                this.updatePlan(slot);
                return is;
            }
        }

        return acquired;
    }

    @Override
    public void jobStateChange(final ICraftingLink link) {
        this.craftingTracker.jobStateChange(link);
    }

    public Component getTermName() {
        final BlockEntity host = this.iHost.getBlockEntity();
        final Level hostWorld = host.getLevel();

        if (((ICustomNameObject) this.iHost).hasCustomInventoryName()) {
            return ((ICustomNameObject) this.iHost).getCustomInventoryName();
        }

        final EnumSet<Direction> possibleDirections = this.iHost.getTargets();
        for (final Direction direction : possibleDirections) {
            final BlockPos targ = host.getBlockPos().relative(direction);
            final BlockEntity directedBlockEntity = hostWorld.getBlockEntity(targ);

            if (directedBlockEntity == null) {
                continue;
            }

            if (directedBlockEntity instanceof IInterfaceHost interfaceHost) {
                if (interfaceHost.getInterfaceDuality().sameGrid(this.gridProxy.getGrid())) {
                    continue;
                }
            }

            final InventoryAdaptor adaptor = InventoryAdaptor.getAdaptor(directedBlockEntity, direction.getOpposite());
            if (directedBlockEntity instanceof ICraftingMachine || adaptor != null) {
                if (adaptor != null && !adaptor.hasSlots()) {
                    continue;
                }

                final BlockState directedBlockState = hostWorld.getBlockState(targ);
                final Block directedBlock = directedBlockState.getBlock();
                ItemStack what = new ItemStack(directedBlock, 1);
                try {
                    Vec3 from = new Vec3(host.getBlockPos().getX() + 0.5, host.getBlockPos().getY() + 0.5,
                            host.getBlockPos().getZ() + 0.5);
                    from = from.add(direction.getStepX() * 0.501, direction.getStepY() * 0.501,
                            direction.getStepZ() * 0.501);
                    final Vec3 to = from.add(direction.getStepX(), direction.getStepY(),
                            direction.getStepZ());
                    final BlockHitResult hit = null;// hostWorld.rayTraceBlocks( from, to ); //FIXME:
                    // https://github.com/MinecraftForge/MinecraftForge/pull/6708
                    if (hit != null && !BAD_BLOCKS.contains(directedBlock)
                            && hit.getBlockPos().equals(directedBlockEntity.getBlockPos())) {
                        final ItemStack g = directedBlock.getPickBlock(directedBlockState, hit, hostWorld,
                                directedBlockEntity.getBlockPos(), null);
                        if (!g.isEmpty()) {
                            what = g;
                        }
                    }
                } catch (final Throwable t) {
                    BAD_BLOCKS.add(directedBlock); // nope!
                }

                if (what.getItem() != Items.AIR) {
                    return new TranslatableComponent(what.getDescriptionId());
                }

                final Item item = Item.byBlock(directedBlock);
                if (item == Items.AIR) {
                    return new TranslatableComponent(directedBlock.getDescriptionId());
                }
            }
        }

        return new TextComponent("Nothing");
    }

    public long getSortValue() {
        final BlockEntity te = this.iHost.getBlockEntity();
        return te.getBlockPos().getZ() << 24 ^ te.getBlockPos().getX() << 8 ^ te.getBlockPos().getY();
    }

    public void initialize() {
        this.updateCraftingList();
    }

    public int getPriority() {
        return this.priority;
    }

    public void setPriority(final int newValue) {
        this.priority = newValue;
        this.iHost.saveChanges();

        this.gridProxy.ifPresent((grid, node) -> new GridCraftingPatternChange(this, node));
    }

    @SuppressWarnings("unchecked")
    public <T> LazyOptional<T> getCapability(Capability<T> capabilityClass, Direction facing) {
        if (capabilityClass == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return (LazyOptional<T>) LazyOptional.of(() -> this.storage);
        } else if (capabilityClass == Capabilities.STORAGE_MONITORABLE_ACCESSOR) {
            return (LazyOptional<T>) LazyOptional.of(() -> this.accessor);
        }
        return LazyOptional.empty();
    }

    private class InterfaceRequestSource extends MachineSource {
        private final InterfaceRequestContext context;

        public InterfaceRequestSource(IActionHost v) {
            super(v);
            this.context = new InterfaceRequestContext();
        }

        @Override
        public <T> Optional<T> context(Class<T> key) {
            if (key == InterfaceRequestContext.class) {
                return (Optional<T>) Optional.of(this.context);
            }

            return super.context(key);
        }

    }

    private class InterfaceRequestContext implements Comparable<Integer> {

        @Override
        public int compareTo(Integer o) {
            return Integer.compare(DualityItemInterface.this.priority, o);
        }
    }

    private class InterfaceInventory extends MEMonitorIInventory {

        public InterfaceInventory(final DualityItemInterface iface) {
            super(new AdaptorItemHandler(iface.storage));
            this.setActionSource(mySource);
        }

        @Override
        public IAEItemStack injectItems(final IAEItemStack input, final Actionable type, final IActionSource src) {
            final Optional<InterfaceRequestContext> context = src.context(InterfaceRequestContext.class);
            final boolean isInterface = context.isPresent();

            if (isInterface) {
                return input;
            }

            return super.injectItems(input, type, src);
        }

        @Override
        public IAEItemStack extractItems(final IAEItemStack request, final Actionable type, final IActionSource src) {
            final Optional<InterfaceRequestContext> context = src.context(InterfaceRequestContext.class);
            final boolean hasLowerOrEqualPriority = context
                    .map(c -> c.compareTo(DualityItemInterface.this.priority) <= 0)
                    .orElse(false);

            if (hasLowerOrEqualPriority) {
                return null;
            }

            return super.extractItems(request, type, src);
        }
    }

    private class Accessor implements IStorageMonitorableAccessor {

        @Nullable
        @Override
        public IStorageMonitorable getInventory(IActionSource src) {
            return DualityItemInterface.this.getMonitorable(src, DualityItemInterface.this);
        }

    }

    @Override
    @Nullable
    public IGridNode getActionableNode() {
        return gridProxy.getNode();
    }
}
