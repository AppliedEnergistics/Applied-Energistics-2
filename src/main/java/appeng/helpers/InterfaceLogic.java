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

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.config.Actionable;
import appeng.api.config.Upgrades;
import appeng.api.implementations.IUpgradeInventory;
import appeng.api.implementations.IUpgradeableObject;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.IStorageMonitorableAccessor;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalBlockPos;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.core.settings.TickRates;
import appeng.helpers.externalstorage.GenericStackFluidStorage;
import appeng.helpers.externalstorage.GenericStackInvStorage;
import appeng.helpers.externalstorage.GenericStackItemStorage;
import appeng.me.helpers.MachineSource;
import appeng.me.storage.DelegatingMEInventory;
import appeng.parts.automation.StackUpgradeInventory;
import appeng.parts.automation.UpgradeInventory;
import appeng.util.ConfigInventory;
import appeng.util.ConfigManager;
import appeng.util.Platform;
import appeng.util.inv.InternalInventoryHost;

/**
 * Contains behavior for interface blocks and parts, which is independent of the storage channel.
 */
public class InterfaceLogic implements ICraftingRequester, IUpgradeableObject, IConfigurableObject,
        InternalInventoryHost {

    public static final int NUMBER_OF_SLOTS = 9;

    @Nullable
    private InterfaceInventory localInvHandler;
    @Nullable
    private MEStorage networkStorage;

    protected final InterfaceLogicHost host;
    protected final IManagedGridNode mainNode;
    protected final IActionSource actionSource;
    protected final IActionSource interfaceRequestSource;
    private final MultiCraftingTracker craftingTracker;
    private final UpgradeInventory upgrades;
    private final IStorageMonitorableAccessor accessor = this::getMonitorable;
    private final ConfigManager cm = new ConfigManager((manager, setting) -> {
        onConfigChanged();
    });
    /**
     * Work planned by {@link #updatePlan()} to be performed by {@link #usePlan}. Positive amounts mean restocking from
     * the network is required while negative amounts mean moving to the network is required.
     */
    private final GenericStack[] plannedWork = new GenericStack[NUMBER_OF_SLOTS];
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

    /**
     * Used to expose items in the local storage of this interface to external machines.
     */
    private final GenericStackItemStorage localItemStorage;

    /**
     * Used to expose fluids in the local storage of this interface to external machines.
     */
    private final GenericStackFluidStorage localFluidStorage;

    public InterfaceLogic(IManagedGridNode gridNode, InterfaceLogicHost host, Item is) {
        this.host = host;
        this.config = ConfigInventory.configStacks(null, NUMBER_OF_SLOTS, this::readConfig);
        this.storage = ConfigInventory.storage(NUMBER_OF_SLOTS, this::updatePlan);
        this.mainNode = gridNode
                .setFlags(GridFlags.REQUIRE_CHANNEL)
                .addService(IGridTickable.class, new Ticker());
        this.actionSource = new MachineSource(mainNode::getNode);

        this.interfaceRequestSource = new InterfaceRequestSource(mainNode::getNode);

        gridNode.addService(ICraftingRequester.class, this);
        this.upgrades = new StackUpgradeInventory(is, this, 1);
        this.craftingTracker = new MultiCraftingTracker(this, 9);

        getConfig().setCapacity(AEKeyType.items(), Container.LARGE_MAX_STACK_SIZE);
        getStorage().setCapacity(AEKeyType.items(), Container.LARGE_MAX_STACK_SIZE);
        getConfig().setCapacity(AEKeyType.fluids(), 4 * AEFluidKey.AMOUNT_BUCKET);
        getStorage().setCapacity(AEKeyType.fluids(), 4 * AEFluidKey.AMOUNT_BUCKET);
        this.localItemStorage = new GenericStackItemStorage(getStorage());
        this.localFluidStorage = new GenericStackFluidStorage(getStorage());
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

    public void writeToNBT(CompoundTag tag) {
        this.config.writeToChildTag(tag, "config");
        this.storage.writeToChildTag(tag, "storage");
        this.upgrades.writeToNBT(tag, "upgrades");
        this.cm.writeToNBT(tag);
        this.craftingTracker.writeToNBT(tag);
        tag.putInt("priority", this.priority);
    }

    public void readFromNBT(CompoundTag tag) {
        this.craftingTracker.readFromNBT(tag);
        this.upgrades.readFromNBT(tag, "upgrades");
        this.config.readFromChildTag(tag, "config");
        this.storage.readFromChildTag(tag, "storage");
        this.cm.readFromNBT(tag);
        this.readConfig();
        this.priority = tag.getInt("priority");
    }

    public IStorageMonitorableAccessor getGridStorageAccessor() {
        return accessor;
    }

    private class Ticker implements IGridTickable {
        @Override
        public TickingRequest getTickingRequest(IGridNode node) {
            return new TickingRequest(TickRates.Interface, !hasWorkToDo(),
                    true);
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

        final BlockEntity te = this.host.getBlockEntity();
        if (te != null && te.getLevel() != null) {
            Platform.notifyBlocksOfNeighbors(te.getLevel(), te.getBlockPos());
        }
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

    public GenericStackInvStorage<ItemVariant> getLocalItemStorage() {
        return localItemStorage;
    }

    public GenericStackInvStorage<FluidVariant> getLocalFluidStorage() {
        return localFluidStorage;
    }

    private MEStorage getMonitorable(IActionSource src) {
        // If the given action source can access the grid, return the real inventory
        if (Platform.canAccess(mainNode, src)) {
            return getInventory();
        }

        // Otherwise, return a fallback that only exposes the local interface inventory
        return getLocalInventory();
    }

    /**
     * Gets the inventory that is exposed to an ME compatible API user if they have access to the grid this interface is
     * a part of. This is normally accessed by storage buses.
     * <p/>
     * If the interface has configured slots, it will <b>always</b> expose its local inventory instead of the grid's
     * inventory.
     */
    private MEStorage getInventory() {
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
            } else if (req.what().equals(stored.what())) {
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

            var acquired = (int) StorageHelper.poweredExtraction(energySrc, networkInv, what, amount,
                    this.interfaceRequestSource);
            if (acquired > 0) {
                var inserted = storage.insert(slot, what, acquired, Actionable.MODULATE);
                if (inserted < acquired) {
                    throw new IllegalStateException("bad attempt at managing inventory. Voided items: " + inserted);
                }
                return true;
            } else {
                return this.handleCrafting(slot, what, amount);
            }
        }

        // else wtf?
        return false;
    }

    private boolean handleCrafting(int x, AEKey key, long amount) {
        var grid = mainNode.getGrid();
        if (grid != null && upgrades.getInstalledUpgrades(Upgrades.CRAFTING) > 0 && key != null) {
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
    }

    @Override
    public void saveChanges() {
        this.host.saveChanges();
    }

    @Override
    public void onChangeInventory(InternalInventory inv, int slot, ItemStack removedStack, ItemStack newStack) {
        // Cancel crafting if the crafting card is removed
        if (inv == upgrades && upgrades.getInstalledUpgrades(Upgrades.CRAFTING) == 0) {
            this.cancelCrafting();
        }
    }

    @Override
    public boolean isClientSide() {
        Level level = this.host.getBlockEntity().getLevel();
        return level == null || level.isClientSide();
    }

    public void addDrops(List<ItemStack> drops) {
        for (var is : this.upgrades) {
            if (!is.isEmpty()) {
                drops.add(is);
            }
        }

        for (int i = 0; i < this.storage.size(); i++) {
            var stack = storage.getStack(i);
            if (stack != null && stack.what() instanceof AEItemKey itemKey) {
                drops.add(itemKey.toStack((int) stack.amount()));
            }
        }
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
            if (getRequestInterfacePriority(source).isPresent()) {
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
            if (requestPriority.isPresent() && requestPriority.getAsInt() <= getPriority()) {
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
