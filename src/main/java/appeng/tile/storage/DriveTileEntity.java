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

package appeng.tile.storage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;

import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.api.networking.GridFlags;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.ICellHandler;
import appeng.api.storage.cells.ICellInventory;
import appeng.api.storage.cells.ICellInventoryHandler;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.block.storage.DriveSlotsState;
import appeng.client.render.model.DriveModelData;
import appeng.container.implementations.DriveContainer;
import appeng.core.Api;
import appeng.core.sync.BasePacket;
import appeng.helpers.IPriorityHost;
import appeng.me.GridAccessException;
import appeng.me.helpers.MachineSource;
import appeng.me.storage.DriveWatcher;
import appeng.tile.grid.AENetworkInvTileEntity;
import appeng.tile.inventory.AppEngCellInventory;
import appeng.util.Platform;
import appeng.util.inv.InvOperation;
import appeng.util.inv.filter.IAEItemFilter;

public class DriveTileEntity extends AENetworkInvTileEntity implements IChestOrDrive, IPriorityHost {

    private static final int BIT_POWER_MASK = Integer.MIN_VALUE;
    private static final int BIT_STATE_MASK = 0b111111111111111111111111111111;

    private static final int BIT_CELL_STATE_MASK = 0b111;
    private static final int BIT_CELL_STATE_BITS = 3;

    private final AppEngCellInventory inv = new AppEngCellInventory(this, 10);
    private final ICellHandler[] handlersBySlot = new ICellHandler[10];
    private final DriveWatcher<IAEItemStack>[] invBySlot = new DriveWatcher[10];
    private final IActionSource mySrc;
    private boolean isCached = false;
    private Map<IStorageChannel<? extends IAEStack<?>>, List<IMEInventoryHandler>> inventoryHandlers;
    private int priority = 0;
    private boolean wasActive = false;
    // This is only used on the client
    private final Item[] cellItems = new Item[10];

    /**
     * The state of all cells inside a drive as bitset, using the following format.
     *
     * - Bit 31: power state. 0 = off, 1 = on.
     *
     * - Bit 30: reserved
     *
     * - Bit 29-0: 3 bits for the state of each cell
     *
     * Cell states:
     *
     * - Bit 2-0: {@link CellState} ordinal
     *
     *
     *
     */
    private int state = 0;

    public DriveTileEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
        this.mySrc = new MachineSource(this);
        this.getProxy().setFlags(GridFlags.REQUIRE_CHANNEL);
        this.inv.setFilter(new CellValidInventoryFilter());
        this.inventoryHandlers = new IdentityHashMap<>();
    }

    @Override
    public void setOrientation(Direction inForward, Direction inUp) {
        super.setOrientation(inForward, inUp);
        this.getProxy().setValidSides(EnumSet.complementOf(EnumSet.of(inForward)));
    }

    @Override
    protected void writeToStream(final PacketBuffer data) throws IOException {
        super.writeToStream(data);
        int newState = 0;

        if (this.getProxy().isActive()) {
            newState |= BIT_POWER_MASK;
        }
        for (int x = 0; x < this.getCellCount(); x++) {
            final int o = this.getCellStatus(x).ordinal();
            final int i = (o << (BIT_CELL_STATE_BITS * x));
            newState |= i;
        }

        data.writeInt(newState);

        writeCellItemIds(data);
    }

    private void writeCellItemIds(PacketBuffer data) {
        List<ResourceLocation> cellItemIds = new ArrayList<>(getCellCount());
        byte[] bm = new byte[getCellCount()];
        for (int x = 0; x < this.getCellCount(); x++) {
            Item item = getCellItem(x);
            if (item != null && item.getRegistryName() != null) {
                ResourceLocation itemId = item.getRegistryName();
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
    protected boolean readFromStream(final PacketBuffer data) throws IOException {
        boolean c = super.readFromStream(data);
        final int oldState = this.state;
        this.state = data.readInt();

        c |= this.readCellItemIDs(data);

        return (this.state & BIT_STATE_MASK) != (oldState & BIT_STATE_MASK) || c;
    }

    private boolean readCellItemIDs(final PacketBuffer data) {
        int uniqueStrCount = data.readByte();
        String[] uniqueStrs = new String[uniqueStrCount];
        for (int i = 0; i < uniqueStrCount; i++) {
            uniqueStrs[i] = data.readString(BasePacket.MAX_STRING_LENGTH);
        }

        boolean changed = false;
        for (int i = 0; i < getCellCount(); i++) {
            byte idx = data.readByte();

            // an index of 0 indicates the slot is empty
            Item item = null;
            if (idx > 0) {
                --idx;
                String itemId = uniqueStrs[idx];
                item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));
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
        return 10;
    }

    @Nullable
    @Override
    public Item getCellItem(int slot) {
        // Client-side we'll need to actually use the synced state
        if (world == null || world.isRemote) {
            return cellItems[slot];
        }

        ItemStack stackInSlot = inv.getStackInSlot(slot);
        if (!stackInSlot.isEmpty()) {
            return stackInSlot.getItem();
        }
        return null;
    }

    @Override
    public CellState getCellStatus(final int slot) {
        if (Platform.isClient()) {
            final int cellState = ((this.state >> (slot * BIT_CELL_STATE_BITS)) & BIT_CELL_STATE_MASK);
            return CellState.values()[cellState];
        }

        final DriveWatcher handler = this.invBySlot[slot];
        if (handler == null) {
            return CellState.ABSENT;
        }

        return handler.getStatus();
    }

    @Override
    public boolean isPowered() {
        if (isRemote()) {
            return (this.state & BIT_POWER_MASK) == BIT_POWER_MASK;
        }

        return this.getProxy().isActive();
    }

    @Override
    public boolean isCellBlinking(final int slot) {
        return false;
    }

    @Override
    public void read(BlockState blockState, final CompoundNBT data) {
        super.read(blockState, data);
        this.isCached = false;
        this.priority = data.getInt("priority");
    }

    @Override
    public CompoundNBT write(final CompoundNBT data) {
        super.write(data);
        data.putInt("priority", this.priority);
        return data;
    }

    @MENetworkEventSubscribe
    public void powerRender(final MENetworkPowerStatusChange c) {
        this.recalculateDisplay();
    }

    private void recalculateDisplay() {
        final boolean currentActive = this.getProxy().isActive();
        int newState = 0;

        if (currentActive) {
            newState |= BIT_POWER_MASK;
        }

        if (this.wasActive != currentActive) {
            this.wasActive = currentActive;
            try {
                this.getProxy().getGrid().postEvent(new MENetworkCellArrayUpdate());
            } catch (final GridAccessException e) {
                // :P
            }
        }

        for (int x = 0; x < this.getCellCount(); x++) {
            newState |= (this.getCellStatus(x).ordinal() << (BIT_CELL_STATE_BITS * x));
        }

        if (newState != this.state) {
            this.state = newState;
            this.markForUpdate();
        }
    }

    @MENetworkEventSubscribe
    public void channelRender(final MENetworkChannelsChanged c) {
        this.recalculateDisplay();
    }

    @Override
    public AECableType getCableConnectionType(final AEPartLocation dir) {
        return AECableType.SMART;
    }

    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this);
    }

    @Override
    public IItemHandler getInternalInventory() {
        return this.inv;
    }

    @Override
    public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc,
            final ItemStack removed, final ItemStack added) {
        if (this.isCached) {
            this.isCached = false; // recalculate the storage cell.
            this.updateState();
        }

        try {
            this.getProxy().getGrid().postEvent(new MENetworkCellArrayUpdate());

            final IStorageGrid gs = this.getProxy().getStorage();
            Platform.postChanges(gs, removed, added, this.mySrc);
        } catch (final GridAccessException ignored) {
        }

        this.markForUpdate();
    }

    private void updateState() {
        if (!this.isCached) {
            final Collection<IStorageChannel<? extends IAEStack<?>>> storageChannels = Api.instance().storage()
                    .storageChannels();
            storageChannels.forEach(channel -> this.inventoryHandlers.put(channel, new ArrayList<>(10)));

            double power = 2.0;

            for (int x = 0; x < this.inv.getSlots(); x++) {
                final ItemStack is = this.inv.getStackInSlot(x);
                this.invBySlot[x] = null;
                this.handlersBySlot[x] = null;

                if (!is.isEmpty()) {
                    this.handlersBySlot[x] = Api.instance().registries().cell().getHandler(is);

                    if (this.handlersBySlot[x] != null) {
                        for (IStorageChannel<? extends IAEStack<?>> channel : storageChannels) {

                            ICellInventoryHandler cell = this.handlersBySlot[x].getCellInventory(is, this, channel);

                            if (cell != null) {
                                this.inv.setHandler(x, cell);
                                power += this.handlersBySlot[x].cellIdleDrain(is, cell);

                                final DriveWatcher<IAEItemStack> ih = new DriveWatcher(cell, is, this.handlersBySlot[x],
                                        this);
                                ih.setPriority(this.priority);
                                this.invBySlot[x] = ih;
                                this.inventoryHandlers.get(channel).add(ih);

                                break;
                            }
                        }
                    }
                }
            }

            this.getProxy().setIdlePowerUsage(power);

            this.isCached = true;
        }
    }

    @Override
    public void onReady() {
        super.onReady();
        this.updateState();
    }

    @Override
    public List<IMEInventoryHandler> getCellArray(final IStorageChannel channel) {
        if (this.getProxy().isActive()) {
            this.updateState();

            return this.inventoryHandlers.get(channel);
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
        this.saveChanges();

        this.isCached = false; // recalculate the storage cell.
        this.updateState();

        try {
            this.getProxy().getGrid().postEvent(new MENetworkCellArrayUpdate());
        } catch (final GridAccessException e) {
            // :P
        }
    }

    @Override
    public void blinkCell(final int slot) {
        this.recalculateDisplay();
    }

    @Override
    public void saveChanges(final ICellInventory<?> cellInventory) {
        this.world.markChunkDirty(this.pos, this);
    }

    private class CellValidInventoryFilter implements IAEItemFilter {

        @Override
        public boolean allowExtract(IItemHandler inv, int slot, int amount) {
            return true;
        }

        @Override
        public boolean allowInsert(IItemHandler inv, int slot, ItemStack stack) {
            return !stack.isEmpty() && Api.instance().registries().cell().isCellHandled(stack);
        }

    }

    @Override
    public ItemStack getItemStackRepresentation() {
        return Api.instance().definitions().blocks().drive().maybeStack(1).orElse(ItemStack.EMPTY);
    }

    @Nonnull
    @Override
    public IModelData getModelData() {
        return new DriveModelData(getUp(), getForward(), DriveSlotsState.fromChestOrDrive(this));
    }

    @Override
    public ContainerType<?> getContainerType() {
        return DriveContainer.TYPE;
    }

}
