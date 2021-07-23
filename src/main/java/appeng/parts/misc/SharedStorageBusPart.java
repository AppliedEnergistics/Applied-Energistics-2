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

package appeng.parts.misc;

import java.util.Collections;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.BlockGetter;

import appeng.api.config.Settings;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.events.GridCellArrayUpdate;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.cells.ICellContainer;
import appeng.api.storage.cells.ICellInventory;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.util.AECableType;
import appeng.api.util.IConfigManager;
import appeng.core.Api;
import appeng.helpers.IPriorityHost;
import appeng.parts.automation.UpgradeablePart;
import net.minecraft.world.item.ItemStack;

/**
 * @author BrockWS
 * @version rv6 - 22/05/2018
 * @since rv6 22/05/2018
 */
public abstract class SharedStorageBusPart extends UpgradeablePart
        implements IGridTickable, ICellContainer, IPriorityHost {
    private boolean wasActive = false;
    private int priority = 0;

    public SharedStorageBusPart(ItemStack is) {
        super(is);
        getMainNode().addService(IGridTickable.class, this);
    }

    protected void updateStatus() {
        final boolean currentActive = this.getMainNode().isActive();
        if (this.wasActive != currentActive) {
            this.wasActive = currentActive;
            this.getHost().markForUpdate();

            this.getMainNode().ifPresent(grid -> grid.postEvent(new GridCellArrayUpdate()));
        }
    }

    @Override
    protected void onMainNodeStateChanged(IGridNodeListener.State reason) {
        updateStatus();
    }

    /**
     * Helper method to get this parts storage channel
     *
     * @return Storage channel
     */
    public IStorageChannel getStorageChannel() {
        return Api.instance().storage().getStorageChannel(IItemStorageChannel.class);
    }

    protected abstract void resetCache();

    protected abstract void resetCache(boolean fullReset);

    @Override
    public List<IMEInventoryHandler> getCellArray(final IStorageChannel channel) {
        return Collections.emptyList();
    }

    @Override
    public void blinkCell(int slot) {
    }

    @Override
    public void saveChanges(ICellInventory<?> cellInventory) {
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    @Override
    public void setPriority(final int newValue) {
        this.priority = newValue;
        this.getHost().markForSave();
        this.resetCache(true);
    }

    @Override
    public void upgradesChanged() {
        super.upgradesChanged();
        this.resetCache(true);
    }

    @Override
    public void updateSetting(final IConfigManager manager, final Settings settingName, final Enum<?> newValue) {
        this.resetCache(true);
        this.getHost().markForSave();
    }

    @Override
    public void onNeighborChanged(BlockGetter w, net.minecraft.core.BlockPos pos, BlockPos neighbor) {
        if (pos.relative(this.getSide().getDirection()).equals(neighbor)) {
            this.resetCache(false);
        }
    }

    @Override
    public void readFromNBT(final CompoundTag data) {
        super.readFromNBT(data);
        this.priority = data.getInt("priority");
    }

    @Override
    public void writeToNBT(final CompoundTag data) {
        super.writeToNBT(data);
        data.putInt("priority", this.priority);
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {
        bch.addBox(3, 3, 15, 13, 13, 16);
        bch.addBox(2, 2, 14, 14, 14, 15);
        bch.addBox(5, 5, 12, 11, 11, 14);
    }

    @Override
    protected int getUpgradeSlots() {
        return 5;
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 4;
    }

}
