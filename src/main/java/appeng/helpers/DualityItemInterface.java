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

import java.util.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
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

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.Setting;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.config.YesNo;
import appeng.api.implementations.IUpgradeInventory;
import appeng.api.implementations.IUpgradeableObject;
import appeng.api.implementations.blockentities.ICraftingMachine;
import appeng.api.implementations.blockentities.ISegmentedInventory;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.crafting.*;
import appeng.api.networking.events.GridCraftingPatternChange;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.*;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.api.storage.data.MixedItemList;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalBlockPos;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.blockentity.inventory.AppEngInternalAEInventory;
import appeng.blockentity.inventory.AppEngInternalInventory;
import appeng.crafting.execution.GenericStackHelper;
import appeng.me.storage.ItemHandlerAdapter;
import appeng.me.storage.NullInventory;
import appeng.parts.automation.StackUpgradeInventory;
import appeng.parts.automation.UpgradeInventory;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerListener;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.AdaptorItemHandler;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;
import appeng.util.item.AEItemStack;

public class DualityItemInterface
        extends DualityInterface
        implements IAEAppEngInventory, IConfigManagerListener, ICraftingProvider, ICraftingMedium, ICraftingRequester,
        IUpgradeableObject,
        IConfigurableObject {

    public static final int NUMBER_OF_STORAGE_SLOTS = 9;
    public static final int NUMBER_OF_CONFIG_SLOTS = 9;
    public static final int NUMBER_OF_PATTERN_SLOTS = 9;

    private static final Collection<Block> BAD_BLOCKS = new HashSet<>(100);
    private final IAEItemStack[] requireWork = { null, null, null, null, null, null, null, null, null };
    private final MultiCraftingTracker craftingTracker;
    private final ConfigManager cm = new ConfigManager(this);
    private final AppEngInternalAEInventory config = new AppEngInternalAEInventory(this, NUMBER_OF_CONFIG_SLOTS);
    private final AppEngInternalInventory storage = new AppEngInternalInventory(this, NUMBER_OF_STORAGE_SLOTS);
    private final AppEngInternalInventory patterns = new AppEngInternalInventory(this, NUMBER_OF_PATTERN_SLOTS);
    @Nullable
    private InterfaceInventory localInvHandler;
    private final UpgradeInventory upgrades;
    private boolean hasConfig = false;
    private List<IPatternDetails> craftingList = null;
    private List<IAEStack> waitingToSend = null;
    private IMEInventory<IAEItemStack> destination;
    private int isWorking = -1;

    public DualityItemInterface(IManagedGridNode gridNode, final IItemInterfaceHost ih, ItemStack is) {
        super(gridNode, ih);
        gridNode.addService(ICraftingProvider.class, this)
                .addService(ICraftingRequester.class, this);

        this.upgrades = new StackUpgradeInventory(is, this, 1);
        this.cm.registerSetting(Settings.BLOCK, YesNo.NO);
        this.cm.registerSetting(Settings.INTERFACE_TERMINAL, YesNo.YES);

        this.craftingTracker = new MultiCraftingTracker(this, 9);
    }

    @Override
    public void saveChanges() {
        this.host.saveChanges();
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
                mainNode.ifPresent((grid, node) -> {
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
        Level level = this.host.getBlockEntity().getLevel();
        return level == null || level.isClientSide();
    }

    public void writeToNBT(final CompoundTag data) {
        super.writeToNBT(data);

        this.config.writeToNBT(data, "config");
        this.patterns.writeToNBT(data, "patterns");
        this.storage.writeToNBT(data, "storage");
        this.upgrades.writeToNBT(data, "upgrades");
        this.cm.writeToNBT(data);
        this.craftingTracker.writeToNBT(data);

        final ListTag waitingToSend = new ListTag();
        if (this.waitingToSend != null) {
            for (var stack : this.waitingToSend) {
                waitingToSend.add(GenericStackHelper.writeGenericStack(stack));
            }
        }
        data.put("waitingToSend", waitingToSend);
    }

    public void readFromNBT(final CompoundTag data) {
        super.readFromNBT(data);
        this.waitingToSend = null;
        final ListTag waitingList = data.getList("waitingToSend", 10);
        if (waitingList != null) {
            for (int x = 0; x < waitingList.size(); x++) {
                final CompoundTag c = waitingList.getCompound(x);
                if (c != null) {
                    this.addToSendList(GenericStackHelper.readGenericStack(c));
                }
            }
        }

        this.craftingTracker.readFromNBT(data);
        this.upgrades.readFromNBT(data, "upgrades");
        this.config.readFromNBT(data, "config");
        this.patterns.readFromNBT(data, "patterns");
        this.storage.readFromNBT(data, "storage");
        this.cm.readFromNBT(data);
        this.readConfig();
        this.updateCraftingList();
    }

    private void addToSendList(final IAEStack is) {
        if (is == null || is.getStackSize() == 0) {
            return;
        }

        if (this.waitingToSend == null) {
            this.waitingToSend = new ArrayList<>();
        }

        this.waitingToSend.add(is);

        mainNode.ifPresent((grid, node) -> {
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
            mainNode.ifPresent((grid, node) -> {
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

        if (!this.mainNode.isReady()) {
            return;
        }

        if (this.craftingList != null) {
            final Iterator<IPatternDetails> i = this.craftingList.iterator();
            while (i.hasNext()) {
                var details = i.next();
                boolean found = false;

                for (int x = 0; x < accountedFor.length; x++) {
                    final ItemStack is = this.patterns.getStackInSlot(x);
                    // TODO: used to be always false, and still is! ...
                    if (details.getDefinition() == is) {
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

        this.mainNode.ifPresent((grid, node) -> grid.postEvent(new GridCraftingPatternChange(this, node)));
    }

    @Override
    protected boolean hasWorkToDo() {
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
            var work = StorageChannels.items().createStack(stored);
            work.setStackSize(-work.getStackSize());
            this.requireWork[slot] = work;
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
                var work = StorageChannels.items().createStack(stored);
                work.setStackSize(-work.getStackSize());
                this.requireWork[slot] = work;
                return;
            }
        }

        // else

        this.requireWork[slot] = null;
    }

    public void notifyNeighbors() {
        if (this.mainNode.isActive()) {
            this.mainNode.ifPresent((grid, node) -> {
                grid.postEvent(new GridCraftingPatternChange(this, node));
                grid.getTickManager().wakeDevice(node);
            });
        }

        final BlockEntity te = this.host.getBlockEntity();
        if (te != null && te.getLevel() != null) {
            Platform.notifyBlocksOfNeighbors(te.getLevel(), te.getBlockPos());
        }
    }

    private void addToCraftingList(final ItemStack is) {
        var details = AEApi.patternDetailsHelper().decodePattern(is,
                this.host.getBlockEntity().getLevel());

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

    public IItemHandler getConfig() {
        return this.config;
    }

    public IItemHandler getPatterns() {
        return this.patterns;
    }

    public void gridChanged() {
        var grid = mainNode.getGrid();
        if (grid != null) {
            this.items.setInternal(grid.getStorageService()
                    .getInventory(StorageChannels.items()));
            this.fluids.setInternal(grid.getStorageService()
                    .getInventory(StorageChannels.fluids()));
        } else {
            this.items.setInternal(new NullInventory<>(StorageChannels.items()));
            this.fluids.setInternal(new NullInventory<>(StorageChannels.fluids()));
        }

        this.notifyNeighbors();
    }

    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.SMART;
    }

    public DimensionalBlockPos getLocation() {
        return new DimensionalBlockPos(this.host.getBlockEntity());
    }

    public IItemHandler getInternalInventory() {
        return this.storage;
    }

    private void pushItemsOut(final EnumSet<Direction> possibleDirections) {
        if (!this.hasItemsToSend()) {
            return;
        }

        final BlockEntity blockEntity = this.host.getBlockEntity();
        final Level level = blockEntity.getLevel();

        final Iterator<IAEStack> i = this.waitingToSend.iterator();
        while (i.hasNext()) {
            var whatToSend = i.next();

            for (final Direction s : possibleDirections) {
                var adjPos = blockEntity.getBlockPos().relative(s);
                final BlockEntity te = level.getBlockEntity(adjPos);
                IForeignInventory foreignInventory = whatToSend.getChannel().getForeignInventory(level, adjPos, te,
                        s.getOpposite());
                if (foreignInventory != null) {
                    var leftover = foreignInventory.injectItems(whatToSend, Actionable.MODULATE);

                    if (leftover == null || leftover.getStackSize() == 0) {
                        i.remove();
                        break;
                    } else {
                        whatToSend.setStackSize(leftover.getStackSize());
                    }
                }
            }
        }

        if (this.waitingToSend.isEmpty()) {
            this.waitingToSend = null;
        }
    }

    @Override
    protected boolean updateStorage() {
        boolean didSomething = false;

        if (this.hasItemsToSend()) {
            this.pushItemsOut(this.host.getTargets());
            didSomething = true;
        }

        for (int x = 0; x < NUMBER_OF_STORAGE_SLOTS; x++) {
            if (this.requireWork[x] != null) {
                didSomething = this.usePlan(x, this.requireWork[x]) || didSomething;
            }
        }

        return didSomething;
    }

    @Override
    protected boolean hasConfig() {
        return this.hasConfig;
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
        var grid = mainNode.getGrid();
        if (grid == null) {
            return false;
        }

        this.destination = grid.getStorageService()
                .getInventory(StorageChannels.items());
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
        var grid = mainNode.getGrid();
        if (grid != null && upgrades.getInstalledUpgrades(Upgrades.CRAFTING) > 0 && itemStack != null) {
            return this.craftingTracker.handleCrafting(x, itemStack.getStackSize(), itemStack, d,
                    this.host.getBlockEntity().getLevel(),
                    grid.getCraftingService(),
                    this.actionSource);
        }

        return false;
    }

    /**
     * Returns an ME compatible monitor for the interfaces local storage.
     */
    @Override
    protected <T extends IAEStack> IMEMonitor<T> getLocalInventory(IStorageChannel<T> channel) {
        if (channel == StorageChannels.items()) {
            if (localInvHandler == null) {
                localInvHandler = new InterfaceInventory();
            }
            return localInvHandler.cast(channel);
        }
        return null;
    }

    public IItemHandler getSubInventory(ResourceLocation id) {
        if (id.equals(ISegmentedInventory.STORAGE)) {
            return this.storage;
        } else if (id.equals(ISegmentedInventory.PATTERNS)) {
            return this.patterns;
        } else if (id.equals(ISegmentedInventory.CONFIG)) {
            return this.config;
        } else if (id.equals(ISegmentedInventory.UPGRADES)) {
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
    public void onSettingChanged(IConfigManager manager, Setting<?> setting) {
        if (upgrades.getInstalledUpgrades(Upgrades.CRAFTING) == 0) {
            this.cancelCrafting();
        }
        this.host.saveChanges();
    }

    private void cancelCrafting() {
        this.craftingTracker.cancel();
    }

    private Map<IStorageChannel<?>, IForeignInventory<?>> getAdapterSet(Level level, BlockPos pos,
            @Nullable BlockEntity blockEntity, Direction side) {
        Map<IStorageChannel<?>, IForeignInventory<?>> adapters = new HashMap<>();
        for (var chan : StorageChannels.getAll()) {
            var foreignInventory = chan.getForeignInventory(level, pos, blockEntity, side);
            if (foreignInventory != null) {
                adapters.put(chan, foreignInventory);
            }
        }
        return adapters;
    }

    private boolean isAtLeastOneAdapterBusy(Map<IStorageChannel<?>, IForeignInventory<?>> adapters) {
        for (var foreignInv : adapters.values()) {
            if (foreignInv.isBusy()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean pushPattern(final IPatternDetails patternDetails, final MixedItemList[] table) {
        if (this.hasItemsToSend() || !this.mainNode.isActive() || !this.craftingList.contains(patternDetails)) {
            return false;
        }

        final BlockEntity blockEntity = this.host.getBlockEntity();
        final Level level = blockEntity.getLevel();

        final EnumSet<Direction> possibleDirections = this.host.getTargets();
        for (final Direction s : possibleDirections) {
            var adjPos = blockEntity.getBlockPos().relative(s);
            var te = level.getBlockEntity(adjPos);
            if (te instanceof IItemInterfaceHost interfaceHost) {
                if (interfaceHost.getInterfaceDuality().sameGrid(this.mainNode.getGrid())) {
                    continue;
                }
            }

            if (te instanceof ICraftingMachine cm) {
                if (cm.acceptsPlans()) {
                    if (cm.pushPattern(patternDetails, table, s.getOpposite())) {
                        return true;
                    }
                    continue;
                }
            }

            var adapters = getAdapterSet(level, adjPos, te, s.getOpposite());
            if (this.isBlocking() && isAtLeastOneAdapterBusy(adapters)) {
                continue;
            }

            if (this.acceptsItems(adapters, table)) {
                for (var inputList : table) {
                    for (var input : inputList) {
                        IForeignInventory foreignInventory = adapters.get(input.getChannel());
                        this.addToSendList(foreignInventory.injectItems(input, Actionable.MODULATE));
                    }
                }
                this.pushItemsOut(possibleDirections);
                return true;
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
            final EnumSet<Direction> possibleDirections = this.host.getTargets();
            final BlockEntity blockEntity = this.host.getBlockEntity();
            final Level level = blockEntity.getLevel();

            boolean allAreBusy = true;

            for (final Direction s : possibleDirections) {
                final BlockEntity te = level.getBlockEntity(blockEntity.getBlockPos().relative(s));

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
        return grid != null && grid == this.mainNode.getGrid();
    }

    private boolean isBlocking() {
        return this.cm.getSetting(Settings.BLOCK) == YesNo.YES;
    }

    private boolean acceptsItems(Map<IStorageChannel<?>, IForeignInventory<?>> ad, MixedItemList[] inputs) {
        for (var inputList : inputs) {
            for (var input : inputList) {
                IForeignInventory foreignInv = ad.get(input.getChannel());
                if (foreignInv == null)
                    return false;
                var leftover = foreignInv.injectItems(input, Actionable.SIMULATE);
                if (leftover != null && leftover.getStackSize() == input.getStackSize()) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void provideCrafting(final ICraftingProviderHelper craftingTracker) {
        if (this.mainNode.isActive() && this.craftingList != null) {
            for (var details : this.craftingList) {
                craftingTracker.addCraftingOption(this, details, getPriority());
            }
        }
    }

    public void addDrops(final List<ItemStack> drops) {
        if (this.waitingToSend != null) {
            for (var stack : this.waitingToSend) {
                if (stack instanceof IAEItemStack itemStack) {
                    // TODO: check max stack size?
                    drops.add(itemStack.createItemStack());
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

    @Override
    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        return this.craftingTracker.getRequestedJobs();
    }

    @Override
    public IAEStack injectCraftedItems(final ICraftingLink link, final IAEStack stack,
                                       final Actionable mode) {
        // Cast is safe: we know we only requested items.
        var acquired = (IAEItemStack) stack;
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

        return stack;
    }

    @Override
    public void jobStateChange(final ICraftingLink link) {
        this.craftingTracker.jobStateChange(link);
    }

    public Component getTermName() {
        final BlockEntity host = this.host.getBlockEntity();
        final Level hostWorld = host.getLevel();

        if (((ICustomNameObject) this.host).hasCustomInventoryName()) {
            return ((ICustomNameObject) this.host).getCustomInventoryName();
        }

        final EnumSet<Direction> possibleDirections = this.host.getTargets();
        for (final Direction direction : possibleDirections) {
            final BlockPos targ = host.getBlockPos().relative(direction);
            final BlockEntity directedBlockEntity = hostWorld.getBlockEntity(targ);

            if (directedBlockEntity == null) {
                continue;
            }

            if (directedBlockEntity instanceof IItemInterfaceHost interfaceHost) {
                if (interfaceHost.getInterfaceDuality().sameGrid(this.mainNode.getGrid())) {
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
        final BlockEntity te = this.host.getBlockEntity();
        return te.getBlockPos().getZ() << 24 ^ te.getBlockPos().getX() << 8 ^ te.getBlockPos().getY();
    }

    public void initialize() {
        this.updateCraftingList();
    }

    public void setPriority(int priority) {
        super.setPriority(priority);
        this.mainNode.ifPresent((grid, node) -> new GridCraftingPatternChange(this, node));
    }

    public <T> LazyOptional<T> getCapability(Capability<T> capabilityClass, Direction facing) {
        if (capabilityClass == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return LazyOptional.of(() -> this.storage).cast();
        }
        return super.getCapability(capabilityClass, facing);
    }

    /**
     * An adapter that makes the interface's local storage available to an AE-compatible client, such as a storage bus.
     */
    private class InterfaceInventory extends ItemHandlerAdapter implements IMEMonitor<IAEItemStack> {

        InterfaceInventory() {
            super(storage);
            this.setActionSource(actionSource);
        }

        @Override
        public IAEItemStack injectItems(final IAEItemStack input, final Actionable type, final IActionSource src) {
            // Prevents other interfaces from injecting their items into this interface when they push
            // their local inventory into the network. This prevents items from bouncing back and forth
            // between interfaces.
            if (getRequestInterfacePriority(src).isPresent()) {
                return input;
            }

            return super.injectItems(input, type, src);
        }

        @Override
        public IAEItemStack extractItems(final IAEItemStack request, final Actionable type, final IActionSource src) {
            // Prevents interfaces of lower priority fullfilling their item stocking requests from this interface
            // Otherwise we'd see a "ping-pong" effect where two interfaces could start pulling items back and
            // forth of they wanted to stock the same item and happened to have storage buses on them.
            var requestPriority = getRequestInterfacePriority(src);
            if (requestPriority.isPresent() && requestPriority.getAsInt() <= getPriority()) {
                return null;
            }

            return super.extractItems(request, type, src);
        }

        @Override
        protected void onInjectOrExtract() {
            // Rebuild cache immediately
            this.onTick();
        }

        @Override
        public IItemList<IAEItemStack> getStorageList() {
            return getAvailableItems();
        }
    }

    @Override
    @Nullable
    public IGridNode getActionableNode() {
        return mainNode.getNode();
    }

    @Nonnull
    @Override
    public IUpgradeInventory getUpgrades() {
        return upgrades;
    }

}
