/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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

package appeng.fluids.helper;


import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.config.YesNo;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.implementations.tiles.ICraftingMachine;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
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
import appeng.fluids.util.AEFluidInventory;
import appeng.fluids.util.IAEFluidInventory;
import appeng.fluids.util.IAEFluidTank;
import appeng.helpers.ICustomNameObject;
import appeng.helpers.IInterfaceHost;
import appeng.me.GridAccessException;
import appeng.me.GridNodeCollection;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.MachineSource;
import appeng.me.storage.MEMonitorIFluidHandler;
import appeng.me.storage.MEMonitorPassThrough;
import appeng.me.storage.NullInventory;
import appeng.parts.automation.StackUpgradeInventory;
import appeng.parts.automation.UpgradeInventory;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;
import gregtech.api.block.machines.BlockMachine;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.items.IItemHandler;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;


public class DualityFluidInterface implements IGridTickable, IStorageMonitorable, IAEFluidInventory, IAEAppEngInventory, IUpgradeableHost, IConfigManagerHost, IConfigurableFluidInventory {
    public static final int NUMBER_OF_TANKS = 9;
    public static final int TANK_CAPACITY = Fluid.BUCKET_VOLUME * 4;
    private static final Collection<Block> BAD_BLOCKS = new HashSet<>(100);
    private final ConfigManager cm = new ConfigManager(this);
    private final AENetworkProxy gridProxy;
    private final IFluidInterfaceHost iHost;
    private final IActionSource mySource;
    private final IActionSource interfaceRequestSource;
    private final UpgradeInventory upgrades;
    private boolean hasConfig = false;
    private final IStorageMonitorableAccessor accessor = this::getMonitorable;
    private final AEFluidInventory tanks = new AEFluidInventory(this, NUMBER_OF_TANKS, TANK_CAPACITY);
    private final AEFluidInventory config = new AEFluidInventory(this, NUMBER_OF_TANKS);
    private final IAEFluidStack[] requireWork;
    private int isWorking = -1;
    private int priority;

    private final MEMonitorPassThrough<IAEItemStack> items = new MEMonitorPassThrough<>(new NullInventory<IAEItemStack>(), AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class));
    private final MEMonitorPassThrough<IAEFluidStack> fluids = new MEMonitorPassThrough<>(new NullInventory<IAEFluidStack>(), AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class));
    private boolean resetConfigCache = true;
    private IMEMonitor<IAEFluidStack> configCachedHandler;

    public DualityFluidInterface(final AENetworkProxy networkProxy, final IFluidInterfaceHost ih) {
        this.gridProxy = networkProxy;
        this.gridProxy.setFlags(GridFlags.REQUIRE_CHANNEL);

        this.upgrades = new StackUpgradeInventory(this.gridProxy.getMachineRepresentation(), this, 2);
        this.cm.registerSetting(Settings.INTERFACE_TERMINAL, YesNo.YES);

        this.iHost = ih;

        this.mySource = new MachineSource(this.iHost);
        this.interfaceRequestSource = new InterfaceRequestSource(this.iHost);

        this.fluids.setChangeSource(this.mySource);
        this.items.setChangeSource(this.mySource);

        this.requireWork = new IAEFluidStack[NUMBER_OF_TANKS];
        for (int i = 0; i < NUMBER_OF_TANKS; ++i) {
            this.requireWork[i] = null;
        }
    }

    public IUpgradeableHost getHost() {
        return this.iHost;
    }

    @Override
    public <T extends IAEStack<T>> IMEMonitor<T> getInventory(IStorageChannel<T> channel) {
        if (channel == AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class)) {
            if (this.hasConfig()) {
                return null;
            }

            return (IMEMonitor<T>) this.items;
        } else if (channel == AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class)) {
            if (this.hasConfig()) {
                if (resetConfigCache) {
                    resetConfigCache = false;
                    configCachedHandler = new InterfaceInventory(this);
                }
                return (IMEMonitor<T>) configCachedHandler;
            }

            return (IMEMonitor<T>) this.fluids;
        }

        return null;
    }

    public IStorageMonitorable getMonitorable(final IActionSource src) {
        if (Platform.canAccess(this.gridProxy, src)) {
            return this;
        }

        return null;
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

        final boolean couldDoWork = this.updateStorage();
        return this.hasWorkToDo() ? (couldDoWork ? TickRateModulation.URGENT : TickRateModulation.SLOWER) : TickRateModulation.SLEEP;
    }

    public void notifyNeighbors() {
        if (this.gridProxy.isActive()) {
            try {
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

    private boolean sameGrid(final IGrid grid) throws GridAccessException {
        return grid == this.gridProxy.getGrid();
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

            if (directedTile instanceof IFluidInterfaceHost) {
                try {
                    if (((IFluidInterfaceHost) directedTile).getDualityFluidInterface().sameGrid(this.gridProxy.getGrid())) {
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

    public boolean hasCapability(Capability<?> capabilityClass, EnumFacing facing) {
        return capabilityClass == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || capabilityClass == Capabilities.STORAGE_MONITORABLE_ACCESSOR;
    }

    @SuppressWarnings("unchecked")
    public <T> T getCapability(Capability<T> capabilityClass, EnumFacing facing) {
        if (capabilityClass == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return (T) this.tanks;
        } else if (capabilityClass == Capabilities.STORAGE_MONITORABLE_ACCESSOR) {
            return (T) this.accessor;
        }
        return null;
    }

    private boolean hasConfig() {
        return this.hasConfig;
    }

    private void readConfig() {
        this.hasConfig = false;

        for (int i = 0; i < this.config.getSlots(); i++) {
            if (this.config.getFluidInSlot(i) != null) {
                this.hasConfig = true;
                break;
            }
        }

        final boolean had = this.hasWorkToDo();

        for (int x = 0; x < NUMBER_OF_TANKS; x++) {
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

    private boolean updateStorage() {
        boolean didSomething = false;
        for (int x = 0; x < NUMBER_OF_TANKS; x++) {
            if (this.requireWork[x] != null) {
                didSomething = this.usePlan(x) || didSomething;
            }
        }
        return didSomething;
    }

    private boolean hasWorkToDo() {
        for (final IAEFluidStack requiredWork : this.requireWork) {
            if (requiredWork != null) {
                return true;
            }
        }

        return false;
    }

    private void updatePlan(final int slot) {
        final IAEFluidStack req = this.config.getFluidInSlot(slot);
        final IAEFluidStack stored = this.tanks.getFluidInSlot(slot);

        if (req == null && (stored != null && stored.getStackSize() > 0)) {
            final IAEFluidStack work = stored.copy();
            this.requireWork[slot] = work.setStackSize(-work.getStackSize());
            return;
        } else if (req != null) {
            int tankSize = (int) (Math.pow(4, this.getInstalledUpgrades(Upgrades.CAPACITY) + 1) * Fluid.BUCKET_VOLUME);
            if (stored == null || stored.getStackSize() == 0) // need to add stuff!
            {
                this.requireWork[slot] = req.copy();
                this.requireWork[slot].setStackSize(tankSize);
                return;
            } else if (req.equals(stored)) // same type ( qty different? )!
            {
                if (stored.getStackSize() != tankSize) {
                    this.requireWork[slot] = req.copy();
                    this.requireWork[slot].setStackSize(tankSize - stored.getStackSize());
                    return;
                }
            } else
            // Stored != null; dispose!
            {
                final IAEFluidStack work = stored.copy();
                this.requireWork[slot] = work.setStackSize(-work.getStackSize());
                return;
            }
        }

        this.requireWork[slot] = null;
    }

    private boolean usePlan(final int slot) {
        IAEFluidStack work = this.requireWork[slot];
        this.isWorking = slot;

        boolean changed = false;
        try {
            final IMEInventory<IAEFluidStack> dest = this.gridProxy.getStorage()
                    .getInventory(AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class));
            final IEnergySource src = this.gridProxy.getEnergy();

            if (work.getStackSize() > 0) {
                // make sure strange things didn't happen...
                if (this.tanks.fill(slot, work.getFluidStack(), false) != work.getStackSize()) {
                    changed = true;
                } else if (this.gridProxy.getStorage().getInventory(AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class)).getStorageList().findPrecise(work) != null) {
                    final IAEFluidStack acquired = Platform.poweredExtraction(src, dest, work, this.interfaceRequestSource);
                    if (acquired != null) {
                        changed = true;
                        final int filled = this.tanks.fill(slot, acquired.getFluidStack(), true);
                        if (filled != acquired.getStackSize()) {
                            throw new IllegalStateException("bad attempt at managing tanks. ( fill )");
                        }
                    }
                }
            } else if (work.getStackSize() < 0) {
                IAEFluidStack toStore = work.copy();
                toStore.setStackSize(-toStore.getStackSize());

                // make sure strange things didn't happen...
                final FluidStack canExtract = this.tanks.drain(slot, toStore.getFluidStack(), false);
                if (canExtract == null || canExtract.amount != toStore.getStackSize()) {
                    changed = true;
                } else {
                    IAEFluidStack notStored = Platform.poweredInsert(src, dest, toStore, this.interfaceRequestSource);
                    toStore.setStackSize(toStore.getStackSize() - (notStored == null ? 0 : notStored.getStackSize()));

                    if (toStore.getStackSize() > 0) {
                        // extract items!
                        changed = true;
                        final FluidStack removed = this.tanks.drain(slot, toStore.getFluidStack(), true);
                        if (removed == null || toStore.getStackSize() != removed.amount) {
                            throw new IllegalStateException("bad attempt at managing tanks. ( drain )");
                        }
                    }
                }
            }
        } catch (final GridAccessException e) {
            // :P
        }

        if (changed) {
            this.updatePlan(slot);
        }

        this.isWorking = -1;
        return changed;
    }

    @Override
    public void onFluidInventoryChanged(final IAEFluidTank inventory, final int slot) {
        if (this.isWorking == slot) {
            return;
        }

        if (inventory == this.config) {
            boolean cfg = hasConfig();
            this.readConfig();
            if (cfg != hasConfig) {
                resetConfigCache = true;
                this.notifyNeighbors();
            }
        } else if (inventory == this.tanks) {
            this.saveChanges();

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

    public int getPriority() {
        return this.priority;
    }

    public void setPriority(final int newValue) {
        this.priority = newValue;
    }

    public void writeToNBT(final NBTTagCompound data) {
        data.setInteger("priority", this.priority);
        this.tanks.writeToNBT(data, "storage");
        this.config.writeToNBT(data, "config");
        this.upgrades.writeToNBT(data, "upgrades");
    }

    public void readFromNBT(final NBTTagCompound data) {
        this.config.readFromNBT(data, "config");
        this.tanks.readFromNBT(data, "storage");
        this.priority = data.getInteger("priority");
        this.upgrades.readFromNBT(data, "upgrades");
        this.tanks.setCapacity((int) (Math.pow(4, this.getInstalledUpgrades(Upgrades.CAPACITY) + 1) * Fluid.BUCKET_VOLUME));
        this.readConfig();
    }

    public IAEFluidTank getConfig() {
        return this.config;
    }

    public IAEFluidTank getTanks() {
        return this.tanks;
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
                return (Optional<T>) Optional.of(this.context);
            }

            return super.context(key);
        }
    }

    private class InterfaceRequestContext implements Comparable<Integer> {
        @Override
        public int compareTo(Integer o) {
            return Integer.compare(DualityFluidInterface.this.priority, o);
        }
    }

    private class InterfaceInventory extends MEMonitorIFluidHandler {

        InterfaceInventory(final DualityFluidInterface tileInterface) {
            super(tileInterface.tanks);
        }

        @Override
        public IAEFluidStack injectItems(final IAEFluidStack input, final Actionable type, final IActionSource src) {
            final Optional<InterfaceRequestContext> context = src.context(InterfaceRequestContext.class);
            final boolean isInterface = context.isPresent();

            if (isInterface) {
                return input;
            }

            return super.injectItems(input, type, src);
        }

        @Override
        public IAEFluidStack extractItems(final IAEFluidStack request, final Actionable type, final IActionSource src) {
            final Optional<InterfaceRequestContext> context = src.context(InterfaceRequestContext.class);
            final boolean hasLowerOrEqualPriority = context.map(c -> c.compareTo(DualityFluidInterface.this.priority) <= 0).orElse(false);

            if (hasLowerOrEqualPriority) {
                return null;
            }

            return super.extractItems(request, type, src);
        }
    }

    public void saveChanges() {
        this.iHost.saveChanges();
    }

    @Override
    public void onChangeInventory(IItemHandler inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack) {
        if (inv == this.upgrades) {
            this.tanks.setCapacity((int) (Math.pow(4, this.getInstalledUpgrades(Upgrades.CAPACITY) + 1) * Fluid.BUCKET_VOLUME));
            try {
                this.gridProxy.getTick().alertDevice(this.gridProxy.getNode());
            } catch (GridAccessException ignored) {
            }
            for (int x = 0; x < NUMBER_OF_TANKS; x++) {
                this.updatePlan(x);
            }
        }
    }

    @Override
    public IConfigManager getConfigManager() {
        return this.cm;
    }

    @Override
    public IItemHandler getInventoryByName(String name) {
        if (name.equals("upgrades")) {
            return this.upgrades;
        }
        return null;
    }

    @Override
    public IFluidHandler getFluidInventoryByName(final String name) {
        if (name.equals("config")) {
            return this.config;
        }
        return null;
    }

    @Override
    public int getInstalledUpgrades(Upgrades u) {
        if (this.upgrades == null) {
            return 0;
        }
        return this.upgrades.getInstalledUpgrades(u);
    }

    public void addDrops(final List<ItemStack> drops) {
        for (final ItemStack is : this.upgrades) {
            if (!is.isEmpty()) {
                drops.add(is);
            }
        }
    }

    @Override
    public TileEntity getTile() {
        return (TileEntity) (this.iHost instanceof TileEntity ? this.iHost : null);
    }

    @Override
    public void updateSetting(IConfigManager manager, Enum settingName, Enum newValue) {
    }
}
