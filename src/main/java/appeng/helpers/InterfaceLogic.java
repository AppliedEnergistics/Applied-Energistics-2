/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import com.google.common.collect.ImmutableSet;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.Settings;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalBlockPos;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.core.definitions.AEItems;
import appeng.core.settings.TickRates;
import appeng.me.helpers.MachineSource;
import appeng.me.storage.DelegatingMEInventory;
import appeng.util.ConfigInventory;

/**
 * Contains behavior for interface blocks and parts, which is independent of the storage channel.
 */
public class InterfaceLogic implements ICraftingRequester, IUpgradeableObject, IConfigurableObject {
    @Nullable
    private InterfaceInventory localInvHandler;
    @Nullable
    private MEStorage networkStorage;

    protected final InterfaceLogicHost host;
    protected final IManagedGridNode mainNode;
    protected final IActionSource actionSource;
    protected final IActionSource interfaceRequestSource;
    private final MultiCraftingTracker craftingTracker;
    private final IUpgradeInventory upgrades;
    private final IConfigManager cm;
    /**
     * Work planned by {@link #updatePlan()} to be performed by {@link #usePlan}. Positive amounts mean restocking from
     * the network is required while negative amounts mean moving to the network is required.
     */
    private final GenericStack[] plannedWork;
    private int priority;
    /**
     * Configures what and how much to stock in this inventory.
     */
    private final ConfigInventory config;
    /**
     * True if the interface is configured to stock certain types of resources.
     */
    private boolean hasConfig = false;
    private final ConfigInventory storage;

    public InterfaceLogic(IManagedGridNode gridNode, InterfaceLogicHost host, Item is) {
        this(gridNode, host, is, 9);
    }

    public InterfaceLogic(IManagedGridNode gridNode, InterfaceLogicHost host, Item is, int slots) {
        this.host = host;
        this.config = ConfigInventory.configStacks(slots).changeListener(this::onConfigRowChanged).build();
        this.storage = ConfigInventory.storage(slots).slotFilter(this::isAllowedInStorageSlot)
                .changeListener(this::onStorageChanged).build();
        this.mainNode = gridNode
                .setFlags(GridFlags.REQUIRE_CHANNEL)
                .addService(IGridTickable.class, new Ticker());
        this.actionSource = new MachineSource(mainNode::getNode);

        this.interfaceRequestSource = new InterfaceRequestSource(mainNode::getNode);

        gridNode.addService(ICraftingRequester.class, this);
        this.upgrades = UpgradeInventories.forMachine(is, 1, this::onUpgradesChanged);
        this.craftingTracker = new MultiCraftingTracker(this, slots);
        cm = IConfigManager.builder(this::onConfigChanged)
                .registerSetting(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL)
                .build();
        this.plannedWork = new GenericStack[slots];

        getConfig().useRegisteredCapacities();
        getStorage().useRegisteredCapacities();
    }

    private boolean isAllowedInStorageSlot(int slot, AEKey what) {
        if (slot < config.size()) {
            var configured = config.getKey(slot);
            if (configured == null || configured.equals(what)) {
                return true;
            }
            if (upgrades.isInstalled(AEItems.FUZZY_CARD)) {
                var fuzzyMode = getConfigManager().getSetting(Settings.FUZZY_MODE);
                return configured.fuzzyEquals(what, fuzzyMode);
            }
            return false;
        }
        return true;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
        this.host.saveChanges();
    }

    private void readConfig() {
        this.hasConfig = !this.config.isEmpty();
        updatePlan();
        this.notifyNeighbors();
    }

    public void writeToNBT(CompoundTag tag, HolderLookup.Provider registries) {
        this.config.writeToChildTag(tag, "config", registries);
        this.storage.writeToChildTag(tag, "storage", registries);
        this.upgrades.writeToNBT(tag, "upgrades", registries);
        this.cm.writeToNBT(tag, registries);
        this.craftingTracker.writeToNBT(tag);
        tag.putInt("priority", this.priority);
    }

    public void readFromNBT(CompoundTag tag, HolderLookup.Provider registries) {
        this.craftingTracker.readFromNBT(tag);
        this.upgrades.readFromNBT(tag, "upgrades", registries);
        this.config.readFromChildTag(tag, "config", registries);
        this.storage.readFromChildTag(tag, "storage", registries);
        this.cm.readFromNBT(tag, registries);
        this.readConfig();
        this.priority = tag.getInt("priority");
    }

    private class Ticker implements IGridTickable {
        @Override
        public TickingRequest getTickingRequest(IGridNode node) {
            return new TickingRequest(TickRates.Interface, !hasWorkToDo());
        }

        @Override
        public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
            if (!mainNode.isActive()) {
                return TickRateModulation.SLEEP;
            }

            boolean couldDoWork = updateStorage();
            return hasWorkToDo() ? couldDoWork ? TickRateModulation.URGENT : TickRateModulation.SLOWER
                    : TickRateModulation.SLEEP;
        }
    }

    /**
     * If the request is for a local inventory operation of an AE interface, returns the priority of that interface.
     */
    protected final OptionalInt getRequestInterfacePriority(IActionSource src) {
        return src.context(InterfaceRequestContext.class)
                .map(ctx -> OptionalInt.of(ctx.getPriority()))
                .orElseGet(OptionalInt::empty);
    }

    protected final boolean isSameGrid(IActionSource src) {
        var otherGrid = src.machine().map(IActionHost::getActionableNode).map(IGridNode::getGrid).orElse(null);
        return otherGrid == mainNode.getGrid();
    }

    protected final boolean hasWorkToDo() {
        for (var requiredWork : this.plannedWork) {
            if (requiredWork != null) {
                return true;
            }
        }

        return false;
    }

    public void notifyNeighbors() {
        if (this.mainNode.isActive()) {
            this.mainNode.ifPresent((grid, node) -> {
                grid.getTickManager().wakeDevice(node);
            });
        }

        this.host.getBlockEntity().invalidateCapabilities();
    }

    public void gridChanged() {
        this.networkStorage = mainNode.getGrid().getStorageService().getInventory();

        this.notifyNeighbors();
    }

    @Override
    public IConfigManager getConfigManager() {
        return this.cm;
    }

    public ConfigInventory getStorage() {
        return storage;
    }

    public ConfigInventory getConfig() {
        return config;
    }

    /**
     * Gets the inventory that is exposed to an ME compatible API user if they have access to the grid this interface is
     * a part of. This is normally accessed by storage buses.
     * <p/>
     * If the interface has configured slots, it will <b>always</b> expose its local inventory instead of the grid's
     * inventory.
     */
    public MEStorage getInventory() {
        if (hasConfig) {
            return getLocalInventory();
        }

        return networkStorage;
    }

    /**
     * Returns an ME compatible monitor for the interfaces local storage.
     */
    private MEStorage getLocalInventory() {
        if (localInvHandler == null) {
            localInvHandler = new InterfaceInventory();
        }
        return localInvHandler;
    }

    private class InterfaceRequestSource extends MachineSource {
        private final InterfaceRequestContext context;

        InterfaceRequestSource(IActionHost v) {
            super(v);
            this.context = new InterfaceRequestContext();
        }

        @Override
        public <T> Optional<T> context(Class<T> key) {
            if (key == InterfaceRequestContext.class) {
                return Optional.of(key.cast(this.context));
            }

            return super.context(key);
        }
    }

    private class InterfaceRequestContext {
        public int getPriority() {
            return priority;
        }
    }

    private boolean updateStorage() {
        boolean didSomething = false;

        for (int x = 0; x < plannedWork.length; x++) {
            var work = plannedWork[x];
            if (work != null) {
                var amount = (int) work.amount();
                didSomething = this.usePlan(x, work.what(), amount) || didSomething;
            }
        }

        return didSomething;
    }

    private boolean usePlan(int x, AEKey what, int amount) {
        boolean changed = tryUsePlan(x, what, amount);

        if (changed) {
            this.updatePlan(x);
        }

        return changed;
    }

    @Override
    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        return this.craftingTracker.getRequestedJobs();
    }

    @Override
    public long insertCraftedItems(ICraftingLink link, AEKey what, long amount, Actionable mode) {
        int slot = this.craftingTracker.getSlot(link);
        return storage.insert(slot, what, amount, mode);
    }

    @Override
    public void jobStateChange(ICraftingLink link) {
        this.craftingTracker.jobStateChange(link);
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return upgrades;
    }

    @Override
    @Nullable
    public IGridNode getActionableNode() {
        return mainNode.getNode();
    }

    /**
     * Check if there's any work to do to get into the state configured by {@link #config} and wake up the machine if
     * necessary.
     */
    private void updatePlan() {
        var hadWork = this.hasWorkToDo();
        for (int x = 0; x < this.config.size(); x++) {
            this.updatePlan(x);
        }
        var hasWork = this.hasWorkToDo();

        if (hadWork != hasWork) {
            mainNode.ifPresent((grid, node) -> {
                if (hasWork) {
                    grid.getTickManager().alertDevice(node);
                } else {
                    grid.getTickManager().sleepDevice(node);
                }
            });
        }
    }

    /**
     * Compute the delta between the desired state in {@link #config} and the current contents of the local storage and
     * make a plan on what needs to be changed in {@link #plannedWork}.
     */
    private void updatePlan(int slot) {
        var req = this.config.getStack(slot);
        var stored = this.storage.getStack(slot);

        if (req == null && stored != null) {
            this.plannedWork[slot] = new GenericStack(stored.what(), -stored.amount());
        } else if (req != null) {
            if (stored == null) {
                // Nothing stored, request from network
                this.plannedWork[slot] = req;
            } else if (storedRequestEquals(req.what(), stored.what())) {
                if (req.amount() != stored.amount()) {
                    // Already correct type, but incorrect amount, equilize the difference
                    this.plannedWork[slot] = new GenericStack(req.what(), req.amount() - stored.amount());
                } else {
                    this.plannedWork[slot] = null;
                }
            } else {
                // Requested item differs from stored -> push back into storage before fulfilling request
                this.plannedWork[slot] = new GenericStack(stored.what(), -stored.amount());
            }
        } else {
            // Slot matches desired state
            this.plannedWork[slot] = null;
        }
    }

    private boolean storedRequestEquals(AEKey request, AEKey stored) {
        if (upgrades.isInstalled(AEItems.FUZZY_CARD) && request.supportsFuzzyRangeSearch()) {
            return request.fuzzyEquals(stored, cm.getSetting(Settings.FUZZY_MODE));
        } else {
            return request.equals(stored);
        }
    }

    /**
     * Execute on plan made in {@link #updatePlan(int)}
     */
    private boolean tryUsePlan(int slot, AEKey what, int amount) {
        var grid = mainNode.getGrid();
        if (grid == null) {
            return false;
        }

        var networkInv = grid.getStorageService().getInventory();
        var energySrc = grid.getEnergyService();

        // Always move out unwanted items before handling crafting or restocking
        if (amount < 0) {
            // Move from interface to network storage
            amount = -amount;

            // Make sure the storage has enough items to execute the plan
            var inSlot = storage.getStack(slot);
            if (!what.matches(inSlot) || inSlot.amount() < amount) {
                return true; // Replan
            }

            var inserted = (int) StorageHelper.poweredInsert(energySrc, networkInv, what, amount,
                    this.interfaceRequestSource);

            // Remove the items we just injected somewhere else into the network.
            if (inserted > 0) {
                storage.extract(slot, what, inserted, Actionable.MODULATE);
            }

            return inserted > 0;
        }

        if (this.craftingTracker.isBusy(slot)) {
            // We are already waiting for a crafting result for this slot
            return this.handleCrafting(slot, what, amount);
        } else if (amount > 0) {
            // Move from network into interface
            // Ensure the plan isn't outdated
            if (storage.insert(slot, what, amount, Actionable.SIMULATE) != amount) {
                return true;
            }

            // Try to pull the exact item
            if (acquireFromNetwork(energySrc, networkInv, slot, what, amount)) {
                return true;
            }

            // Try a fuzzy import from network instead if we don't have stacks in stock yet
            if (storage.getStack(slot) == null && upgrades.isInstalled(AEItems.FUZZY_CARD)) {
                FuzzyMode fuzzyMode = getConfigManager().getSetting(Settings.FUZZY_MODE);
                for (var entry : grid.getStorageService().getCachedInventory().findFuzzy(what, fuzzyMode)) {
                    // Simulate insertion first in case the stack size is different
                    long maxAmount = storage.insert(slot, entry.getKey(), amount, Actionable.SIMULATE);
                    if (acquireFromNetwork(energySrc, networkInv, slot, entry.getKey(), maxAmount)) {
                        return true;
                    }
                }
            }

            return this.handleCrafting(slot, what, amount);
        }

        // else wtf?
        return false;
    }

    /**
     * @return true if something was acquired
     */
    private boolean acquireFromNetwork(IEnergyService energySrc, MEStorage networkInv, int slot, AEKey what,
            long amount) {
        var acquired = StorageHelper.poweredExtraction(energySrc, networkInv, what, amount,
                this.interfaceRequestSource);
        if (acquired > 0) {
            var inserted = storage.insert(slot, what, acquired, Actionable.MODULATE);
            if (inserted < acquired) {
                throw new IllegalStateException("bad attempt at managing inventory. Voided items: " + inserted);
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean handleCrafting(int x, AEKey key, long amount) {
        var grid = mainNode.getGrid();
        if (grid != null && upgrades.isInstalled(AEItems.CRAFTING_CARD) && key != null) {
            return this.craftingTracker.handleCrafting(x, key, amount,
                    this.host.getBlockEntity().getLevel(),
                    grid.getCraftingService(),
                    this.actionSource);
        }

        return false;
    }

    private void cancelCrafting() {
        this.craftingTracker.cancel();
    }

    private void onConfigChanged() {
        this.host.saveChanges();
        updatePlan(); // update plan in case fuzzy mode changed
    }

    private void onUpgradesChanged() {
        this.host.saveChanges();

        if (!upgrades.isInstalled(AEItems.CRAFTING_CARD)) {
            // Cancel crafting if the crafting card is removed
            this.cancelCrafting();
        }

        // Update plan in case fuzzy card was inserted or removed
        updatePlan();
    }

    private void onConfigRowChanged() {
        this.host.saveChanges();
        this.readConfig();
    }

    private void onStorageChanged() {
        this.host.saveChanges();
        this.updatePlan();
    }

    public void addDrops(List<ItemStack> drops) {
        for (var is : this.upgrades) {
            if (!is.isEmpty()) {
                drops.add(is);
            }
        }

        for (int i = 0; i < this.storage.size(); i++) {
            var stack = storage.getStack(i);

            if (stack != null) {
                stack.what().addDrops(stack.amount(), drops, this.host.getBlockEntity().getLevel(),
                        this.host.getBlockEntity().getBlockPos());
            }
        }
    }

    public void clearContent() {
        this.upgrades.clear();
        this.storage.clear();
    }

    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.SMART;
    }

    public DimensionalBlockPos getLocation() {
        return new DimensionalBlockPos(this.host.getBlockEntity());
    }

    /**
     * An adapter that wraps access to the interface's local storage behind an action source check, to respect interface
     * priorities when they are attached to a storage bus.
     */
    private class InterfaceInventory extends DelegatingMEInventory {
        InterfaceInventory() {
            super(storage);
        }

        @Override
        public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
            // Prevents other interfaces from injecting their items into this interface when they push
            // their local inventory into the network. This prevents items from bouncing back and forth
            // between interfaces.
            if (getRequestInterfacePriority(source).isPresent() && isSameGrid(source)) {
                return 0;
            }

            return super.insert(what, amount, mode, source);
        }

        @Override
        public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
            // Prevents interfaces of lower priority fullfilling their item stocking requests from this interface
            // Otherwise we'd see a "ping-pong" effect where two interfaces could start pulling items back and
            // forth of they wanted to stock the same item and happened to have storage buses on them.
            var requestPriority = getRequestInterfacePriority(source);
            if (requestPriority.isPresent() && requestPriority.getAsInt() <= getPriority() && isSameGrid(source)) {
                return 0;
            }

            return super.extract(what, amount, mode, source);
        }

        @Override
        public Component getDescription() {
            return host.getMainMenuIcon().getHoverName();
        }
    }
}
