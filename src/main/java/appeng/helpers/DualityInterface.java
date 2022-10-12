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


import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.config.YesNo;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.implementations.tiles.ICraftingMachine;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPart;
import appeng.api.storage.*;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IConfigManager;
import appeng.capabilities.Capabilities;
import appeng.core.settings.TickRates;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.MachineSource;
import appeng.me.storage.MEMonitorIInventory;
import appeng.me.storage.MEMonitorPassThrough;
import appeng.me.storage.NullInventory;
import appeng.parts.automation.StackUpgradeInventory;
import appeng.parts.automation.UpgradeInventory;
import appeng.parts.misc.PartInterface;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.networking.TileCableBus;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.*;
import appeng.util.item.AEItemStack;
import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Ints;
import de.ellpeck.actuallyadditions.api.tile.IPhantomTile;
import gregtech.api.block.machines.BlockMachine;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.RangedWrapper;

import javax.annotation.Nullable;
import java.util.*;


public class DualityInterface implements IGridTickable, IStorageMonitorable, IInventoryDestination, IAEAppEngInventory, IConfigManagerHost, ICraftingProvider, IUpgradeableHost {
    public static final int NUMBER_OF_STORAGE_SLOTS = 9;
    public static final int NUMBER_OF_CONFIG_SLOTS = 9;
    public static final int NUMBER_OF_PATTERN_SLOTS = 36;

    private static final Collection<Block> BAD_BLOCKS = new HashSet<>(100);
    private final IAEItemStack[] requireWork = {null, null, null, null, null, null, null, null, null};
    private final MultiCraftingTracker craftingTracker;
    private final AENetworkProxy gridProxy;
    private final IInterfaceHost iHost;
    private final IActionSource mySource;
    private final IActionSource interfaceRequestSource;
    private final ConfigManager cm = new ConfigManager(this);
    private final AppEngInternalAEInventory config = new AppEngInternalAEInventory(this, NUMBER_OF_CONFIG_SLOTS, 512);
    private final AppEngInternalInventory storage = new AppEngInternalInventory(this, NUMBER_OF_STORAGE_SLOTS, 512);
    private final AppEngInternalInventory patterns = new AppEngInternalInventory(this, NUMBER_OF_PATTERN_SLOTS);
    private final MEMonitorPassThrough<IAEItemStack> items = new MEMonitorPassThrough<>(new NullInventory<IAEItemStack>(), AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class));
    private final MEMonitorPassThrough<IAEFluidStack> fluids = new MEMonitorPassThrough<>(new NullInventory<IAEFluidStack>(), AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class));
    private final UpgradeInventory upgrades;
    private final Accessor accessor = new Accessor();
    private boolean hasConfig = false;
    private int priority;
    private List<ICraftingPatternDetails> craftingList = null;
    private List<ItemStack> waitingToSend = null;
    private IMEInventory<IAEItemStack> destination;
    private int isWorking = -1;
    private EnumSet<EnumFacing> visitedFaces = EnumSet.noneOf(EnumFacing.class);
    private EnumMap<EnumFacing, List<ItemStack>> waitingToSendFacing = new EnumMap<>(EnumFacing.class);
    private boolean resetConfigCache = true;
    private IMEMonitor<IAEItemStack> configCachedHandler;

    public DualityInterface(final AENetworkProxy networkProxy, final IInterfaceHost ih) {
        this.gridProxy = networkProxy;
        this.gridProxy.setFlags(GridFlags.REQUIRE_CHANNEL);

        this.upgrades = new StackUpgradeInventory(this.gridProxy.getMachineRepresentation(), this, 4);
        this.cm.registerSetting(Settings.BLOCK, YesNo.NO);
        this.cm.registerSetting(Settings.INTERFACE_TERMINAL, YesNo.YES);

        this.iHost = ih;
        this.craftingTracker = new MultiCraftingTracker(this.iHost, 9);

        final MachineSource actionSource = new MachineSource(this.iHost);
        this.mySource = actionSource;
        this.fluids.setChangeSource(actionSource);
        this.items.setChangeSource(actionSource);

        this.interfaceRequestSource = new InterfaceRequestSource(this.iHost);
    }

    private static boolean invIsCustomBlocking(BlockingInventoryAdaptor inv) {
        return (inv.containsBlockingItems());
    }

    @Override
    public void saveChanges() {
        this.iHost.saveChanges();
    }

    @Override
    public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc, final ItemStack removed, final ItemStack added) {
        if (this.isWorking == slot) {
            return;
        }
        if (inv == this.config && (!removed.isEmpty() || !added.isEmpty())) {
            boolean cfg = hasConfig();
            this.readConfig();
            if (cfg != hasConfig) {
                resetConfigCache = true;
                this.notifyNeighbors();
            }
        } else if (inv == this.patterns && (!removed.isEmpty() || !added.isEmpty())) {
            this.updateCraftingList();
        } else if (inv == this.storage && slot >= 0) {
            final boolean had = this.hasWorkToDo();

            this.updatePlan(slot);

            final boolean now = this.hasWorkToDo();

            if (had != now) {
                try {
                    if (now) {
                        this.gridProxy.getTick().alertDevice(this.gridProxy.getNode());
                    } else {
                        this.gridProxy.getTick().sleepDevice(this.gridProxy.getNode());
                    }
                } catch (final GridAccessException e) {
                    // :P
                }
            }
        }
    }

    public void writeToNBT(final NBTTagCompound data) {
        this.config.writeToNBT(data, "config");
        this.patterns.writeToNBT(data, "patterns");
        this.storage.writeToNBT(data, "storage");
        this.upgrades.writeToNBT(data, "upgrades");
        this.cm.writeToNBT(data);
        this.craftingTracker.writeToNBT(data);
        data.setInteger("priority", this.priority);

        final NBTTagList waitingToSend = new NBTTagList();
        if (this.waitingToSend != null) {
            for (final ItemStack is : this.waitingToSend) {
                final NBTTagCompound item = new NBTTagCompound();
                is.writeToNBT(item);
                waitingToSend.appendTag(item);
            }
        }
        data.setTag("waitingToSend", waitingToSend);

        NBTTagCompound sidedWaitList = new NBTTagCompound();

        if (this.waitingToSendFacing != null) {
            for (EnumFacing s : this.iHost.getTargets()) {
                NBTTagList waitingListSided = new NBTTagList();
                if (this.waitingToSendFacing.containsKey(s)) {
                    for (final ItemStack is : this.waitingToSendFacing.get(s)) {
                        final NBTTagCompound item = new NBTTagCompound();
                        is.writeToNBT(item);
                        waitingListSided.appendTag(item);
                    }
                    sidedWaitList.setTag(s.name(), waitingListSided);
                }
            }
        }
        data.setTag("sidedWaitList", sidedWaitList);
    }

    public void readFromNBT(final NBTTagCompound data) {
        this.waitingToSend = null;
        final NBTTagList waitingList = data.getTagList("waitingToSend", 10);
        if (waitingList != null) {
            for (int x = 0; x < waitingList.tagCount(); x++) {
                final NBTTagCompound c = waitingList.getCompoundTagAt(x);
                if (c != null) {
                    final ItemStack is = new ItemStack(c);
                    this.addToSendList(is);
                }
            }
        }

        this.waitingToSendFacing = null;
        final NBTTagCompound waitingListSided = data.getCompoundTag("sidedWaitList");

        for (EnumFacing s : EnumFacing.values()) {
            if (waitingListSided.hasKey(s.name())) {
                NBTTagList w = waitingListSided.getTagList(s.name(), 10);
                for (int x = 0; x < w.tagCount(); x++) {
                    final NBTTagCompound c = w.getCompoundTagAt(x);
                    if (c != null) {
                        final ItemStack is = new ItemStack(c);
                        this.addToSendListFacing(is, EnumFacing.getFront(s.getIndex()));
                    }
                }
            }
        }

        this.craftingTracker.readFromNBT(data);

        // fix upgrade slot size mismatch
        NBTTagCompound up = data.getCompoundTag("upgrades");
        if (up.hasKey("Size") && up.getInteger("Size") != this.upgrades.getSlots()) {
            up.setInteger("Size", this.upgrades.getSlots());
            this.upgrades.writeToNBT(up, "upgrades");
        }

        this.upgrades.readFromNBT(data, "upgrades");
        this.config.readFromNBT(data, "config");

        NBTTagCompound pa = data.getCompoundTag("patterns");
        if (pa.hasKey("Size") && pa.getInteger("Size") != this.patterns.getSlots()) {
            pa.setInteger("Size", this.patterns.getSlots());
            this.upgrades.writeToNBT(pa, "patterns");
        }

        this.patterns.readFromNBT(data, "patterns");
        this.storage.readFromNBT(data, "storage");
        this.priority = data.getInteger("priority");
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

        try {
            this.gridProxy.getTick().wakeDevice(this.gridProxy.getNode());
        } catch (final GridAccessException e) {
            // :P
        }
    }

    private void addToSendListFacing(final ItemStack is, EnumFacing f) {
        if (is.isEmpty()) {
            return;
        }
        if (this.waitingToSendFacing == null) {
            this.waitingToSendFacing = new EnumMap<>(EnumFacing.class);
        }

        this.waitingToSendFacing.computeIfAbsent(f, k -> new ArrayList<>());

        this.waitingToSendFacing.get(f).add(is);

        try {
            this.gridProxy.getTick().wakeDevice(this.gridProxy.getNode());
        } catch (final GridAccessException e) {
            // :P
        }
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
            try {
                if (has) {
                    this.gridProxy.getTick().alertDevice(this.gridProxy.getNode());
                } else {
                    this.gridProxy.getTick().sleepDevice(this.gridProxy.getNode());
                }
            } catch (final GridAccessException e) {
                // :P
            }
        }
        this.notifyNeighbors();
    }

    private void updateCraftingList() {
        final Boolean[] accountedFor = new Boolean[this.patterns.getSlots()];
        Arrays.fill(accountedFor, false);

        if (!this.gridProxy.isReady()) {
            return;
        }

        boolean removed = false;

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
                    removed = true;
                    i.remove();
                }
            }
        }

        boolean newPattern = false;

        for (int x = 0; x < accountedFor.length; x++) {
            if (!accountedFor[x]) {
                newPattern = true;
                this.addToCraftingList(this.patterns.getStackInSlot(x));
            }
        }
        try {
            this.gridProxy.getGrid().postEvent(new MENetworkCraftingPatternChange(this, this.gridProxy.getNode()));
        } catch (GridAccessException e) {
            e.printStackTrace();
        }
    }

    private boolean hasWorkToDo() {

        if (hasItemsToSend()) {
            return true;
        }

        if (hasItemsToSendFacing()) {
            return true;
        }

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
            this.config.setStackInSlot(slot, ItemStack.EMPTY);
            req = null;
        }

        final ItemStack stored = this.storage.getStackInSlot(slot);

        if (req == null && !stored.isEmpty()) {
            final IAEItemStack work = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createStack(stored);
            this.requireWork[slot] = work.setStackSize(-work.getStackSize());
            return;
        } else if (req != null) {
            if (stored.isEmpty()) // need to add stuff!
            {
                this.requireWork[slot] = req.copy();
                return;
            } else if (req.isSameType(stored)) // same type and quantity  )!
            {
                if (req.getStackSize() == stored.getCount()) {
                    this.requireWork[slot] = null;
                } else                                // same type ( qty different? )!
                {
                    this.requireWork[slot] = req.copy();
                    this.requireWork[slot].setStackSize(req.getStackSize() - stored.getCount());
                }
                return;
            } else
            // Stored != null; dispose!
            {
                final IAEItemStack work = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createStack(stored);
                this.requireWork[slot] = work.setStackSize(-work.getStackSize());
                return;
            }
        }

        // else

        this.requireWork[slot] = null;
    }

    public void notifyNeighbors() {
        if (this.gridProxy.isActive()) {
            try {
                this.gridProxy.getGrid().postEvent(new MENetworkCraftingPatternChange(this, this.gridProxy.getNode()));
                this.gridProxy.getTick().wakeDevice(this.gridProxy.getNode());
            } catch (final GridAccessException e) {
                // :P
            }
        }

        final TileEntity te = this.iHost.getTileEntity();
        if (te != null && te.getWorld() != null) {
            Platform.notifyBlocksOfNeighbors(te.getWorld(), te.getPos());
        }
    }

    private void addToCraftingList(final ItemStack is) {
        if (is.isEmpty()) {
            return;
        }

        if (is.getItem() instanceof ICraftingPatternItem) {
            final ICraftingPatternItem cpi = (ICraftingPatternItem) is.getItem();
            final ICraftingPatternDetails details = cpi.getPatternForItem(is, this.iHost.getTileEntity().getWorld());

            if (details != null) {
                if (this.craftingList == null) {
                    this.craftingList = new ArrayList<>();
                }

                this.craftingList.add(details);
            }
        }
    }

    private boolean hasItemsToSend() {
        return this.waitingToSend != null && !this.waitingToSend.isEmpty();
    }

    private boolean hasItemsToSendFacing() {
        if (waitingToSendFacing != null) {
            for (EnumFacing enumFacing : waitingToSendFacing.keySet()) {
                if (!waitingToSendFacing.get(enumFacing).isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    public void dropExcessPatterns() {
        IItemHandler patterns = getPatterns();

        List<ItemStack> dropList = new ArrayList<>();
        for (int invSlot = 0; invSlot < patterns.getSlots(); invSlot++) {
            if (invSlot > 8 + this.getInstalledUpgrades(Upgrades.PATTERN_EXPANSION) * 9) {
                ItemStack is = patterns.getStackInSlot(invSlot);
                if (is.isEmpty()) {
                    continue;
                }
                dropList.add(patterns.extractItem(invSlot, Integer.MAX_VALUE, false));
            }
        }
        if (dropList.size() > 0) {
            World world = this.getLocation().getWorld();
            BlockPos blockPos = this.getLocation().getPos();
            Platform.spawnDrops(world, blockPos, dropList);
        }

        this.gridProxy.setIdlePowerUsage(Math.pow(4, (this.getInstalledUpgrades(Upgrades.PATTERN_EXPANSION))));
    }

    @Override
    public boolean canInsert(final ItemStack stack) {
        final IAEItemStack out = this.destination.injectItems(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createStack(stack), Actionable.SIMULATE, null);
        if (out == null) {
            return true;
        }
        return out.getStackSize() != stack.getCount();
    }

    public IItemHandler getConfig() {
        return this.config;
    }

    public IItemHandler getPatterns() {
        return this.patterns;
    }

    public void gridChanged() {
        try {
            this.items.setInternal(this.gridProxy.getStorage().getInventory(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class)));
            this.fluids.setInternal(this.gridProxy.getStorage().getInventory(AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class)));
        } catch (final GridAccessException gae) {
            this.items.setInternal(new NullInventory<IAEItemStack>());
            this.fluids.setInternal(new NullInventory<IAEFluidStack>());
        }

        this.notifyNeighbors();
    }

    public AECableType getCableConnectionType(final AEPartLocation dir) {
        return AECableType.SMART;
    }

    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this.iHost.getTileEntity());
    }

    public IItemHandler getInternalInventory() {
        return this.storage;
    }

    @Override
    public TickingRequest getTickingRequest(final IGridNode node) {
        return new TickingRequest(TickRates.Interface.getMin(), TickRates.Interface.getMax(), !this.hasWorkToDo(), true);
    }

    @Override
    public TickRateModulation tickingRequest(final IGridNode node, final int ticksSinceLastCall) {
        if (!this.gridProxy.isActive()) {
            return TickRateModulation.SLEEP;
        }

        //Previous version might have items saved in this list
        //recover them
        if (this.hasItemsToSend()) {
            this.pushItemsOut(this.iHost.getTargets());
        }

        if (hasItemsToSendFacing()) {
            for (EnumFacing enumFacing : waitingToSendFacing.keySet()) {
                this.pushItemsOut(enumFacing);
            }
        }

        final boolean couldDoWork = this.updateStorage();
        return this.hasWorkToDo() ? (couldDoWork ? TickRateModulation.URGENT : TickRateModulation.SLOWER) : TickRateModulation.SLEEP;
    }

    private void pushItemsOut(final EnumSet<EnumFacing> possibleDirections) {
        if (!this.hasItemsToSend()) {
            return;
        }

        final TileEntity tile = this.iHost.getTileEntity();
        final World w = tile.getWorld();

        final Iterator<ItemStack> i = this.waitingToSend.iterator();
        while (i.hasNext()) {
            ItemStack whatToSend = i.next();

            for (final EnumFacing s : possibleDirections) {
                final TileEntity te = w.getTileEntity(tile.getPos().offset(s));
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

    private void pushItemsOut(final EnumFacing s) {
        if (!this.waitingToSendFacing.containsKey(s) || (this.waitingToSendFacing.containsKey(s) && this.waitingToSendFacing.get(s).isEmpty())) {
            return;
        }

        final TileEntity tile = this.iHost.getTileEntity();
        final World w = tile.getWorld();

        final TileEntity te = w.getTileEntity(tile.getPos().offset(s));
        if (te == null) {
            return;
        }

        if (te instanceof IInterfaceHost || (te instanceof TileCableBus && ((TileCableBus) te).getPart(s.getOpposite()) instanceof PartInterface)) {
            try {
                IInterfaceHost targetTE;
                if (te instanceof IInterfaceHost) {
                    targetTE = (IInterfaceHost) te;
                } else {
                    targetTE = (IInterfaceHost) ((TileCableBus) te).getPart(s.getOpposite());
                }

                if (!targetTE.getInterfaceDuality().sameGrid(this.gridProxy.getGrid())) {
                    IStorageMonitorableAccessor mon = te.getCapability(Capabilities.STORAGE_MONITORABLE_ACCESSOR, s.getOpposite());
                    if (mon != null) {
                        IStorageMonitorable sm = mon.getInventory(this.mySource);
                        if (sm != null && Platform.canAccess(targetTE.getInterfaceDuality().gridProxy, this.mySource)) {
                            IMEMonitor<IAEItemStack> inv = sm.getInventory(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class));
                            if (inv != null) {
                                final Iterator<ItemStack> i = this.waitingToSendFacing.get(s).iterator();
                                while (i.hasNext()) {
                                    ItemStack whatToSend = i.next();
                                    final IAEItemStack result = inv.injectItems(AEItemStack.fromItemStack(whatToSend), Actionable.MODULATE, this.mySource);
                                    if (result != null) {
                                        whatToSend.setCount((int) result.getStackSize());
                                    } else {
                                        i.remove();
                                    }
                                }
                                if (this.waitingToSendFacing.get(s).isEmpty()) {
                                    this.waitingToSendFacing.remove(s);
                                }
                            }
                        }
                    }
                } else {
                    return;
                }
            } catch (GridAccessException e) {
                throw new RuntimeException(e);
            }
            return;
        }

        final InventoryAdaptor ad = InventoryAdaptor.getAdaptor(te, s.getOpposite());

        final Iterator<ItemStack> i = this.waitingToSendFacing.get(s).iterator();
        while (i.hasNext()) {
            ItemStack whatToSend = i.next();
            if (ad != null) {
                final ItemStack result = ad.addItems(whatToSend);
                if (!result.isEmpty()) {
                    whatToSend.setCount(result.getCount());
                } else {
                    i.remove();
                }
            }
        }

        if (this.waitingToSendFacing.get(s).isEmpty()) {
            this.waitingToSendFacing.remove(s);
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

        boolean changed = false;
        try {
            this.destination = this.gridProxy.getStorage().getInventory(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class));
            final IEnergySource src = this.gridProxy.getEnergy();

            if (itemStack.getStackSize() < 0) {
                IAEItemStack toStore = itemStack.copy();
                toStore.setStackSize(-toStore.getStackSize());

                long diff = toStore.getStackSize();

                // make sure strange things didn't happen...
                // TODO: check if OK
                final ItemStack canExtract = adaptor.simulateRemove((int) diff, toStore.getDefinition(), null);
                if (canExtract.isEmpty() || canExtract.getCount() != diff) {
                    changed = true;
                    throw new GridAccessException();
                }

                toStore = Platform.poweredInsert(src, this.destination, toStore, this.interfaceRequestSource);

                if (toStore != null) {
                    diff -= toStore.getStackSize();
                }

                if (diff != 0) {
                    // extract items!
                    changed = true;
                    final ItemStack removed = adaptor.removeItems((int) diff, ItemStack.EMPTY, null);
                    if (removed.isEmpty()) {
                        throw new IllegalStateException("bad attempt at managing inventory. ( removeItems )");
                    } else if (removed.getCount() != diff) {
                        throw new IllegalStateException("bad attempt at managing inventory. ( removeItems )");
                    }
                }
            }

            if (this.craftingTracker.isBusy(x)) {
                changed = this.handleCrafting(x, adaptor, itemStack) || changed;
            } else if (itemStack.getStackSize() > 0) {
                // make sure strange things didn't happen...

                ItemStack inputStack = itemStack.getCachedItemStack(itemStack.getStackSize());

                ItemStack remaining = adaptor.simulateAdd(inputStack);

                if (!remaining.isEmpty()) {
                    itemStack.setCachedItemStack(remaining);
                    changed = true;
                    throw new GridAccessException();
                }

                IAEItemStack storedStack = this.gridProxy.getStorage().getInventory(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class)).getStorageList().findPrecise(itemStack);
                if (storedStack != null) {
                    final IAEItemStack acquired = Platform.poweredExtraction(src, this.destination, itemStack, this.interfaceRequestSource);
                    if (acquired != null) {
                        changed = true;
                        inputStack.setCount(Ints.saturatedCast(acquired.getStackSize()));
                        final ItemStack issue = adaptor.addItems(inputStack);
                        if (!issue.isEmpty()) {
                            throw new IllegalStateException("bad attempt at managing inventory. ( addItems )");
                        }
                    } else if (storedStack.isCraftable()) {
                        itemStack.setCachedItemStack(inputStack);
                        changed = this.handleCrafting(x, adaptor, itemStack) || changed;
                    }
                    if (acquired == null) {
                        itemStack.setCachedItemStack(inputStack);
                    }
                }
            }
            // else wtf?
        } catch (final GridAccessException e) {
            // :P
        }

        if (changed) {
            this.updatePlan(x);
        }

        this.isWorking = -1;
        return changed;
    }

    private InventoryAdaptor getAdaptor(final int slot) {
        return new AdaptorItemHandler(new RangedWrapper(this.storage, slot, slot + 1));
    }

    private boolean handleCrafting(final int x, final InventoryAdaptor d, final IAEItemStack itemStack) {
        try {
            if (this.getInstalledUpgrades(Upgrades.CRAFTING) > 0 && itemStack != null) {
                return this.craftingTracker.handleCrafting(x, itemStack.getStackSize(), itemStack, d, this.iHost.getTileEntity().getWorld(), this.gridProxy.getGrid(), this.gridProxy.getCrafting(), this.mySource);
            }
        } catch (final GridAccessException e) {
            // :P
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
    public TileEntity getTile() {
        return (TileEntity) (this.iHost instanceof TileEntity ? this.iHost : null);
    }

    @Override
    public <T extends IAEStack<T>> IMEMonitor<T> getInventory(IStorageChannel<T> channel) {
        if (channel == AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class)) {
            if (this.hasConfig()) {
                if (resetConfigCache) {
                    resetConfigCache = false;
                    configCachedHandler = new InterfaceInventory(this);
                }
                return (IMEMonitor<T>) configCachedHandler;
            }

            return (IMEMonitor<T>) this.items;
        } else if (channel == AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class)) {
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
    public appeng.api.util.IConfigManager getConfigManager() {
        return this.cm;
    }

    @Override
    public void updateSetting(final IConfigManager manager, final Enum settingName, final Enum newValue) {
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

        final DualityInterface di = this;

        return new IStorageMonitorable() {

            @Override
            public <T extends IAEStack<T>> IMEMonitor<T> getInventory(IStorageChannel<T> channel) {
                if (channel == AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class)) {
                    return (IMEMonitor<T>) new InterfaceInventory(di);
                }
                return null;
            }
        };
    }

    private boolean invIsBlocked(InventoryAdaptor inv) {
        return (inv.containsItems());
    }

    @Override
    public boolean pushPattern(final ICraftingPatternDetails patternDetails, final InventoryCrafting table) {
        if (this.hasItemsToSend() || this.hasItemsToSendFacing() || !this.gridProxy.isActive() || !this.craftingList.contains(patternDetails)) {
            return false;
        }

        final TileEntity tile = this.iHost.getTileEntity();
        final World w = tile.getWorld();

        if (this.visitedFaces.isEmpty()) {
            this.visitedFaces = this.iHost.getTargets();
        }

        for (final EnumFacing s : visitedFaces) {
            final TileEntity te = w.getTileEntity(tile.getPos().offset(s));
            if (te instanceof IInterfaceHost || (te instanceof TileCableBus && ((TileCableBus) te).getPart(s.getOpposite()) instanceof PartInterface)) {
                visitedFaces.remove(s);
                try {
                    IInterfaceHost targetTE;
                    if (te instanceof IInterfaceHost) {
                        targetTE = (IInterfaceHost) te;
                    } else {
                        targetTE = (IInterfaceHost) ((TileCableBus) te).getPart(s.getOpposite());
                    }

                    if (targetTE.getInterfaceDuality().sameGrid(this.gridProxy.getGrid())) {
                        continue;
                    } else {
                        IStorageMonitorableAccessor mon = te.getCapability(Capabilities.STORAGE_MONITORABLE_ACCESSOR, s.getOpposite());
                        if (mon != null) {
                            IStorageMonitorable sm = mon.getInventory(this.mySource);
                            if (sm != null && Platform.canAccess(targetTE.getInterfaceDuality().gridProxy, this.mySource)) {
                                if (this.isBlocking() && sm.getInventory(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class)).getStorageList().size() > 0) {
                                    continue;
                                } else {
                                    IMEMonitor<IAEItemStack> inv = sm.getInventory(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class));
                                    for (int x = 0; x < table.getSizeInventory(); x++) {
                                        final ItemStack is = table.getStackInSlot(x);
                                        if (is.isEmpty()) {
                                            continue;
                                        }
                                        IAEItemStack result = inv.injectItems(AEItemStack.fromItemStack(is), Actionable.SIMULATE, this.mySource);
                                        if (result != null) {
                                            return false;
                                        }
                                    }
                                    for (int x = 0; x < table.getSizeInventory(); x++) {
                                        final ItemStack is = table.getStackInSlot(x);
                                        if (!is.isEmpty()) {
                                            addToSendListFacing(is, s);
                                        }
                                    }
                                    pushItemsOut(s);
                                    return true;
                                }
                            }
                        }
                    }
                } catch (final GridAccessException e) {
                    continue;
                }
                continue;
            }

            if (te instanceof ICraftingMachine) {
                final ICraftingMachine cm = (ICraftingMachine) te;
                if (cm.acceptsPlans()) {
                    visitedFaces.remove(s);
                    if (cm.pushPattern(patternDetails, table, s.getOpposite())) {
                        return true;
                    }
                    continue;
                }
            }

            InventoryAdaptor ad = InventoryAdaptor.getAdaptor(te, s.getOpposite());
            if (ad != null) {
                if (this.isBlocking()) {
                    IPhantomTile phantomTE;
                    if (Loader.isModLoaded("actuallyadditions") && te instanceof IPhantomTile) {
                        phantomTE = ((IPhantomTile) te);
                        if (phantomTE.hasBoundPosition()) {
                            TileEntity phantom = w.getTileEntity(phantomTE.getBoundPosition());
                            if (NonBlockingItems.INSTANCE.getMap().containsKey(w.getBlockState(phantomTE.getBoundPosition()).getBlock().getRegistryName().getResourceDomain())) {
                                if (isCustomInvBlocking(phantom, s)) {
                                    visitedFaces.remove(s);
                                    continue;
                                }
                            }
                        }
                    } else if (NonBlockingItems.INSTANCE.getMap().containsKey(w.getBlockState(tile.getPos().offset(s)).getBlock().getRegistryName().getResourceDomain())) {
                        if (isCustomInvBlocking(te, s)) {
                            visitedFaces.remove(s);
                            continue;
                        }
                    } else if (invIsBlocked(ad)) {
                        visitedFaces.remove(s);
                        continue;
                    }
                }

                if (this.acceptsItems(ad, table)) {
                    visitedFaces.remove(s);
                    for (int x = 0; x < table.getSizeInventory(); x++) {
                        final ItemStack is = table.getStackInSlot(x);
                        if (!is.isEmpty()) {
                            addToSendListFacing(is, s);
                        }
                    }
                    pushItemsOut(s);
                    return true;
                }
            }
            visitedFaces.remove(s);
        }
        return false;
    }

    @Override
    public boolean isBusy() {
        boolean busy = false;

        if (this.hasItemsToSend() || hasItemsToSendFacing()) {
            return true;
        }

        if (this.isBlocking()) {
            final EnumSet<EnumFacing> possibleDirections = this.iHost.getTargets();
            final TileEntity tile = this.iHost.getTileEntity();
            final World w = tile.getWorld();

            boolean allAreBusy = true;

            for (final EnumFacing s : possibleDirections) {
                final TileEntity te = w.getTileEntity(tile.getPos().offset(s));

                if (te instanceof IInterfaceHost || (te instanceof TileCableBus && ((TileCableBus) te).getPart(s.getOpposite()) instanceof PartInterface)) {
                    try {
                        IInterfaceHost targetTE;
                        if (te instanceof IInterfaceHost) {
                            targetTE = (IInterfaceHost) te;
                        } else {
                            targetTE = (IInterfaceHost) ((TileCableBus) te).getPart(s.getOpposite());
                        }

                        if (targetTE.getInterfaceDuality().sameGrid(this.gridProxy.getGrid())) {
                            continue;
                        } else {
                            IStorageMonitorableAccessor mon = te.getCapability(Capabilities.STORAGE_MONITORABLE_ACCESSOR, s.getOpposite());
                            if (mon != null) {
                                IStorageMonitorable sm = mon.getInventory(this.mySource);
                                if (sm != null && Platform.canAccess(targetTE.getInterfaceDuality().gridProxy, this.mySource)) {
                                    if (sm.getInventory(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class)).getStorageList().isEmpty()) {
                                        allAreBusy = false;
                                        break;
                                    }
                                }
                            }
                        }
                    } catch (final GridAccessException e) {
                        continue;
                    }
                    continue;
                }

                final InventoryAdaptor ad = InventoryAdaptor.getAdaptor(te, s.getOpposite());
                if (ad != null) {
                    if (Loader.isModLoaded("actuallyadditions") && Loader.isModLoaded("gregtech") && te instanceof IPhantomTile) {
                        IPhantomTile phantomTE = ((IPhantomTile) te);
                        if (phantomTE.hasBoundPosition()) {
                            TileEntity phantom = w.getTileEntity(phantomTE.getBoundPosition());
                            if (NonBlockingItems.INSTANCE.getMap().containsKey(w.getBlockState(phantomTE.getBoundPosition()).getBlock().getRegistryName().getResourceDomain())) {
                                if (!isCustomInvBlocking(phantom, s)) {
                                    allAreBusy = false;
                                    break;
                                }
                            }
                        }
                    } else if (NonBlockingItems.INSTANCE.getMap().containsKey(w.getBlockState(tile.getPos().offset(s)).getBlock().getRegistryName().getResourceDomain())) {
                        if (!isCustomInvBlocking(te, s)) {
                            allAreBusy = false;
                            break;
                        }
                    } else {
                        if (!invIsBlocked(ad)) {
                            allAreBusy = false;
                            break;
                        }
                    }
                }
            }
            busy = allAreBusy;
        }
        return busy;
    }

    boolean isCustomInvBlocking(TileEntity te, EnumFacing s) {
        BlockingInventoryAdaptor blockingInventoryAdaptor = BlockingInventoryAdaptor.getAdaptor(te, s.getOpposite());
        return invIsCustomBlocking(blockingInventoryAdaptor);
    }

    private boolean sameGrid(final IGrid grid) throws GridAccessException {
        return grid == this.gridProxy.getGrid();
    }

    private boolean isBlocking() {
        return this.cm.getSetting(Settings.BLOCK) == YesNo.YES;
    }

    private boolean acceptsItems(final InventoryAdaptor ad, final InventoryCrafting table) {
        for (int x = 0; x < table.getSizeInventory(); x++) {
            final ItemStack is = table.getStackInSlot(x);
            if (is.isEmpty()) {
                continue;
            }

            if (!ad.simulateAdd(is).isEmpty()) {
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

        if (this.waitingToSendFacing != null) {
            for (List<ItemStack> itemList : waitingToSendFacing.values()) {
                for (final ItemStack is : itemList) {
                    if (!is.isEmpty()) {
                        drops.add(is);
                    }
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
        if (this.getTile() instanceof IUpgradeableHost) {
            return (IUpgradeableHost) this.getTile();
        }
        return null;
    }

    private IPart getPart() {
        return (IPart) (this.iHost instanceof IPart ? this.iHost : null);
    }

    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        return this.craftingTracker.getRequestedJobs();
    }

    public IAEItemStack injectCraftedItems(final ICraftingLink link, final IAEItemStack acquired, final Actionable mode) {
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

    public void jobStateChange(final ICraftingLink link) {
        this.craftingTracker.jobStateChange(link);
    }

    public String getTermName() {
        final TileEntity hostTile = this.iHost.getTileEntity();
        final World hostWorld = hostTile.getWorld();

        if (((ICustomNameObject) this.iHost).hasCustomInventoryName()) {
            return ((ICustomNameObject) this.iHost).getCustomInventoryName();
        }

        final EnumSet<EnumFacing> possibleDirections = this.iHost.getTargets();
        for (final EnumFacing direction : possibleDirections) {
            final BlockPos targ = hostTile.getPos().offset(direction);
            final TileEntity directedTile = hostWorld.getTileEntity(targ);

            if (directedTile == null) {
                continue;
            }

            if (directedTile instanceof IInterfaceHost) {
                try {
                    if (((IInterfaceHost) directedTile).getInterfaceDuality().sameGrid(this.gridProxy.getGrid())) {
                        continue;
                    }
                } catch (final GridAccessException e) {
                    continue;
                }
            }

            final InventoryAdaptor adaptor = InventoryAdaptor.getAdaptor(directedTile, direction.getOpposite());
            if (directedTile instanceof ICraftingMachine || adaptor != null) {
                if (adaptor != null && !adaptor.hasSlots()) {
                    continue;
                }

                final IBlockState directedBlockState = hostWorld.getBlockState(targ);
                final Block directedBlock = directedBlockState.getBlock();
                ItemStack what = new ItemStack(directedBlock, 1, directedBlock.getMetaFromState(directedBlockState));

                if (Loader.isModLoaded("gregtech") && directedBlock instanceof BlockMachine) {
                    MetaTileEntity metaTileEntity = Platform.getMetaTileEntity(directedTile.getWorld(), directedTile.getPos());
                    if (metaTileEntity != null) {
                        return metaTileEntity.getMetaFullName();
                    }
                }

                try {
                    Vec3d from = new Vec3d(hostTile.getPos().getX() + 0.5, hostTile.getPos().getY() + 0.5, hostTile.getPos().getZ() + 0.5);
                    from = from.addVector(direction.getFrontOffsetX() * 0.501, direction.getFrontOffsetY() * 0.501, direction.getFrontOffsetZ() * 0.501);
                    final Vec3d to = from.addVector(direction.getFrontOffsetX(), direction.getFrontOffsetY(), direction.getFrontOffsetZ());
                    final RayTraceResult mop = hostWorld.rayTraceBlocks(from, to, true);
                    if (mop != null && !BAD_BLOCKS.contains(directedBlock)) {
                        if (mop.getBlockPos().equals(directedTile.getPos())) {
                            final ItemStack g = directedBlock.getPickBlock(directedBlockState, mop, hostWorld, directedTile.getPos(), null);
                            if (!g.isEmpty()) {
                                what = g;
                            }
                        }
                    }
                } catch (final Throwable t) {
                    BAD_BLOCKS.add(directedBlock); // nope!
                }

                if (what.getItem() != Items.AIR) {
                    return what.getItem().getItemStackDisplayName(what);
                }

                final Item item = Item.getItemFromBlock(directedBlock);
                if (item == Items.AIR) {
                    return directedBlock.getUnlocalizedName();
                }
            }
        }

        return "Nothing";
    }

    public long getSortValue() {
        final TileEntity te = this.iHost.getTileEntity();
        return (te.getPos().getZ() << 24) ^ (te.getPos().getX() << 8) ^ te.getPos().getY();
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

        try {
            this.gridProxy.getGrid().postEvent(new MENetworkCraftingPatternChange(this, this.gridProxy.getNode()));
        } catch (final GridAccessException e) {
            // :P
        }
    }

    public boolean hasCapability(Capability<?> capabilityClass, EnumFacing facing) {
        return capabilityClass == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || capabilityClass == Capabilities.STORAGE_MONITORABLE_ACCESSOR;
    }

    @SuppressWarnings("unchecked")
    public <T> T getCapability(Capability<T> capabilityClass, EnumFacing facing) {
        if (capabilityClass == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return (T) this.storage;
        } else if (capabilityClass == Capabilities.STORAGE_MONITORABLE_ACCESSOR) {
            return (T) this.accessor;
        }
        return null;
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
            return Integer.compare(DualityInterface.this.priority, o);
        }
    }


    private class InterfaceInventory extends MEMonitorIInventory {

        public InterfaceInventory(final DualityInterface tileInterface) {
            super(new AdaptorItemHandler(tileInterface.storage));
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
            final boolean hasLowerOrEqualPriority = context.map(c -> c.compareTo(DualityInterface.this.priority) <= 0).orElse(false);

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
            return DualityInterface.this.getMonitorable(src, DualityInterface.this);
        }

    }

}
