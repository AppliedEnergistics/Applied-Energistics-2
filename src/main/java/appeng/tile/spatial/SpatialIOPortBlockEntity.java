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

package appeng.tile.spatial;

import javax.annotation.Nonnull;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.YesNo;
import appeng.api.implementations.items.ISpatialStorageCell;
import appeng.api.networking.GridFlags;
import appeng.api.networking.events.GridSpatialEvent;
import appeng.api.networking.spatial.ISpatialService;
import appeng.api.util.AECableType;
import appeng.hooks.ticking.TickHandler;
import appeng.tile.grid.AENetworkInvBlockEntity;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.IWorldRunnable;
import appeng.util.inv.InvOperation;
import appeng.util.inv.WrapperFilteredItemHandler;
import appeng.util.inv.filter.IAEItemFilter;

public class SpatialIOPortBlockEntity extends AENetworkInvBlockEntity {

    private final AppEngInternalInventory inv = new AppEngInternalInventory(this, 2);
    private final IItemHandler invExt = new WrapperFilteredItemHandler(this.inv, new SpatialIOFilter());
    private YesNo lastRedstoneState = YesNo.UNDECIDED;

    private final IWorldRunnable transitionCallback = world -> transition();

    public SpatialIOPortBlockEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState blockState) {
        super(tileEntityTypeIn, pos, blockState);
        this.getMainNode().setFlags(GridFlags.REQUIRE_CHANNEL);
    }

    @Override
    public CompoundTag save(final CompoundTag data) {
        super.save(data);
        data.putInt("lastRedstoneState", this.lastRedstoneState.ordinal());
        return data;
    }

    @Override
    public void load(final CompoundTag data) {
        super.load(data);
        if (data.contains("lastRedstoneState")) {
            this.lastRedstoneState = YesNo.values()[data.getInt("lastRedstoneState")];
        }
    }

    public boolean getRedstoneState() {
        if (this.lastRedstoneState == YesNo.UNDECIDED) {
            this.updateRedstoneState();
        }

        return this.lastRedstoneState == YesNo.YES;
    }

    public void updateRedstoneState() {
        final YesNo currentState = this.level.getBestNeighborSignal(this.worldPosition) != 0 ? YesNo.YES : YesNo.NO;
        if (this.lastRedstoneState != currentState) {
            this.lastRedstoneState = currentState;
            if (this.lastRedstoneState == YesNo.YES) {
                this.triggerTransition();
            }
        }
    }

    private void triggerTransition() {
        if (!isRemote()) {
            final ItemStack cell = this.inv.getStackInSlot(0);
            if (this.isSpatialCell(cell)) {
                TickHandler.instance().addCallable(null, transitionCallback);// this needs to be cross world synced.
            }
        }
    }

    private boolean isSpatialCell(final ItemStack cell) {
        if (!cell.isEmpty() && cell.getItem() instanceof ISpatialStorageCell) {
            final ISpatialStorageCell sc = (ISpatialStorageCell) cell.getItem();
            return sc != null && sc.isSpatialStorage(cell);
        }
        return false;
    }

    private void transition() throws Exception {
        if (!(this.level instanceof ServerLevel serverWorld)) {
            return;
        }

        final ItemStack cell = this.inv.getStackInSlot(0);
        if (!this.isSpatialCell(cell) || !this.inv.getStackInSlot(1).isEmpty()) {
            return;
        }

        final ISpatialStorageCell sc = (ISpatialStorageCell) cell.getItem();

        getMainNode().ifPresent((grid, node) -> {
            var spc = grid.getService(ISpatialService.class);
            if (!spc.hasRegion() || !spc.isValidRegion()) {
                return;
            }

            var energy = grid.getEnergyService();
            final double req = spc.requiredPower();
            final double pr = energy.extractAEPower(req, Actionable.SIMULATE, PowerMultiplier.CONFIG);
            if (Math.abs(pr - req) < req * 0.001) {
                var evt = grid.postEvent(new GridSpatialEvent(getLevel(), getBlockPos(), req));
                if (!evt.isTransitionPrevented()) {
                    // Prefer player id from security system, but if unavailable, use the
                    // player who placed the grid node (if any)
                    int playerId;
                    if (grid.getSecurityService().isAvailable()) {
                        playerId = grid.getSecurityService().getOwner();
                    } else {
                        playerId = node.getOwningPlayerId();
                    }

                    boolean success = sc.doSpatialTransition(cell, serverWorld, spc.getMin(), spc.getMax(),
                            playerId);
                    if (success) {
                        energy.extractAEPower(req, Actionable.MODULATE, PowerMultiplier.CONFIG);
                        this.inv.setStackInSlot(0, ItemStack.EMPTY);
                        this.inv.setStackInSlot(1, cell);
                    }
                }
            }
        });
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.SMART;
    }

    @Override
    protected @Nonnull IItemHandler getItemHandlerForSide(@Nonnull Direction side) {
        return this.invExt;
    }

    @Override
    public IItemHandler getInternalInventory() {
        return this.inv;
    }

    @Override
    public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc,
            final ItemStack removed, final ItemStack added) {

    }

    private class SpatialIOFilter implements IAEItemFilter {
        @Override
        public boolean allowExtract(IItemHandler inv, int slot, int amount) {
            return slot == 1;
        }

        @Override
        public boolean allowInsert(IItemHandler inv, int slot, ItemStack stack) {
            return slot == 0 && SpatialIOPortBlockEntity.this.isSpatialCell(stack);
        }

    }
}
