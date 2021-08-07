/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

package appeng.blockentity.storage;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.implementations.blockentities.IColorableBlockEntity;
import appeng.api.implementations.blockentities.IMEChest;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.events.GridCellArrayUpdate;
import appeng.api.networking.events.GridPowerStorageStateChanged;
import appeng.api.networking.events.GridPowerStorageStateChanged.PowerEventType;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.security.ISecurityService;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.IStorageMonitorableAccessor;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.ICellGuiHandler;
import appeng.api.storage.cells.ICellHandler;
import appeng.api.storage.cells.ICellInventory;
import appeng.api.storage.cells.ICellInventoryHandler;
import appeng.api.storage.cells.ICellProvider;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.AEColor;
import appeng.api.util.IConfigManager;
import appeng.blockentity.ServerTickingBlockEntity;
import appeng.blockentity.grid.AENetworkPowerBlockEntity;
import appeng.blockentity.inventory.AppEngInternalInventory;
import appeng.capabilities.Capabilities;
import appeng.container.me.fluids.FluidTerminalContainer;
import appeng.container.me.items.ItemTerminalContainer;
import appeng.core.Api;
import appeng.core.definitions.AEBlocks;
import appeng.helpers.IPriorityHost;
import appeng.me.helpers.MEMonitorHandler;
import appeng.me.helpers.MachineSource;
import appeng.me.storage.MEInventoryHandler;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;
import appeng.util.fluid.AEFluidStack;
import appeng.util.helpers.ItemHandlerUtil;
import appeng.util.inv.InvOperation;
import appeng.util.inv.WrapperChainedItemHandler;
import appeng.util.inv.filter.IAEItemFilter;
import appeng.util.item.AEItemStack;

public class ChestBlockEntity extends AENetworkPowerBlockEntity
        implements IMEChest, ITerminalHost, IPriorityHost, IConfigManagerHost, IColorableBlockEntity,
        ServerTickingBlockEntity,
        ICellProvider {

    private static final int BIT_POWER_MASK = Byte.MIN_VALUE;
    private static final int BIT_STATE_MASK = 0b111;

    private static final int BIT_CELL_STATE_MASK = 0b111;
    private static final int BIT_CELL_STATE_BITS = 3;

    private final AppEngInternalInventory inputInventory = new AppEngInternalInventory(this, 1);
    private final AppEngInternalInventory cellInventory = new AppEngInternalInventory(this, 1);
    private final IItemHandler internalInventory = new WrapperChainedItemHandler(this.inputInventory,
            this.cellInventory);

    private final IActionSource mySrc = new MachineSource(this);
    private final IConfigManager config = new ConfigManager(this);
    private long lastStateChange = 0;
    private int priority = 0;
    private int state = 0;
    private boolean wasActive = false;
    private AEColor paintedColor = AEColor.TRANSPARENT;
    private boolean isCached = false;
    private ChestMonitorHandler cellHandler;
    private Accessor accessor;
    private IFluidHandler fluidHandler;
    // This is only used on the client to display the right cell model without
    // synchronizing the entire
    // cell's inventory when a chest comes into view.
    private Item cellItem = Items.AIR;
    private double idlePowerUsage;

    public ChestBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.setInternalMaxPower(PowerMultiplier.CONFIG.multiply(40));
        this.getMainNode()
                .addService(ICellProvider.class, this)
                .setFlags(GridFlags.REQUIRE_CHANNEL);
        this.config.registerSetting(Settings.SORT_BY, SortOrder.NAME);
        this.config.registerSetting(Settings.VIEW_MODE, ViewItems.ALL);
        this.config.registerSetting(Settings.SORT_DIRECTION, SortDir.ASCENDING);

        this.setInternalPublicPowerStorage(true);
        this.setInternalPowerFlow(AccessRestriction.WRITE);

        this.inputInventory.setFilter(new InputInventoryFilter());
        this.cellInventory.setFilter(new CellInventoryFilter());
    }

    public ItemStack getCell() {
        return this.cellInventory.getStackInSlot(0);
    }

    @Override
    protected void PowerEvent(final PowerEventType x) {
        if (x == PowerEventType.REQUEST_POWER) {
            this.getMainNode().ifPresent(
                    grid -> grid.postEvent(new GridPowerStorageStateChanged(this, PowerEventType.REQUEST_POWER)));
        } else {
            this.recalculateDisplay();
        }
    }

    private void recalculateDisplay() {
        final int oldState = this.state;

        for (int x = 0; x < this.getCellCount(); x++) {
            this.state |= this.getCellStatus(x).ordinal() << BIT_CELL_STATE_BITS * x;
        }

        if (this.isPowered()) {
            this.state |= BIT_POWER_MASK;
        } else {
            this.state &= ~BIT_POWER_MASK;
        }

        final boolean currentActive = this.getMainNode().isActive();
        if (this.wasActive != currentActive) {
            this.wasActive = currentActive;
            getMainNode().ifPresent(grid -> grid.postEvent(new GridCellArrayUpdate()));
        }

        if (oldState != this.state) {
            this.markForUpdate();
        }
    }

    @Override
    public int getCellCount() {
        return 1;
    }

    @SuppressWarnings("unchecked")
    private void updateHandler() {
        if (!this.isCached) {
            this.cellHandler = null;
            this.accessor = null;
            this.fluidHandler = null;

            final ItemStack is = this.getCell();
            if (!is.isEmpty()) {
                this.isCached = true;
                ICellHandler cellHandler = Api.instance().registries().cell().getHandler(is);
                if (cellHandler != null) {
                    idlePowerUsage = 1.0;

                    for (IStorageChannel channel : Api.instance().storage().storageChannels()) {
                        final ICellInventoryHandler<IAEItemStack> newCell = cellHandler.getCellInventory(is,
                                this::saveChanges,
                                channel);
                        if (newCell != null) {
                            idlePowerUsage += cellHandler.cellIdleDrain(is, newCell);
                            this.cellHandler = this.wrap(newCell);
                            break;
                        }
                    }

                    this.getMainNode().setIdlePowerUsage(idlePowerUsage);
                    this.accessor = new Accessor();

                    if (this.cellHandler != null && this.cellHandler.getChannel() == Api.instance().storage()
                            .getStorageChannel(IFluidStorageChannel.class)) {
                        this.fluidHandler = new FluidHandler();
                    }
                }
            }
        }
    }

    private <T extends IAEStack<T>> ChestMonitorHandler<T> wrap(final IMEInventoryHandler<T> h) {
        if (h == null) {
            return null;
        }

        final MEInventoryHandler<T> ih = new MEInventoryHandler<T>(h, h.getChannel());
        ih.setPriority(this.priority);

        final ChestMonitorHandler<T> g = new ChestMonitorHandler<T>(ih);
        g.addListener(new ChestNetNotifier<T>(h.getChannel()), g);

        return g;
    }

    @Override
    public CellState getCellStatus(final int slot) {
        if (isRemote()) {
            return CellState.values()[this.state >> slot * BIT_CELL_STATE_BITS & BIT_CELL_STATE_MASK];
        }

        this.updateHandler();

        final ItemStack cell = this.getCell();
        final ICellHandler ch = Api.instance().registries().cell().getHandler(cell);

        if (this.cellHandler != null && ch != null) {
            return ch.getStatusForCell(cell, this.cellHandler.getInternalHandler());
        }

        return CellState.ABSENT;
    }

    @Nullable
    @Override
    public Item getCellItem(int slot) {
        if (slot != 0) {
            return null;
        }
        // Client-side we'll need to actually use the synced state
        if (level == null || level.isClientSide) {
            return cellItem;
        }
        ItemStack cell = getCell();
        return cell.isEmpty() ? null : cell.getItem();
    }

    @Override
    public boolean isPowered() {
        if (isRemote()) {
            return (this.state & BIT_POWER_MASK) == BIT_POWER_MASK;
        }

        boolean gridPowered = this.getAECurrentPower() > 64;

        if (!gridPowered) {
            gridPowered = this.getMainNode().isPowered();
        }

        return super.getAECurrentPower() > 1 || gridPowered;
    }

    @Override
    public boolean isCellBlinking(final int slot) {
        return false;
    }

    @Override
    protected double extractAEPower(final double amt, final Actionable mode) {
        double stash = 0.0;

        var grid = getMainNode().getGrid();
        if (grid != null) {
            var eg = grid.getEnergyService();
            stash = eg.extractAEPower(amt, mode, PowerMultiplier.ONE);
            if (stash >= amt) {
                return stash;
            }
        }

        // local battery!
        return super.extractAEPower(amt - stash, mode) + stash;
    }

    @Override
    public void serverTick() {
        var grid = getMainNode().getGrid();
        if (grid != null) {
            if (!grid.getEnergyService().isNetworkPowered()) {
                final double powerUsed = this.extractAEPower(idlePowerUsage, Actionable.MODULATE,
                        PowerMultiplier.CONFIG); // drain
                if (powerUsed + 0.1 >= idlePowerUsage != (this.state & BIT_POWER_MASK) > 0) {
                    this.recalculateDisplay();
                }
            }
        } else {
            final double powerUsed = this.extractAEPower(idlePowerUsage, Actionable.MODULATE, PowerMultiplier.CONFIG); // drain
            if (powerUsed + 0.1 >= idlePowerUsage != (this.state & BIT_POWER_MASK) > 0) {
                this.recalculateDisplay();
            }
        }

        if (!ItemHandlerUtil.isEmpty(this.inputInventory)) {
            this.tryToStoreContents();
        }
    }

    @Override
    protected void writeToStream(final FriendlyByteBuf data) throws IOException {
        super.writeToStream(data);

        this.state = 0;

        for (int x = 0; x < this.getCellCount(); x++) {
            this.state |= this.getCellStatus(x).ordinal() << 3 * x;
        }

        if (this.isPowered()) {
            this.state |= BIT_POWER_MASK;
        } else {
            this.state &= ~BIT_POWER_MASK;
        }

        data.writeByte(this.state);
        data.writeByte(this.paintedColor.ordinal());

        // Note that we trust that the change detection in recalculateDisplay will trip
        // when it changes from
        // empty->non-empty, so when the cell is changed, it should re-send the state
        // because of that
        data.writeRegistryIdUnsafe(ForgeRegistries.ITEMS, getCell().getItem().getRegistryName());
    }

    @Override
    protected boolean readFromStream(final FriendlyByteBuf data) throws IOException {
        final boolean c = super.readFromStream(data);

        final int oldState = this.state;

        this.state = data.readByte();
        final AEColor oldPaintedColor = this.paintedColor;
        this.paintedColor = AEColor.values()[data.readByte()];
        this.cellItem = data.readRegistryIdUnsafe(ForgeRegistries.ITEMS);

        this.lastStateChange = this.level.getGameTime();

        return oldPaintedColor != this.paintedColor || (this.state & 0xDB6DB6DB) != (oldState & 0xDB6DB6DB) || c;
    }

    @Override
    public void load(final CompoundTag data) {
        super.load(data);
        this.config.readFromNBT(data);
        this.priority = data.getInt("priority");
        if (data.contains("paintedColor")) {
            this.paintedColor = AEColor.values()[data.getByte("paintedColor")];
        }
    }

    @Override
    public CompoundTag save(final CompoundTag data) {
        super.save(data);
        this.config.writeToNBT(data);
        data.putInt("priority", this.priority);
        data.putByte("paintedColor", (byte) this.paintedColor.ordinal());
        return data;
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        this.recalculateDisplay();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IAEStack<T>> IMEMonitor<T> getInventory(IStorageChannel<T> channel) {
        this.updateHandler();

        if (this.cellHandler != null && this.cellHandler.getChannel() == channel) {
            return this.cellHandler;
        }
        return null;
    }

    @Override
    public IItemHandler getInternalInventory() {
        return this.internalInventory;
    }

    @Override
    public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc,
            final ItemStack removed, final ItemStack added) {
        if (inv == this.cellInventory) {
            this.cellHandler = null;
            this.isCached = false; // recalculate the storage cell.

            ifGridPresent(g -> {
                g.postEvent(new GridCellArrayUpdate());
                Platform.postWholeCellChanges(g.getStorageService(), removed, added, this.mySrc);
            });

            // update the neighbors
            if (this.level != null) {
                Platform.notifyBlocksOfNeighbors(this.level, this.worldPosition);
                this.markForUpdate();
            }
        }
        if (inv == this.inputInventory && mc == InvOperation.INSERT) {
            this.tryToStoreContents();
        }
    }

    @Override
    protected IItemHandler getItemHandlerForSide(@Nonnull Direction side) {
        if (side == this.getForward()) {
            return this.cellInventory;
        } else {
            return this.inputInventory;
        }
    }

    private void tryToStoreContents() {
        if (!ItemHandlerUtil.isEmpty(this.inputInventory)) {
            this.updateHandler();

            if (this.cellHandler != null && this.cellHandler.getChannel() == Api.instance().storage()
                    .getStorageChannel(IItemStorageChannel.class)) {
                final IAEItemStack returns = Platform.poweredInsert(this, this.cellHandler,
                        AEItemStack.fromItemStack(this.inputInventory.getStackInSlot(0)), this.mySrc);

                if (returns == null) {
                    this.inputInventory.setStackInSlot(0, ItemStack.EMPTY);
                } else {
                    this.inputInventory.setStackInSlot(0, returns.createItemStack());
                }
            }
        }
    }

    @Override
    public List<IMEInventoryHandler> getCellArray(final IStorageChannel channel) {
        if (this.getMainNode().isActive()) {
            this.updateHandler();

            if (this.cellHandler != null && this.cellHandler.getChannel() == channel) {
                return Collections.singletonList(this.cellHandler);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    @Override
    public void setPriority(final int newValue) {
        this.priority = newValue;
        this.cellHandler = null;
        this.isCached = false; // recalculate the storage cell.

        getMainNode().ifPresent(grid -> grid.postEvent(new GridCellArrayUpdate()));
    }

    private void blinkCell(final int slot) {
        final long now = this.level.getGameTime();
        if (now - this.lastStateChange > 8) {
            this.state = 0;
        }
        this.lastStateChange = now;

        this.state |= 1 << slot * BIT_CELL_STATE_BITS + 2;

        this.recalculateDisplay();
    }

    @Override
    public IConfigManager getConfigManager() {
        return this.config;
    }

    @Override
    public void updateSetting(final IConfigManager manager, final Settings settingName, final Enum<?> newValue) {

    }

    public boolean openGui(final Player p) {
        this.updateHandler();
        if (this.cellHandler != null) {
            final ICellHandler ch = Api.instance().registries().cell().getHandler(this.getCell());

            if (ch != null) {
                final ICellGuiHandler chg = Api.instance().registries().cell()
                        .getGuiHandler(this.cellHandler.getChannel(), this.getCell());
                if (chg != null) {
                    chg.openChestGui(p, this, ch, this.cellHandler, this.getCell(), this.cellHandler.getChannel());
                    return true;
                }
            }

        }

        return false;
    }

    @Override
    public AEColor getColor() {
        return this.paintedColor;
    }

    @Override
    public boolean recolourBlock(final Direction side, final AEColor newPaintedColor, final Player who) {
        if (this.paintedColor == newPaintedColor) {
            return false;
        }

        this.paintedColor = newPaintedColor;
        this.saveChanges();
        this.markForUpdate();
        return true;
    }

    private void saveChanges(final ICellInventory<?> cellInventory) {
        if (cellInventory != null) {
            cellInventory.persist();
        }
        this.level.blockEntityChanged(this.worldPosition);
    }

    private class ChestNetNotifier<T extends IAEStack<T>> implements IMEMonitorHandlerReceiver<T> {

        private final IStorageChannel<T> chan;

        public ChestNetNotifier(final IStorageChannel<T> chan) {
            this.chan = chan;
        }

        @Override
        public boolean isValid(final Object verificationToken) {
            ChestBlockEntity.this.updateHandler();
            if (ChestBlockEntity.this.cellHandler != null
                    && this.chan == ChestBlockEntity.this.cellHandler.getChannel()) {
                return verificationToken == ChestBlockEntity.this.cellHandler;
            }
            return false;
        }

        @Override
        public void postChange(final IBaseMonitor<T> monitor, final Iterable<T> change, final IActionSource source) {
            if (source == ChestBlockEntity.this.mySrc
                    || source.machine().map(machine -> machine == ChestBlockEntity.this).orElse(false)) {
                if (getMainNode().isActive()) {
                    getMainNode()
                            .ifPresent(grid -> grid.getStorageService().postAlterationOfStoredItems(this.chan, change,
                                    ChestBlockEntity.this.mySrc));
                }
            }

            ChestBlockEntity.this.blinkCell(0);
        }

        @Override
        public void onListUpdate() {
            // not used here
        }
    }

    private class ChestMonitorHandler<T extends IAEStack<T>> extends MEMonitorHandler<T> {

        public ChestMonitorHandler(final IMEInventoryHandler<T> t) {
            super(t);
        }

        private ICellInventoryHandler<T> getInternalHandler() {
            final IMEInventoryHandler<T> h = this.getHandler();
            if (h instanceof MEInventoryHandler) {
                return (ICellInventoryHandler<T>) ((MEInventoryHandler<T>) h).getInternal();
            }
            return (ICellInventoryHandler<T>) this.getHandler();
        }

        @Override
        public T injectItems(final T input, final Actionable mode, final IActionSource src) {
            if (src.player().map(player -> !this.securityCheck(player, SecurityPermissions.INJECT)).orElse(false)) {
                return input;
            }
            return super.injectItems(input, mode, src);
        }

        private boolean securityCheck(final Player player, final SecurityPermissions requiredPermission) {
            if (ChestBlockEntity.this.getBlockEntity() instanceof IActionHost && requiredPermission != null) {

                final IGridNode gn = ((IActionHost) ChestBlockEntity.this.getBlockEntity()).getActionableNode();
                if (gn != null && gn.getGrid() != null) {
                    final IGrid g = gn.getGrid();
                    final boolean requirePower = false;
                    if (requirePower) {
                        final IEnergyService eg = g.getService(IEnergyService.class);
                        if (!eg.isNetworkPowered()) {
                            return false;
                        }
                    }

                    final ISecurityService sg = g.getService(ISecurityService.class);
                    if (sg.hasPermission(player, requiredPermission)) {
                        return true;
                    }
                }

                return false;
            }
            return true;
        }

        @Override
        public T extractItems(final T request, final Actionable mode, final IActionSource src) {
            if (src.player().map(player -> !this.securityCheck(player, SecurityPermissions.EXTRACT)).orElse(false)) {
                return null;
            }
            return super.extractItems(request, mode, src);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
        this.updateHandler();
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && this.fluidHandler != null
                && facing != this.getForward()) {
            return (LazyOptional<T>) LazyOptional.of(() -> this.fluidHandler);
        }
        if (capability == Capabilities.STORAGE_MONITORABLE_ACCESSOR && this.accessor != null
                && facing != this.getForward()) {
            return (LazyOptional<T>) LazyOptional.of(() -> this.accessor);
        }
        return super.getCapability(capability, facing);
    }

    private class Accessor implements IStorageMonitorableAccessor {
        @Nullable
        @Override
        public IStorageMonitorable getInventory(IActionSource src) {
            if (Platform.canAccess(ChestBlockEntity.this.getMainNode(), src)) {
                return ChestBlockEntity.this;
            }
            return null;
        }
    }

    private class FluidHandler implements IFluidHandler, IFluidTank {

        private boolean canAcceptLiquids() {
            return ChestBlockEntity.this.cellHandler != null && ChestBlockEntity.this.cellHandler.getChannel() == Api
                    .instance().storage().getStorageChannel(IFluidStorageChannel.class);
        }

        @Nonnull
        @Override
        public FluidStack getFluid() {
            return FluidStack.EMPTY;
        }

        @Override
        public int getFluidAmount() {
            return 0;
        }

        @Override
        public int getCapacity() {
            return canAcceptLiquids() ? FluidAttributes.BUCKET_VOLUME : 0;
        }

        @Override
        public boolean isFluidValid(FluidStack stack) {
            return canAcceptLiquids();
        }

        @Override
        public int getTanks() {
            return 1;
        }

        @Nonnull
        @Override
        public FluidStack getFluidInTank(int tank) {
            return FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            return tank == 0 ? FluidAttributes.BUCKET_VOLUME : 0;
        }

        @Override
        public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
            return tank == 0;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            ChestBlockEntity.this.updateHandler();
            if (canAcceptLiquids()) {
                final IAEFluidStack results = Platform.poweredInsert(ChestBlockEntity.this,
                        ChestBlockEntity.this.cellHandler, AEFluidStack.fromFluidStack(resource),
                        ChestBlockEntity.this.mySrc,
                        action == FluidAction.EXECUTE ? Actionable.MODULATE : Actionable.SIMULATE);

                if (results == null) {
                    return resource.getAmount();
                }
                return resource.getAmount() - (int) results.getStackSize();
            }
            return 0;
        }

        @Nonnull
        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return FluidStack.EMPTY;
        }

        @Nonnull
        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return FluidStack.EMPTY;
        }
    }

    private class InputInventoryFilter implements IAEItemFilter {
        @Override
        public boolean allowExtract(IItemHandler inv, int slot, int amount) {
            return false;
        }

        @Override
        public boolean allowInsert(IItemHandler inv, int slot, ItemStack stack) {
            if (ChestBlockEntity.this.isPowered()) {
                ChestBlockEntity.this.updateHandler();
                return ChestBlockEntity.this.cellHandler != null
                        && ChestBlockEntity.this.cellHandler.getChannel() == Api
                                .instance().storage().getStorageChannel(IItemStorageChannel.class);
            }
            return false;
        }
    }

    private static class CellInventoryFilter implements IAEItemFilter {

        @Override
        public boolean allowExtract(IItemHandler inv, int slot, int amount) {
            return true;
        }

        @Override
        public boolean allowInsert(IItemHandler inv, int slot, ItemStack stack) {
            return Api.instance().registries().cell().getHandler(stack) != null;
        }

    }

    @Override
    public ItemStack getItemStackRepresentation() {
        return AEBlocks.CHEST.stack();
    }

    @Override
    public MenuType<?> getContainerType() {
        this.updateHandler();
        if (this.cellHandler != null) {
            if (this.cellHandler.getChannel() == Api.instance().storage()
                    .getStorageChannel(IItemStorageChannel.class)) {
                return ItemTerminalContainer.TYPE;
            }
            if (this.cellHandler.getChannel() == Api.instance().storage()
                    .getStorageChannel(IFluidStorageChannel.class)) {
                return FluidTerminalContainer.TYPE;
            }
        }
        return null;
    }

}
