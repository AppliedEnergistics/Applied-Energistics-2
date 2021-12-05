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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.IModelData;

import appeng.api.implementations.blockentities.IChestOrDrive;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNodeListener;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.StorageCells;
import appeng.api.storage.cells.CellState;
import appeng.api.util.AECableType;
import appeng.block.storage.DriveSlotsState;
import appeng.blockentity.grid.AENetworkInvBlockEntity;
import appeng.blockentity.inventory.AppEngCellInventory;
import appeng.client.render.model.DriveModelData;
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

    private static final int SLOT_COUNT = 10;

    private static final int BIT_POWER_MASK = Integer.MIN_VALUE;
    private static final int BIT_STATE_MASK = 0b111111111111111111111111111111;

    private static final int BIT_CELL_STATE_MASK = 0b111;
    private static final int BIT_CELL_STATE_BITS = 3;

    private final AppEngCellInventory inv = new AppEngCellInventory(this, SLOT_COUNT);
    private final DriveWatcher[] invBySlot = new DriveWatcher[SLOT_COUNT];
    private boolean isCached = false;
    private int priority = 0;
    private boolean wasActive = false;
    // This is only used on the client
    private final Item[] cellItems = new Item[SLOT_COUNT];

    /**
     * The state of all cells inside a drive as bitset, using the following format.
     * <p>
     * - Bit 31: power state. 0 = off, 1 = on.
     * <p>
     * - Bit 30: reserved
     * <p>
     * - Bit 29-0: 3 bits for the state of each cell
     * <p>
     * Cell states:
     * <p>
     * - Bit 2-0: {@link CellState} ordinal
     */
    private int state = 0;

    public DriveBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.getMainNode()
                .addService(IStorageProvider.class, this)
                .setFlags(GridFlags.REQUIRE_CHANNEL);
        this.inv.setFilter(new CellValidInventoryFilter());
    }

    @Override
    public void setOrientation(Direction inForward, Direction inUp) {
        super.setOrientation(inForward, inUp);
        this.getMainNode().setExposedOnSides(EnumSet.complementOf(EnumSet.of(inForward)));
    }

    @Override
    protected void writeToStream(FriendlyByteBuf data) {
        super.writeToStream(data);
        this.state = calculateCurrentVisualState();
        data.writeInt(this.state);
        writeCellItemIds(data);
    }

    private void writeCellItemIds(FriendlyByteBuf data) {
        List<ResourceLocation> cellItemIds = new ArrayList<>(getCellCount());
        byte[] bm = new byte[getCellCount()];
        for (int x = 0; x < this.getCellCount(); x++) {
            Item item = getCellItem(x);
            if (item != null && !Objects.equals(Registry.ITEM.getKey(item), Registry.ITEM.getDefaultKey())) {
                ResourceLocation itemId = Registry.ITEM.getKey(item);
                int idx = cellItemIds.indexOf(itemId);
                if (idx == -1) {
                    cellItemIds.add(itemId);
                    bm[x] = (byte) cellItemIds.size(); // We use 1-based in bm[]
                } else {
                    bm[x] = (byte) (1 + idx); // 1-based indexing!!
                }
            }
        }

        // Write out the list of unique cell item ids
        data.writeByte(cellItemIds.size());
        for (ResourceLocation itemId : cellItemIds) {
            data.writeResourceLocation(itemId);
        }
        // Then the lookup table for each slot
        for (int i = 0; i < getCellCount(); i++) {
            data.writeByte(bm[i]);
        }
    }

    @Override
    protected boolean readFromStream(FriendlyByteBuf data) {
        boolean c = super.readFromStream(data);
        final int oldState = this.state;
        this.state = data.readInt();

        c |= this.readCellItemIDs(data);

        return (this.state & BIT_STATE_MASK) != (oldState & BIT_STATE_MASK) || c;
    }

    private boolean readCellItemIDs(FriendlyByteBuf data) {
        int uniqueStrCount = data.readByte();
        String[] uniqueStrs = new String[uniqueStrCount];
        for (int i = 0; i < uniqueStrCount; i++) {
            uniqueStrs[i] = data.readUtf();
        }

        boolean changed = false;
        for (int i = 0; i < getCellCount(); i++) {
            byte idx = data.readByte();

            // an index of 0 indicates the slot is empty
            Item item = null;
            if (idx > 0) {
                --idx;
                String itemId = uniqueStrs[idx];
                item = Registry.ITEM.get(new ResourceLocation(itemId));
            }
            if (cellItems[i] != item) {
                changed = true;
                cellItems[i] = item;
            }
        }

        return changed;
    }

    @Override
    public int getCellCount() {
        return SLOT_COUNT;
    }

    @Nullable
    @Override
    public Item getCellItem(int slot) {
        // Client-side we'll need to actually use the synced state
        if (level == null || level.isClientSide) {
            return cellItems[slot];
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
            final int cellState = this.state >> slot * BIT_CELL_STATE_BITS & BIT_CELL_STATE_MASK;
            return CellState.values()[cellState];
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
            return (this.state & BIT_POWER_MASK) == BIT_POWER_MASK;
        }

        return this.getMainNode().isActive();
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
        int newState = calculateCurrentVisualState();

        if (newState != this.state) {
            this.state = newState;
            this.markForUpdate();
        }
    }

    private int calculateCurrentVisualState() {
        updateState(); // refresh cells

        int newState = 0;

        if (getMainNode().isActive()) {
            newState |= BIT_POWER_MASK;
        }

        for (int x = 0; x < this.getCellCount(); x++) {
            newState |= this.getCellStatus(x).ordinal() << BIT_CELL_STATE_BITS * x;
        }
        return newState;
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {

        var currentActive = getMainNode().isActive();
        if (this.wasActive != currentActive) {
            this.wasActive = currentActive;
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
        if (this.getMainNode().isActive()) {
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
    public IModelData getModelData() {
        return new DriveModelData(getUp(), getForward(), DriveSlotsState.fromChestOrDrive(this));
    }

    public void openMenu(Player player) {
        MenuOpener.open(DriveMenu.TYPE, player, MenuLocators.forBlockEntity(this));
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu) {
        openMenu(player);
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return AEBlocks.DRIVE.stack();
    }
}
