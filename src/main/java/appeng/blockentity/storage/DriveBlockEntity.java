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

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.implementations.blockentities.IChestOrDrive;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNodeListener;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.StorageCells;
import appeng.api.storage.cells.CellState;
import appeng.api.util.AECableType;
import appeng.block.orientation.RelativeSide;
import appeng.blockentity.grid.AENetworkInvBlockEntity;
import appeng.blockentity.inventory.AppEngCellInventory;
import appeng.client.render.model.DriveModelData;
import appeng.core.AELog;
import appeng.core.definitions.AEBlocks;
import appeng.helpers.IPriorityHost;
import appeng.me.storage.DriveWatcher;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.implementations.DriveMenu;
import appeng.menu.locator.MenuLocators;
import appeng.util.inv.filter.IAEItemFilter;

public class DriveBlockEntity extends AENetworkInvBlockEntity
        implements IChestOrDrive, IPriorityHost, IStorageProvider {

    private static final EnumSet<RelativeSide> GRID_CONNECTABLE_SIDES = EnumSet
            .complementOf(EnumSet.of(RelativeSide.FRONT));

    private final AppEngCellInventory inv = new AppEngCellInventory(this, getCellCount());
    private final DriveWatcher[] invBySlot = new DriveWatcher[getCellCount()];
    private boolean isCached = false;
    private int priority = 0;
    private boolean wasOnline = false;
    // This is only used on the client
    private final Item[] clientSideCellItems = new Item[getCellCount()];
    private final CellState[] clientSideCellState = new CellState[getCellCount()];
    private boolean clientSideOnline;

    public DriveBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        getMainNode()
                .addService(IStorageProvider.class, this)
                .setFlags(GridFlags.REQUIRE_CHANNEL);
        inv.setFilter(new CellValidInventoryFilter());

        Arrays.fill(clientSideCellState, CellState.ABSENT);
    }

    @Override
    public Set<RelativeSide> getGridConnectableSides() {
        return GRID_CONNECTABLE_SIDES;
    }

    @Override
    protected void writeToStream(FriendlyByteBuf data) {
        super.writeToStream(data);
        updateClientSideState();

        // Pack the enums into an int of 3 bit per cell state, using 30 bits total
        int packedState = 0;
        for (int i = 0; i < getCellCount(); i++) {
            packedState |= clientSideCellState[i].ordinal() << (i * 3);
        }
        // Then pack the online state into bit 31
        if (clientSideOnline) {
            packedState |= 1 << 31;
        }
        data.writeInt(packedState);

        for (int i = 0; i < getCellCount(); i++) {
            data.writeVarInt(BuiltInRegistries.ITEM.getId(getCellItem(i)));
        }
    }

    @Override
    protected void saveVisualState(CompoundTag data) {
        super.saveVisualState(data);

        data.putBoolean("online", isPowered());

        for (int i = 0; i < getCellCount(); i++) {
            var cellItem = getCellItem(i);
            if (cellItem != null) {
                var cellData = new CompoundTag();
                cellData.putString("id", BuiltInRegistries.ITEM.getKey(cellItem).toString());

                var cellState = getCellStatus(i);
                cellData.putString("state", cellState.name().toLowerCase(Locale.ROOT));
                data.put("cell" + i, cellData);
            }
        }
    }

    @Override
    protected boolean readFromStream(FriendlyByteBuf data) {
        var changed = super.readFromStream(data);

        var packedState = data.readInt();
        for (int i = 0; i < getCellCount(); i++) {
            var cellStateOrdinal = (packedState >> (i * 3)) & 0b111;
            var cellState = CellState.values()[cellStateOrdinal];
            if (clientSideCellState[i] != cellState) {
                clientSideCellState[i] = cellState;
                changed = true;
            }
        }

        var online = (packedState & (1 << 31)) != 0;
        if (clientSideOnline != online) {
            clientSideOnline = online;
            changed = true;
        }

        for (int i = 0; i < getCellCount(); i++) {
            var itemId = data.readVarInt();
            Item item = itemId == 0 ? null : BuiltInRegistries.ITEM.byId(itemId);
            if (itemId != 0 && item == Items.AIR) {
                AELog.warn("Received unknown item id from server for disk drive %s: %d", this, itemId);
            }
            if (clientSideCellItems[i] != item) {
                clientSideCellItems[i] = item;
                changed = true;
            }
        }

        return changed;
    }

    @Override
    protected void loadVisualState(CompoundTag data) {
        super.loadVisualState(data);

        clientSideOnline = data.getBoolean("online");

        for (int i = 0; i < getCellCount(); i++) {
            this.clientSideCellItems[i] = null;
            this.clientSideCellState[i] = CellState.ABSENT;

            var tagName = "cell" + i;
            if (data.contains(tagName, Tag.TAG_COMPOUND)) {
                var cellData = data.getCompound(tagName);
                var id = new ResourceLocation(cellData.getString("id"));
                var cellStateName = cellData.getString("state");

                clientSideCellItems[i] = BuiltInRegistries.ITEM.getOptional(id).orElse(null);
                try {
                    clientSideCellState[i] = CellState.valueOf(cellStateName.toUpperCase(Locale.ROOT));
                } catch (IllegalArgumentException e) {
                    AELog.warn("Cannot parse cell state for cell %d: %s", i, cellStateName);
                }
            }
        }
    }

    @Override
    public int getCellCount() {
        return 10;
    }

    @Nullable
    @Override
    public Item getCellItem(int slot) {
        // Client-side we'll need to actually use the synced state
        if (level == null || level.isClientSide) {
            return clientSideCellItems[slot];
        }

        ItemStack stackInSlot = inv.getStackInSlot(slot);
        if (!stackInSlot.isEmpty()) {
            return stackInSlot.getItem();
        }
        return null;
    }

    @Override
    public CellState getCellStatus(int slot) {
        if (isClientSide()) {
            return this.clientSideCellState[slot];
        }

        var handler = this.invBySlot[slot];
        if (handler == null) {
            return CellState.ABSENT;
        }

        return handler.getStatus();
    }

    @Override
    public boolean isPowered() {
        if (isClientSide()) {
            return clientSideOnline;
        }

        return this.getMainNode().isOnline();
    }

    @Override
    public boolean isCellBlinking(int slot) {
        return false;
    }

    @Override
    public void loadTag(CompoundTag data) {
        super.loadTag(data);
        this.isCached = false;
        this.priority = data.getInt("priority");
    }

    @Override
    public void saveAdditional(CompoundTag data) {
        super.saveAdditional(data);
        data.putInt("priority", this.priority);
    }

    private void updateVisualStateIfNeeded() {
        if (updateClientSideState()) {
            this.markForUpdate();
        }
    }

    private boolean updateClientSideState() {
        if (isClientSide()) {
            return false;
        }

        updateState(); // refresh cells

        var changed = false;
        var online = getMainNode().isOnline();
        if (online != this.clientSideOnline) {
            this.clientSideOnline = online;
            changed = true;
        }

        for (int x = 0; x < this.getCellCount(); x++) {
            var cellItem = getCellItem(x);
            if (cellItem != this.clientSideCellItems[x]) {
                this.clientSideCellItems[x] = cellItem;
                changed = true;
            }

            var cellState = this.getCellStatus(x);
            if (cellState != this.clientSideCellState[x]) {
                this.clientSideCellState[x] = cellState;
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        var currentOnline = getMainNode().isOnline();
        if (this.wasOnline != currentOnline) {
            this.wasOnline = currentOnline;
            IStorageProvider.requestUpdate(getMainNode());
            updateVisualStateIfNeeded();
        }
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.SMART;
    }

    @Override
    public InternalInventory getInternalInventory() {
        return this.inv;
    }

    @Override
    public void onChangeInventory(InternalInventory inv, int slot) {
        if (this.isCached) {
            this.isCached = false; // recalculate the storage cell.
            this.updateState();
        }

        IStorageProvider.requestUpdate(getMainNode());

        this.markForUpdate();
    }

    private void updateState() {
        if (!this.isCached) {
            double power = 2.0;
            for (int slot = 0; slot < this.inv.size(); slot++) {
                power += updateStateForSlot(slot);
            }
            this.getMainNode().setIdlePowerUsage(power);

            this.isCached = true;
        }
    }

    // Returns idle power draw of slot
    private double updateStateForSlot(int slot) {
        this.invBySlot[slot] = null;
        this.inv.setHandler(slot, null);

        var is = this.inv.getStackInSlot(slot);
        if (!is.isEmpty()) {
            var cell = StorageCells.getCellInventory(is, this::onCellContentChanged);

            if (cell != null) {
                this.inv.setHandler(slot, cell);

                var driveWatcher = new DriveWatcher(cell, () -> blinkCell(slot));
                this.invBySlot[slot] = driveWatcher;

                return cell.getIdleDrain();
            }
        }

        return 0;
    }

    @Override
    public void onReady() {
        super.onReady();
        this.updateState();
    }

    @Override
    public void mountInventories(IStorageMounts storageMounts) {
        if (this.getMainNode().isOnline()) {
            this.updateState();
            for (var inventory : this.invBySlot) {
                if (inventory != null) {
                    storageMounts.mount(inventory, priority);
                }
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
        this.saveChanges();

        this.isCached = false; // recalculate the storage cell.
        this.updateState();

        IStorageProvider.requestUpdate(getMainNode());
    }

    private void blinkCell(int slot) {
        this.updateVisualStateIfNeeded();
    }

    /**
     * When the content of a storage cell changes, we need to persist it. But instead of taking the performance hit of
     * serializing it to NBT right away, we just queue up a save for the entire BE. As part of saving the BE, the cell
     * will then be serialized to NBT.
     */
    private void onCellContentChanged() {
        this.level.blockEntityChanged(this.worldPosition);
    }

    private static class CellValidInventoryFilter implements IAEItemFilter {

        @Override
        public boolean allowExtract(InternalInventory inv, int slot, int amount) {
            return true;
        }

        @Override
        public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
            return !stack.isEmpty() && StorageCells.isCellHandled(stack);
        }

    }

    @Override
    public DriveModelData getRenderAttachmentData() {
        var cells = new Item[getCellCount()];
        for (int i = 0; i < getCellCount(); i++) {
            cells[i] = getCellItem(i);
        }
        return new DriveModelData(cells);
    }

    public void openMenu(Player player) {
        MenuOpener.open(DriveMenu.TYPE, player, MenuLocators.forBlockEntity(this));
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu) {
        MenuOpener.returnTo(DriveMenu.TYPE, player, MenuLocators.forBlockEntity(this));
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return AEBlocks.DRIVE.stack();
    }
}
