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

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.item.FixedItemInv;
import alexiil.mc.lib.attributes.item.LimitedFixedItemInv;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.YesNo;
import appeng.api.implementations.items.ISpatialStorageCell;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.events.MENetworkEvent;
import appeng.api.networking.events.MENetworkSpatialEvent;
import appeng.api.networking.spatial.ISpatialCache;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.hooks.ticking.TickHandler;
import appeng.me.cache.SpatialPylonCache;
import appeng.tile.grid.AENetworkInvTileEntity;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.IWorldCallable;
import appeng.util.Platform;
import appeng.util.inv.InvOperation;

public class SpatialIOPortTileEntity extends AENetworkInvTileEntity implements IWorldCallable<Void> {

    private final AppEngInternalInventory inv = new AppEngInternalInventory(this, 2);
    private final LimitedFixedItemInv invExt;
    private YesNo lastRedstoneState = YesNo.UNDECIDED;

    public SpatialIOPortTileEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
        this.getProxy().setFlags(GridFlags.REQUIRE_CHANNEL);

        invExt = inv.createLimitedFixedInv();
        invExt.getRule(0).disallowExtraction().allowInsertion().filterInserts(this::isSpatialCell);
        invExt.getRule(1).disallowInsertion().allowExtraction();
    }

    @Override
    public CompoundNBT write(final CompoundNBT data) {
        super.write(data);
        data.putInt("lastRedstoneState", this.lastRedstoneState.ordinal());
        return data;
    }

    @Override
    public void read(BlockState blockState, final CompoundNBT data) {
        super.read(blockState, data);
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
        final YesNo currentState = this.world.getRedstonePowerFromNeighbors(this.pos) != 0 ? YesNo.YES : YesNo.NO;
        if (this.lastRedstoneState != currentState) {
            this.lastRedstoneState = currentState;
            if (this.lastRedstoneState == YesNo.YES) {
                this.triggerTransition();
            }
        }
    }

    private void triggerTransition() {
        if (!isRemote()) {
            final ItemStack cell = this.inv.getInvStack(0);
            if (this.isSpatialCell(cell)) {
                TickHandler.instance().addCallable(null, this);// this needs to be cross world synced.
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

    @Override
    public Void call(final World world) throws Exception {
        if (!(this.world instanceof ServerWorld)) {
            return null;
        }
        ServerWorld serverWorld = (ServerWorld) this.world;

        final ItemStack cell = this.inv.getInvStack(0);
        if (this.isSpatialCell(cell) && this.inv.getInvStack(1).isEmpty()) {
            final IGrid gi = this.getProxy().getGrid();
            final IEnergyGrid energy = this.getProxy().getEnergy();

            final ISpatialStorageCell sc = (ISpatialStorageCell) cell.getItem();

            final SpatialPylonCache spc = gi.getCache(ISpatialCache.class);
            if (spc.hasRegion() && spc.isValidRegion()) {
                final double req = spc.requiredPower();
                final double pr = energy.extractAEPower(req, Actionable.SIMULATE, PowerMultiplier.CONFIG);
                if (Math.abs(pr - req) < req * 0.001) {
                    final MENetworkEvent res = gi.postEvent(new MENetworkSpatialEvent(this, req));
                    if (!res.isCanceled()) {
                        // Prefer player id from security system, but if unavailable, use the
                        // player who placed the grid node (if any)
                        int playerId;
                        if (this.getProxy().getSecurity().isAvailable()) {
                            playerId = this.getProxy().getSecurity().getOwner();
                        } else {
                            playerId = this.getProxy().getNode().getPlayerID();
                        }

                        boolean success = sc.doSpatialTransition(cell, serverWorld, spc.getMin(), spc.getMax(),
                                playerId);
                        if (success) {
                            energy.extractAEPower(req, Actionable.MODULATE, PowerMultiplier.CONFIG);
                            this.inv.setInvStack(0, ItemStack.EMPTY, Simulation.ACTION);
                            this.inv.setInvStack(1, cell, Simulation.ACTION);
                        }
                    }
                }
            }
        }

        return null;
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
    protected @Nonnull FixedItemInv getItemHandlerForSide(@Nonnull Direction side) {
        return this.invExt;
    }

    @Override
    public FixedItemInv getInternalInventory() {
        return this.inv;
    }

    @Override
    public void onChangeInventory(final FixedItemInv inv, final int slot, final InvOperation mc,
            final ItemStack removed, final ItemStack added) {

    }

}
