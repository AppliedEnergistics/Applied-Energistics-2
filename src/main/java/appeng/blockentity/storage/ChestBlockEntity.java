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

import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.TypeFilter;
import appeng.api.config.ViewItems;
import appeng.api.implementations.blockentities.IColorableBlockEntity;
import appeng.api.implementations.blockentities.IMEChest;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.events.GridPowerStorageStateChanged;
import appeng.api.networking.events.GridPowerStorageStateChanged.PowerEventType;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.storage.IStorageMonitorableAccessor;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageCells;
import appeng.api.storage.StorageHelper;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.ICellHandler;
import appeng.api.storage.cells.StorageCell;
import appeng.api.util.AEColor;
import appeng.api.util.IConfigManager;
import appeng.blockentity.ServerTickingBlockEntity;
import appeng.blockentity.grid.AENetworkPowerBlockEntity;
import appeng.capabilities.Capabilities;
import appeng.core.definitions.AEBlocks;
import appeng.helpers.IPriorityHost;
import appeng.me.helpers.MachineSource;
import appeng.me.storage.DelegatingMEInventory;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.implementations.ChestMenu;
import appeng.menu.locator.MenuLocators;
import appeng.util.ConfigManager;
import appeng.util.Platform;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.CombinedInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;

public class ChestBlockEntity extends AENetworkPowerBlockEntity
        implements IMEChest, ITerminalHost, IPriorityHost, IColorableBlockEntity,
        ServerTickingBlockEntity, IStorageProvider {

    private static final int BIT_POWER_MASK = Byte.MIN_VALUE;
    private static final int BIT_STATE_MASK = 0b111;

    private static final int BIT_CELL_STATE_MASK = 0b111;
    private static final int BIT_CELL_STATE_BITS = 3;

    private final AppEngInternalInventory inputInventory = new AppEngInternalInventory(this, 1);
    private final AppEngInternalInventory cellInventory = new AppEngInternalInventory(this, 1);
    private final InternalInventory internalInventory = new CombinedInternalInventory(this.inputInventory,
            this.cellInventory);

    private final IActionSource mySrc = new MachineSource(this);
    private final IConfigManager config = new ConfigManager(this::saveChanges);
    private int priority = 0;
    private int state = 0;
    private boolean wasOnline = false;
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
        this.setInternalMaxPower(PowerMultiplier.CONFIG.multiply(500));
        this.getMainNode()
                .addService(IStorageProvider.class, this)
                .setFlags(GridFlags.REQUIRE_CHANNEL);
        this.config.registerSetting(Settings.SORT_BY, SortOrder.NAME);
        this.config.registerSetting(Settings.VIEW_MODE, ViewItems.ALL);
        this.config.registerSetting(Settings.TYPE_FILTER, TypeFilter.ALL);
        this.config.registerSetting(Settings.SORT_DIRECTION, SortDir.ASCENDING);

        this.setInternalPublicPowerStorage(true);
        this.setInternalPowerFlow(AccessRestriction.WRITE);

        this.inputInventory.setFilter(new InputInventoryFilter());
        this.cellInventory.setFilter(new CellInventoryFilter());
    }

    public ItemStack getCell() {
        return this.cellInventory.getStackInSlot(0);
    }

    public void setCell(ItemStack stack) {
        this.cellInventory.setItemDirect(0, Objects.requireNonNull(stack));
    }

    @Override
    protected void PowerEvent(PowerEventType x) {
        if (x == PowerEventType.REQUEST_POWER) {
            this.getMainNode().ifPresent(
                    grid -> grid.postEvent(new GridPowerStorageStateChanged(this, PowerEventType.REQUEST_POWER)));
        } else {
            this.recalculateDisplay();
        }
    }

    private void recalculateDisplay() {
        var oldState = this.state;

        var state = 0;

        for (int x = 0; x < this.getCellCount(); x++) {
            state |= this.getCellStatus(x).ordinal() << BIT_CELL_STATE_BITS * x;
        }

        if (this.isPowered()) {
            state |= BIT_POWER_MASK;
        }

        if (oldState != state) {
            this.state = state;
            this.markForUpdate();
        }
    }

    @Override
    public int getCellCount() {
        return 1;
    }

    private void updateHandler() {
        if (!this.isCached) {
            this.cellHandler = null;
            this.accessor = null;
            this.fluidHandler = null;

            var is = this.getCell();
            if (!is.isEmpty()) {
                this.isCached = true;
                var newCell = StorageCells.getCellInventory(is, this::onCellContentChanged);
                if (newCell != null) {
                    idlePowerUsage = 1.0 + newCell.getIdleDrain();
                    this.cellHandler = this.wrap(newCell);

                    this.getMainNode().setIdlePowerUsage(idlePowerUsage);
                    this.accessor = new Accessor();

                    if (this.cellHandler != null) {
                        this.fluidHandler = new FluidHandler();
                    }
                }
            }
        }
    }

    private ChestMonitorHandler wrap(StorageCell cellInventory) {
        if (cellInventory == null) {
            return null;
        }

        return new ChestMonitorHandler(cellInventory, cellInventory);
    }

    @Override
    public CellState getCellStatus(int slot) {
        if (isClientSide()) {
            return CellState.values()[this.state >> slot * BIT_CELL_STATE_BITS & BIT_CELL_STATE_MASK];
        }

        this.updateHandler();

        final ItemStack cell = this.getCell();
        final ICellHandler ch = StorageCells.getHandler(cell);

        if (this.cellHandler != null && ch != null) {
            return this.cellHandler.cellInventory.getStatus();
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
        if (isClientSide()) {
            return (this.state & BIT_POWER_MASK) == BIT_POWER_MASK;
        }

        boolean gridPowered = this.getAECurrentPower() > 64;

        if (!gridPowered) {
            gridPowered = this.getMainNode().isPowered();
        }

        return super.getAECurrentPower() > 1 || gridPowered;
    }

    @Override
    public boolean isCellBlinking(int slot) {
        return false;
    }

    @Override
    protected double extractAEPower(double amt, Actionable mode) {
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

        if (!this.inputInventory.isEmpty()) {
            this.tryToStoreContents();
        }
    }

    @Override
    protected void writeToStream(FriendlyByteBuf data) {
        super.writeToStream(data);

        this.state = 0;

        for (int x = 0; x < this.getCellCount(); x++) {
            this.state |= this.getCellStatus(x).ordinal() << 3 * x;
        }

        if (this.isPowered()) {
            this.state |= BIT_POWER_MASK;
        }

        data.writeByte(this.state);
        data.writeByte(this.paintedColor.ordinal());

        // Note that we trust that the change detection in recalculateDisplay will trip
        // when it changes from
        // empty->non-empty, so when the cell is changed, it should re-send the state
        // because of that
        data.writeVarInt(Item.getId(getCell().getItem()));
    }

    @Override
    protected boolean readFromStream(FriendlyByteBuf data) {
        final boolean c = super.readFromStream(data);

        final int oldState = this.state;

        this.state = data.readByte();
        final AEColor oldPaintedColor = this.paintedColor;
        this.paintedColor = AEColor.values()[data.readByte()];
        this.cellItem = Item.byId(data.readVarInt());

        return oldPaintedColor != this.paintedColor || (this.state & 0xDB6DB6DB) != (oldState & 0xDB6DB6DB) || c;
    }

    @Override
    public void loadTag(CompoundTag data) {
        super.loadTag(data);
        this.config.readFromNBT(data);
        this.priority = data.getInt("priority");
        if (data.contains("paintedColor")) {
            this.paintedColor = AEColor.values()[data.getByte("paintedColor")];
        }
    }

    @Override
    public void saveAdditional(CompoundTag data) {
        super.saveAdditional(data);
        this.config.writeToNBT(data);
        data.putInt("priority", this.priority);
        data.putByte("paintedColor", (byte) this.paintedColor.ordinal());
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        var currentOnline = this.getMainNode().isOnline();
        if (this.wasOnline != currentOnline) {
            this.wasOnline = currentOnline;
            IStorageProvider.requestUpdate(getMainNode());
            recalculateDisplay();
        }
    }

    @Override
    public MEStorage getInventory() {
        this.updateHandler();

        if (this.cellHandler != null) {
            return this.cellHandler;
        }
        return null;
    }

    @Override
    public InternalInventory getInternalInventory() {
        return this.internalInventory;
    }

    @Override
    public void onChangeInventory(InternalInventory inv, int slot) {
        if (inv == this.cellInventory) {
            this.cellHandler = null;
            this.isCached = false; // recalculate the storage cell.

            IStorageProvider.requestUpdate(getMainNode());

            // update the neighbors
            if (this.level != null) {
                Platform.notifyBlocksOfNeighbors(this.level, this.worldPosition);
                this.markForUpdate();
            }
        }
        if (inv == this.inputInventory && !inv.getStackInSlot(slot).isEmpty()) {
            this.tryToStoreContents();
        }
    }

    @Override
    public InternalInventory getExposedInventoryForSide(Direction side) {
        if (side == this.getForward()) {
            return this.cellInventory;
        } else {
            return this.inputInventory;
        }
    }

    private void tryToStoreContents() {

        if (!this.inputInventory.isEmpty()) {
            this.updateHandler();

            if (this.cellHandler != null) {
                var stack = this.inputInventory.getStackInSlot(0);
                if (stack.isEmpty()) {
                    return;
                }

                var inserted = StorageHelper.poweredInsert(this, this.cellHandler,
                        AEItemKey.of(stack), stack.getCount(), this.mySrc);

                if (inserted >= stack.getCount()) {
                    this.inputInventory.setItemDirect(0, ItemStack.EMPTY);
                } else {
                    stack.shrink((int) inserted);
                    this.inputInventory.setItemDirect(0, stack);
                }
            }
        }
    }

    @Override
    public void mountInventories(IStorageMounts storageMounts) {
        if (this.getMainNode().isOnline()) {
            this.updateHandler();

            if (this.cellHandler != null) {
                storageMounts.mount(this.cellHandler, priority);
            }
        }
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    @Override
    public void setPriority(int newValue) {
        this.priority = newValue;
        this.cellHandler = null;
        this.isCached = false; // recalculate the storage cell.

        IStorageProvider.requestUpdate(getMainNode());
    }

    private void blinkCell(int slot) {
        this.recalculateDisplay();
    }

    @Override
    public IConfigManager getConfigManager() {
        return this.config;
    }

    public boolean openGui(Player p) {
        this.updateHandler();
        if (this.cellHandler != null) {
            var ch = StorageCells.getHandler(this.getCell());

            if (ch != null) {
                var chg = StorageCells.getGuiHandler(this.getCell());
                if (chg != null) {
                    chg.openChestGui(p, this, ch, this.getCell());
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
    public boolean recolourBlock(Direction side, AEColor newPaintedColor, Player who) {
        if (this.paintedColor == newPaintedColor) {
            return false;
        }

        this.paintedColor = newPaintedColor;
        this.saveChanges();
        this.markForUpdate();
        return true;
    }

    private void onCellContentChanged() {
        if (cellHandler != null) {
            cellHandler.cellInventory.persist();
        }
        this.level.blockEntityChanged(this.worldPosition);
    }

    public void openCellInventoryMenu(Player player) {
        MenuOpener.open(ChestMenu.TYPE, player, MenuLocators.forBlockEntity(this));
    }

    private class ChestMonitorHandler extends DelegatingMEInventory {
        private final StorageCell cellInventory;

        public ChestMonitorHandler(MEStorage inventory, StorageCell cellInventory) {
            super(inventory);
            this.cellInventory = cellInventory;
        }

        @Override
        public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
            if (source.player().map(player -> !this.securityCheck(player, SecurityPermissions.INJECT)).orElse(false)) {
                return 0;
            }
            var inserted = super.insert(what, amount, mode, source);
            if (inserted > 0 && mode == Actionable.MODULATE) {
                blinkCell(0);
            }
            return inserted;
        }

        private boolean securityCheck(Player player, SecurityPermissions requiredPermission) {
            return Platform.checkPermissions(player, ChestBlockEntity.this, requiredPermission, false, false);
        }

        @Override
        public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
            if (source.player().map(player -> !this.securityCheck(player, SecurityPermissions.EXTRACT)).orElse(false)) {
                return 0;
            }
            var extracted = super.extract(what, amount, mode, source);
            if (extracted > 0 && mode == Actionable.MODULATE) {
                blinkCell(0);
            }
            return extracted;
        }
    }

    @Nullable
    public IFluidHandler getFluidHandler(Direction side) {
        if (side != getForward()) {
            return fluidHandler;
        } else {
            return null;
        }
    }

    @Nullable
    public IStorageMonitorableAccessor getMEHandler(Direction side) {
        if (side != getForward()) {
            return accessor;
        } else {
            return null;
        }
    }

    private class Accessor implements IStorageMonitorableAccessor {
        @Nullable
        @Override
        public MEStorage getInventory(IActionSource src) {
            if (Platform.canAccess(ChestBlockEntity.this.getMainNode(), src)) {
                return ChestBlockEntity.this.getInventory();
            }
            return null;
        }
    }

    private class FluidHandler implements IFluidHandler, IFluidTank {

        private boolean canAcceptLiquids() {
            return ChestBlockEntity.this.cellHandler != null;
        }

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
            return canAcceptLiquids() ? FluidType.BUCKET_VOLUME : 0;
        }

        @Override
        public boolean isFluidValid(FluidStack stack) {
            return canAcceptLiquids();
        }

        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            return tank == 0 ? FluidType.BUCKET_VOLUME : 0;
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return tank == 0;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            ChestBlockEntity.this.updateHandler();
            if (canAcceptLiquids()) {
                var what = AEFluidKey.of(resource);
                if (what != null) {
                    return (int) StorageHelper.poweredInsert(ChestBlockEntity.this,
                            ChestBlockEntity.this.cellHandler,
                            what,
                            resource.getAmount(),
                            ChestBlockEntity.this.mySrc,
                            Actionable.of(action));
                }
            }
            return 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return FluidStack.EMPTY;
        }
    }

    private class InputInventoryFilter implements IAEItemFilter {
        @Override
        public boolean allowExtract(InternalInventory inv, int slot, int amount) {
            return false;
        }

        @Override
        public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
            if (isPowered()) {
                updateHandler();
                if (cellHandler == null) {
                    return false;
                }

                var what = AEItemKey.of(stack);
                if (what == null) {
                    return false;
                }

                return cellHandler.insert(what, stack.getCount(), Actionable.SIMULATE, mySrc) > 0;
            }
            return false;
        }
    }

    private static class CellInventoryFilter implements IAEItemFilter {

        @Override
        public boolean allowExtract(InternalInventory inv, int slot, int amount) {
            return true;
        }

        @Override
        public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
            return StorageCells.getHandler(stack) != null;
        }

    }

    @Override
    public ItemStack getMainMenuIcon() {
        return AEBlocks.CHEST.stack();
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu) {
        MenuOpener.returnTo(ChestMenu.TYPE, player, MenuLocators.forBlockEntity(this));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
        this.updateHandler();
        if (capability == ForgeCapabilities.FLUID_HANDLER && this.fluidHandler != null
                && facing != this.getForward()) {
            return (LazyOptional<T>) LazyOptional.of(() -> this.fluidHandler);
        }
        if (capability == Capabilities.STORAGE_MONITORABLE_ACCESSOR && this.accessor != null
                && facing != this.getForward()) {
            return (LazyOptional<T>) LazyOptional.of(() -> this.accessor);
        }
        return super.getCapability(capability, facing);
    }
}
