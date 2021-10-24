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

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;

import appeng.api.config.Actionable;
import appeng.api.config.Setting;
import appeng.api.config.Upgrades;
import appeng.api.implementations.IUpgradeInventory;
import appeng.api.implementations.IUpgradeableObject;
import appeng.api.inventories.ISegmentedInventory;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.StorageHelper;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackList;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalBlockPos;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.me.storage.ItemHandlerAdapter;
import appeng.me.storage.NullInventory;
import appeng.parts.automation.StackUpgradeInventory;
import appeng.parts.automation.UpgradeInventory;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerListener;
import appeng.util.Platform;
import appeng.util.inv.AppEngInternalAEInventory;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import appeng.util.item.AEItemStack;

public class DualityItemInterface
        extends DualityInterface
        implements InternalInventoryHost, IConfigManagerListener, ICraftingRequester, IUpgradeableObject,
        IConfigurableObject {

    public static final int NUMBER_OF_STORAGE_SLOTS = 9;
    public static final int NUMBER_OF_CONFIG_SLOTS = 9;

    private final IAEItemStack[] requireWork = { null, null, null, null, null, null, null, null, null };
    private final MultiCraftingTracker craftingTracker;
    private final ConfigManager cm = new ConfigManager(this);
    private final AppEngInternalAEInventory config = new AppEngInternalAEInventory(this, NUMBER_OF_CONFIG_SLOTS);
    private final AppEngInternalInventory storage = new AppEngInternalInventory(this, NUMBER_OF_STORAGE_SLOTS);
    @Nullable
    private InterfaceInventory localInvHandler;
    private final UpgradeInventory upgrades;
    private boolean hasConfig = false;
    private IMEInventory<IAEItemStack> destination;
    private int isWorking = -1;

    public DualityItemInterface(IManagedGridNode gridNode, final IItemInterfaceHost ih, ItemStack is) {
        super(gridNode, ih);
        gridNode.addService(ICraftingRequester.class, this);

        this.upgrades = new StackUpgradeInventory(is, this, 1);

        this.craftingTracker = new MultiCraftingTracker(this, 9);
    }

    @Override
    public void saveChanges() {
        this.host.saveChanges();
    }

    @Override
    public void onChangeInventory(final InternalInventory inv, final int slot,
            final ItemStack removed, final ItemStack added) {
        if (this.isWorking == slot) {
            return;
        }

        if (inv == this.config) {
            this.readConfig();
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

    @Override
    public void writeToNBT(final CompoundTag data) {
        super.writeToNBT(data);

        this.config.writeToNBT(data, "config");
        this.storage.writeToNBT(data, "storage");
        this.upgrades.writeToNBT(data, "upgrades");
        this.cm.writeToNBT(data);
        this.craftingTracker.writeToNBT(data);
    }

    @Override
    public void readFromNBT(final CompoundTag data) {
        super.readFromNBT(data);

        this.craftingTracker.readFromNBT(data);
        this.upgrades.readFromNBT(data, "upgrades");
        this.config.readFromNBT(data, "config");
        this.storage.readFromNBT(data, "storage");
        this.cm.readFromNBT(data);
        this.readConfig();
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

    @Override
    protected boolean hasWorkToDo() {
        for (final IAEItemStack requiredWork : this.requireWork) {
            if (requiredWork != null) {
                return true;
            }
        }

        return false;
    }

    private void updatePlan(final int slot) {
        IAEItemStack req = this.config.getAEStackInSlot(slot);
        if (req != null && req.getStackSize() <= 0) {
            this.config.setItemDirect(slot, ItemStack.EMPTY);
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
                grid.getTickManager().wakeDevice(node);
            });
        }

        final BlockEntity te = this.host.getBlockEntity();
        if (te != null && te.getLevel() != null) {
            Platform.notifyBlocksOfNeighbors(te.getLevel(), te.getBlockPos());
        }
    }

    public InternalInventory getConfig() {
        return this.config;
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

    public InternalInventory getInternalInventory() {
        return this.storage;
    }

    @Override
    protected boolean updateStorage() {
        boolean didSomething = false;

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
        this.isWorking = x;

        boolean changed = tryUsePlan(x, itemStack);

        if (changed) {
            this.updatePlan(x);
        }

        this.isWorking = -1;
        return changed;
    }

    private boolean tryUsePlan(int slot, IAEItemStack itemStack) {
        var grid = mainNode.getGrid();
        if (grid == null) {
            return false;
        }

        this.destination = grid.getStorageService()
                .getInventory(StorageChannels.items());
        var src = grid.getEnergyService();

        if (this.craftingTracker.isBusy(slot)) {
            return this.handleCrafting(slot, storage.getSlotInv(slot), itemStack);
        } else if (itemStack.getStackSize() > 0) {
            // make sure strange things didn't happen...
            if (!storage.insertItem(slot, itemStack.createItemStack(), true).isEmpty()) {
                return true;
            }

            var acquired = StorageHelper.poweredExtraction(src, this.destination, itemStack,
                    this.interfaceRequestSource);
            if (acquired != null) {
                var overflow = storage.insertItem(slot, acquired.createItemStack(), false);
                if (!overflow.isEmpty()) {
                    throw new IllegalStateException("bad attempt at managing inventory. ( addItems )");
                }
                return true;
            } else {
                return this.handleCrafting(slot, storage.getSlotInv(slot), itemStack);
            }
        } else if (itemStack.getStackSize() < 0) {
            var toStore = itemStack.copy();
            toStore.setStackSize(-toStore.getStackSize());

            long diff = toStore.getStackSize();

            // Make sure the storage has enough items to execute the plan
            var inSlot = storage.getStackInSlot(slot);
            if (!ItemStack.isSameItemSameTags(itemStack.getDefinition(), inSlot) || inSlot.getCount() < diff) {
                return true;
            }

            var remainder = StorageHelper.poweredInsert(src, this.destination, toStore, this.interfaceRequestSource);

            // Remove the items we just injected somewhere else into the network.
            int toExtract = (int) (diff - IAEStack.getStackSizeOrZero(remainder));
            storage.getSlotInv(slot).removeItems(toExtract, ItemStack.EMPTY, null);

            return toExtract > 0;
        }

        // else wtf?
        return false;
    }

    private boolean handleCrafting(final int x, InternalInventory sink, final IAEItemStack itemStack) {
        var grid = mainNode.getGrid();
        if (grid != null && upgrades.getInstalledUpgrades(Upgrades.CRAFTING) > 0 && itemStack != null) {
            return this.craftingTracker.handleCrafting(x, itemStack.getStackSize(), itemStack, sink,
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

    public InternalInventory getSubInventory(ResourceLocation id) {
        if (id.equals(ISegmentedInventory.STORAGE)) {
            return this.storage;
        } else if (id.equals(ISegmentedInventory.CONFIG)) {
            return this.config;
        } else if (id.equals(ISegmentedInventory.UPGRADES)) {
            return this.upgrades;
        }

        return null;
    }

    public InternalInventory getStorage() {
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

    public void addDrops(final List<ItemStack> drops) {
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
            var storageSlot = this.storage.getSlotInv(slot);

            if (mode == Actionable.SIMULATE) {
                return AEItemStack.fromItemStack(storageSlot.simulateAdd(acquired.createItemStack()));
            } else {
                final IAEItemStack is = AEItemStack.fromItemStack(storageSlot.addItems(acquired.createItemStack()));
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

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capabilityClass, Direction facing) {
        if (capabilityClass == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return LazyOptional.of(this.storage::toItemHandler).cast();
        }
        return super.getCapability(capabilityClass, facing);
    }

    /**
     * An adapter that makes the interface's local storage available to an AE-compatible client, such as a storage bus.
     */
    private class InterfaceInventory extends ItemHandlerAdapter implements IMEMonitor<IAEItemStack> {

        InterfaceInventory() {
            super(storage.toItemHandler());
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
        public IAEStackList<IAEItemStack> getStorageList() {
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
