/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.parts.automation;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;

import appeng.api.config.Actionable;
import appeng.api.config.IncludeExclude;
import appeng.api.config.Setting;
import appeng.api.config.Upgrades;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.security.IActionSource;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.AECableType;
import appeng.api.util.IConfigManager;
import appeng.helpers.IPriorityHost;
import appeng.util.prioritylist.IPartitionList;

public abstract class AbstractFormationPlanePart<T extends IAEStack> extends UpgradeablePart
        implements IStorageProvider, IPriorityHost {

    private boolean wasActive = false;
    private int priority = 0;
    private final PlaneConnectionHelper connectionHelper = new PlaneConnectionHelper(this);
    private final IMEInventory<T> inventory = new InWorldStorage();
    private final IStorageChannel<T> channel;
    private IncludeExclude filterMode = IncludeExclude.WHITELIST;
    private IPartitionList<T> filter;

    public AbstractFormationPlanePart(ItemStack is, IStorageChannel<T> channel) {
        super(is);
        this.channel = channel;
        getMainNode().addService(IStorageProvider.class, this);
    }

    protected final void updateFilter() {
        this.filter = createFilter();
        this.filterMode = this.getInstalledUpgrades(Upgrades.INVERTER) > 0
                ? IncludeExclude.BLACKLIST
                : IncludeExclude.WHITELIST;
    }

    @Override
    protected int getUpgradeSlots() {
        return 5;
    }

    @Override
    public void upgradesChanged() {
        this.updateFilter();
    }

    @Override
    public void onSettingChanged(IConfigManager manager, Setting<?> setting) {
        this.getHost().markForSave();
    }

    public void stateChanged() {
        var currentActive = this.getMainNode().isActive();
        if (this.wasActive != currentActive) {
            this.wasActive = currentActive;
            this.remountStorage();
            this.getHost().markForUpdate();
        }
    }

    private void remountStorage() {
        IStorageProvider.requestUpdate(getMainNode());
    }

    @Override
    protected void onMainNodeStateChanged(IGridNodeListener.State reason) {
        stateChanged();
    }

    @Override
    public void getBoxes(IPartCollisionHelper bch) {
        connectionHelper.getBoxes(bch);
    }

    public PlaneConnections getConnections() {
        return connectionHelper.getConnections();
    }

    @Override
    public void onNeighborChanged(BlockGetter level, BlockPos pos, BlockPos neighbor) {
        if (pos.relative(this.getSide()).equals(neighbor)) {
            // The neighbor this plane is facing has changed
            clearBlocked(level, neighbor);
        } else {
            connectionHelper.updateConnections();
        }
    }

    protected abstract void clearBlocked(BlockGetter level, BlockPos pos);

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 1;
    }

    @Override
    public void readFromNBT(final CompoundTag data) {
        super.readFromNBT(data);
        this.priority = data.getInt("priority");
        remountStorage();
    }

    @Override
    public void writeToNBT(final CompoundTag data) {
        super.writeToNBT(data);
        data.putInt("priority", this.getPriority());
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    @Override
    public void setPriority(final int newValue) {
        this.priority = newValue;
        this.getHost().markForSave();
        this.remountStorage();
    }

    @Override
    public void mountInventories(IStorageMounts mounts) {
        if (getMainNode().isActive()) {
            // Update the filter at least once before registering the inventory
            updateFilter();
            mounts.mount(inventory, priority);
        }
    }

    @Override
    public ItemStack getItemStackRepresentation() {
        return getItemStack();
    }

    @Override
    public MenuType<?> getMenuType() {
        return null;
    }

    /**
     * Places the given stacks in-world and returns what couldn't be placed.
     * 
     * @see IMEInventory#injectItems
     */
    protected abstract T placeInWorld(T input, Actionable type);

    /**
     * Creates a partition list to filter stacks being injected into the plane against. If an inverter card is present,
     * it's a blacklist.
     */
    protected abstract IPartitionList<T> createFilter();

    /**
     * Models the block adjacent to this formation plane as storage.
     */
    class InWorldStorage implements IMEInventory<T> {
        @Override
        public T injectItems(T input, Actionable type, IActionSource src) {
            if (filter != null && !filter.matchesFilter(input, filterMode)) {
                return input;
            }

            return placeInWorld(input, type);
        }

        @Override
        public IStorageChannel<T> getChannel() {
            return channel;
        }
    }

}
