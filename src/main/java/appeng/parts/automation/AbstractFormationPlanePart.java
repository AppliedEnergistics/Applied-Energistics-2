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

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import appeng.api.config.Actionable;
import appeng.api.config.Settings;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.security.IActionSource;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.cells.ICellContainer;
import appeng.api.storage.cells.ICellInventory;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.IConfigManager;
import appeng.helpers.IPriorityHost;

public abstract class AbstractFormationPlanePart<T extends IAEStack<T>> extends UpgradeablePart
        implements ICellContainer, IPriorityHost, IMEInventory<T> {

    private boolean wasActive = false;
    private int priority = 0;
    protected boolean blocked = false;
    private final PlaneConnectionHelper connectionHelper = new PlaneConnectionHelper(this);

    public AbstractFormationPlanePart(ItemStack is) {
        super(is);
    }

    protected abstract void updateHandler();

    @Override
    protected int getUpgradeSlots() {
        return 5;
    }

    @Override
    public void upgradesChanged() {
        this.updateHandler();
    }

    @Override
    public void updateSetting(final IConfigManager manager, final Settings settingName, final Enum<?> newValue) {
        this.updateHandler();
        this.getHost().markForSave();
    }

    public void stateChanged() {
        final boolean currentActive = this.getMainNode().isActive();
        if (this.wasActive != currentActive) {
            this.wasActive = currentActive;
            this.updateHandler();
            this.getHost().markForUpdate();
        }
    }

    @Override
    protected void onMainNodeStateChanged(IGridNodeListener.State reason) {
        stateChanged();
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {
        connectionHelper.getBoxes(bch);
    }

    public PlaneConnections getConnections() {
        return connectionHelper.getConnections();
    }

    @Override
    public void onNeighborChanged(IBlockReader w, BlockPos pos, BlockPos neighbor) {
        if (pos.relative(this.getSide().getDirection()).equals(neighbor)) {
            final TileEntity te = this.getHost().getTile();
            final AEPartLocation side = this.getSide();

            final BlockPos tePos = te.getBlockPos().relative(side.getDirection());

            this.blocked = !w.getBlockState(tePos).getMaterial().isReplaceable();
        } else {
            connectionHelper.updateConnections();
        }
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 1;
    }

    @Override
    public T extractItems(final T request, final Actionable mode, final IActionSource src) {
        return null;
    }

    @Override
    public IItemList<T> getAvailableItems(final IItemList<T> out) {
        return out;
    }

    @Override
    public void readFromNBT(final CompoundNBT data) {
        super.readFromNBT(data);
        this.priority = data.getInt("priority");
    }

    @Override
    public void writeToNBT(final CompoundNBT data) {
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
        this.updateHandler();
    }

    @Override
    public void blinkCell(final int slot) {
        // :P
    }

    @Override
    public void saveChanges(final ICellInventory<?> cell) {
        // nope!
    }
}
