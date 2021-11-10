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
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.BlankVariantView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.InsertionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
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
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.events.GridPowerStorageStateChanged;
import appeng.api.networking.events.GridPowerStorageStateChanged.PowerEventType;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.security.ISecurityService;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorListener;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.IStorageMonitorableAccessor;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.StorageCells;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.StorageHelper;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.ICellHandler;
import appeng.api.storage.cells.ICellInventory;
import appeng.api.storage.data.AEFluidKey;
import appeng.api.storage.data.AEItemKey;
import appeng.api.storage.data.AEKey;
import appeng.api.util.AEColor;
import appeng.api.util.IConfigManager;
import appeng.blockentity.ServerTickingBlockEntity;
import appeng.blockentity.grid.AENetworkPowerBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.helpers.IPriorityHost;
import appeng.me.helpers.MEMonitorHandler;
import appeng.me.helpers.MachineSource;
import appeng.menu.me.fluids.FluidTerminalMenu;
import appeng.menu.me.items.ItemTerminalMenu;
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
    private final IConfigManager config = new ConfigManager();
    private long lastStateChange = 0;
    private int priority = 0;
    private int state = 0;
    private boolean wasActive = false;
    private AEColor paintedColor = AEColor.TRANSPARENT;
    private boolean isCached = false;
    private ChestMonitorHandler<?> cellHandler;
    private Accessor accessor;
    private Storage<FluidVariant> fluidHandler;
    // This is only used on the client to display the right cell model without
    // synchronizing the entire
    // cell's inventory when a chest comes into view.
    private Item cellItem = Items.AIR;
    private double idlePowerUsage;

    public ChestBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.setInternalMaxPower(PowerMultiplier.CONFIG.multiply(40));
        this.getMainNode()
                .addService(IStorageProvider.class, this)
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
        var oldState = this.state;

        for (int x = 0; x < this.getCellCount(); x++) {
            this.state |= this.getCellStatus(x).ordinal() << BIT_CELL_STATE_BITS * x;
        }

        if (this.isPowered()) {
            this.state |= BIT_POWER_MASK;
        } else {
            this.state &= ~BIT_POWER_MASK;
        }

        var currentActive = this.getMainNode().isActive();
        if (this.wasActive != currentActive) {
            this.wasActive = currentActive;
            IStorageProvider.requestUpdate(getMainNode());
        }

        if (oldState != this.state) {
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

                    if (this.cellHandler != null && this.cellHandler.getChannel() == StorageChannels.fluids()) {
                        this.fluidHandler = new FluidHandler();
                    }
                }
            }
        }
    }

    private <T extends AEKey> ChestMonitorHandler<T> wrap(ICellInventory<T> cellInventory) {
        if (cellInventory == null) {
            return null;
        }

        var g = new ChestMonitorHandler<>(cellInventory, cellInventory);
        g.addListener(new ChestNetNotifier<>(cellInventory.getChannel()), g);

        return g;
    }

    @Override
    public CellState getCellStatus(final int slot) {
        if (isRemote()) {
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

        if (!this.inputInventory.isEmpty()) {
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
        data.writeVarInt(Item.getId(getCell().getItem()));
    }

    @Override
    protected boolean readFromStream(final FriendlyByteBuf data) throws IOException {
        final boolean c = super.readFromStream(data);

        final int oldState = this.state;

        this.state = data.readByte();
        final AEColor oldPaintedColor = this.paintedColor;
        this.paintedColor = AEColor.values()[data.readByte()];
        this.cellItem = Item.byId(data.readVarInt());

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

    @Override
    public <T extends AEKey> IMEMonitor<T> getInventory(IStorageChannel<T> channel) {
        this.updateHandler();

        if (this.cellHandler != null && this.cellHandler.getChannel() == channel) {
            return this.cellHandler.cast(channel);
        }
        return null;
    }

    @Override
    public InternalInventory getInternalInventory() {
        return this.internalInventory;
    }

    @Override
    public void onChangeInventory(final InternalInventory inv, final int slot,
            final ItemStack removed, final ItemStack added) {
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
        if (inv == this.inputInventory && !added.isEmpty()) {
            this.tryToStoreContents();
        }
    }

    @Override
    public InternalInventory getExposedInventoryForSide(@Nonnull Direction side) {
        if (side == this.getForward()) {
            return this.cellInventory;
        } else {
            return this.inputInventory;
        }
    }

    private void tryToStoreContents() {

        if (!this.inputInventory.isEmpty()) {
            this.updateHandler();

            if (this.cellHandler != null && this.cellHandler.getChannel() == StorageChannels.items()) {
                var storage = this.cellHandler.cast(StorageChannels.items());

                var stack = this.inputInventory.getStackInSlot(0);
                if (stack.isEmpty()) {
                    return;
                }

                var inserted = StorageHelper.poweredInsert(this, storage,
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
        if (this.getMainNode().isActive()) {
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
    public void setPriority(final int newValue) {
        this.priority = newValue;
        this.cellHandler = null;
        this.isCached = false; // recalculate the storage cell.

        IStorageProvider.requestUpdate(getMainNode());
    }

    private void blinkCell(int slot) {
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

    public boolean openGui(final Player p) {
        this.updateHandler();
        if (this.cellHandler != null) {
            var ch = StorageCells.getHandler(this.getCell());

            if (ch != null) {
                var chg = StorageCells.getGuiHandler(this.cellHandler.getChannel(), this.getCell());
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
    public boolean recolourBlock(final Direction side, final AEColor newPaintedColor, final Player who) {
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

    private class ChestNetNotifier<T extends AEKey> implements IMEMonitorListener<T> {

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
        public void postChange(IMEMonitor<T> monitor, Iterable<T> change, IActionSource source) {
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

    private class ChestMonitorHandler<T extends AEKey> extends MEMonitorHandler<T> {
        private final ICellInventory<T> cellInventory;

        public ChestMonitorHandler(IMEInventory<T> inventory, ICellInventory<T> cellInventory) {
            super(inventory);
            this.cellInventory = cellInventory;
        }

        @Override
        public long insert(T what, long amount, Actionable mode, IActionSource source) {
            if (source.player().map(player -> !this.securityCheck(player, SecurityPermissions.INJECT)).orElse(false)) {
                return 0;
            }
            return super.insert(what, amount, mode, source);
        }

        private boolean securityCheck(final Player player, final SecurityPermissions requiredPermission) {
            if (ChestBlockEntity.this.getBlockEntity() instanceof IActionHost && requiredPermission != null) {

                final IGridNode gn = ((IActionHost) ChestBlockEntity.this.getBlockEntity()).getActionableNode();
                if (gn != null && gn.getGrid() != null) {
                    final IGrid g = gn.getGrid();
                    final boolean requirePower = false;
                    if (requirePower) {
                        final IEnergyService eg = g.getEnergyService();
                        if (!eg.isNetworkPowered()) {
                            return false;
                        }
                    }

                    final ISecurityService sg = g.getSecurityService();
                    if (sg.hasPermission(player, requiredPermission)) {
                        return true;
                    }
                }

                return false;
            }
            return true;
        }

        @Override
        public long extract(T what, long amount, Actionable mode, IActionSource source) {
            if (source.player().map(player -> !this.securityCheck(player, SecurityPermissions.EXTRACT)).orElse(false)) {
                return 0;
            }
            return super.extract(what, amount, mode, source);
        }
    }

    @Nullable
    public Storage<FluidVariant> getFluidHandler(Direction side) {
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
        public IStorageMonitorable getInventory(IActionSource src) {
            if (Platform.canAccess(ChestBlockEntity.this.getMainNode(), src)) {
                return ChestBlockEntity.this;
            }
            return null;
        }
    }

    private class FluidHandler extends SnapshotParticipant<FluidHandler.QueuedInsert>
            implements InsertionOnlyStorage<FluidVariant> {
        private QueuedInsert queuedInsert;

        private record QueuedInsert(AEFluidKey what, long amount) {
        }

        /**
         * If we accept fluids, simulate that we have an empty tank with 1 bucket capacity at all times.
         */
        private final List<StorageView<FluidVariant>> fakeInputTanks = Collections.singletonList(
                new BlankVariantView<>(FluidVariant.blank(), AEFluidKey.AMOUNT_BUCKET));

        private boolean canAcceptLiquids() {
            return ChestBlockEntity.this.cellHandler != null
                    && ChestBlockEntity.this.cellHandler.getChannel() == StorageChannels.fluids();
        }

        @Override
        public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
            StoragePreconditions.notBlankNotNegative(resource, maxAmount);

            if (queuedInsert != null) {
                return 0; // Can only insert once per action
            }

            ChestBlockEntity.this.updateHandler();
            if (canAcceptLiquids()) {
                var what = AEFluidKey.of(resource);
                var inserted = pushToNetwork(new QueuedInsert(what, maxAmount), false);
                if (inserted > 0) {
                    updateSnapshots(transaction);
                    this.queuedInsert = new QueuedInsert(what, inserted);
                }
                return inserted;
            }
            return 0;
        }

        @Override
        public Iterator<StorageView<FluidVariant>> iterator(TransactionContext transaction) {
            if (canAcceptLiquids()) {
                return fakeInputTanks.iterator();
            } else {
                return Collections.emptyIterator();
            }
        }

        @Override
        protected final QueuedInsert createSnapshot() {
            return queuedInsert;
        }

        @Override
        protected final void readSnapshot(QueuedInsert snapshot) {
            this.queuedInsert = snapshot;
        }

        @Override
        protected final void onFinalCommit() {
            pushToNetwork(queuedInsert, true);
            queuedInsert = null;
        }

        private long pushToNetwork(QueuedInsert queuedInsert, boolean commit) {
            ChestBlockEntity.this.updateHandler();
            if (canAcceptLiquids()) {
                final Actionable mode = commit ? Actionable.MODULATE : Actionable.SIMULATE;
                return StorageHelper.poweredInsert(
                        ChestBlockEntity.this,
                        ChestBlockEntity.this.cellHandler.cast(StorageChannels.fluids()),
                        queuedInsert.what(),
                        queuedInsert.amount(),
                        ChestBlockEntity.this.mySrc,
                        mode);
            }
            return 0;
        }
    }

    private class InputInventoryFilter implements IAEItemFilter {
        @Override
        public boolean allowExtract(InternalInventory inv, int slot, int amount) {
            return false;
        }

        @Override
        public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
            if (ChestBlockEntity.this.isPowered()) {
                ChestBlockEntity.this.updateHandler();
                return ChestBlockEntity.this.cellHandler != null
                        && ChestBlockEntity.this.cellHandler.getChannel() == StorageChannels.items();
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
    public ItemStack getItemStackRepresentation() {
        return AEBlocks.CHEST.stack();
    }

    @Override
    public MenuType<?> getMenuType() {
        this.updateHandler();
        if (this.cellHandler != null) {
            if (this.cellHandler.getChannel() == StorageChannels.items()) {
                return ItemTerminalMenu.TYPE;
            }
            if (this.cellHandler.getChannel() == StorageChannels.fluids()) {
                return FluidTerminalMenu.TYPE;
            }
        }
        return null;
    }

}
