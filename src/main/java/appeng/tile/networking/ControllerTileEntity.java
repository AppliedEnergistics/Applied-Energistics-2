/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.tile.networking;

import java.util.EnumSet;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;

import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.events.GridControllerChange;
import appeng.api.networking.events.GridPowerStorageStateChanged;
import appeng.api.networking.events.GridPowerStorageStateChanged.PowerEventType;
import appeng.api.networking.pathing.ControllerState;
import appeng.api.util.AECableType;
import appeng.block.networking.ControllerBlock;
import appeng.block.networking.ControllerBlock.ControllerBlockState;
import appeng.core.Api;
import appeng.me.GridAccessException;
import appeng.tile.grid.AENetworkPowerTileEntity;
import appeng.util.inv.InvOperation;

public class ControllerTileEntity extends AENetworkPowerTileEntity {

    static {
        Api.instance().grid().addNodeOwnerEventHandler(
                GridControllerChange.class,
                ControllerTileEntity.class,
                ControllerTileEntity::updateState);
    }

    private boolean isValid = false;

    public ControllerTileEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
        this.setInternalMaxPower(8000);
        this.setInternalPublicPowerStorage(true);
        this.getMainNode().setIdlePowerUsage(3);
        this.getMainNode().setFlags(GridFlags.CANNOT_CARRY, GridFlags.DENSE_CAPACITY);
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.DENSE_SMART;
    }

    @Override
    public void onReady() {
        this.onNeighborChange(true);
        super.onReady();
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.ActiveChangeReason reason) {
        this.updateState();
    }

    public void onNeighborChange(final boolean force) {
        final boolean xx = this.checkController(this.pos.offset(Direction.EAST))
                && this.checkController(this.pos.offset(Direction.WEST));
        final boolean yy = this.checkController(this.pos.offset(Direction.UP))
                && this.checkController(this.pos.offset(Direction.DOWN));
        final boolean zz = this.checkController(this.pos.offset(Direction.NORTH))
                && this.checkController(this.pos.offset(Direction.SOUTH));

        // int meta = world.getBlockMetadata( xCoord, yCoord, zCoord );
        // boolean hasPower = meta > 0;
        // boolean isConflict = meta == 2;

        final boolean oldValid = this.isValid;

        this.isValid = xx && !yy && !zz || !xx && yy && !zz || !xx && !yy && zz
                || (xx ? 1 : 0) + (yy ? 1 : 0) + (zz ? 1 : 0) <= 1;

        if (oldValid != this.isValid || force) {
            if (this.isValid) {
                this.getMainNode().setExposedOnSides(EnumSet.allOf(Direction.class));
            } else {
                this.getMainNode().setExposedOnSides(EnumSet.noneOf(Direction.class));
            }

            this.updateState();
        }

    }

    private void updateState() {
        if (!this.getMainNode().isReady()) {
            return;
        }

        ControllerBlockState metaState = ControllerBlockState.offline;

        try {
            if (this.getMainNode().getEnergy().isNetworkPowered()) {
                metaState = ControllerBlockState.online;

                if (this.getMainNode().getPath().getControllerState() == ControllerState.CONTROLLER_CONFLICT) {
                    metaState = ControllerBlockState.conflicted;
                }
            }
        } catch (final GridAccessException e) {
            metaState = ControllerBlockState.offline;
        }

        if (this.checkController(this.pos)
                && this.world.getBlockState(this.pos).get(ControllerBlock.CONTROLLER_STATE) != metaState) {
            this.world.setBlockState(this.pos,
                    this.world.getBlockState(this.pos).with(ControllerBlock.CONTROLLER_STATE, metaState));
        }

    }

    @Override
    protected double getFunnelPowerDemand(final double maxReceived) {
        try {
            final IEnergyService grid = this.getMainNode().getEnergy();

            return grid.getEnergyDemand(maxReceived);
        } catch (final GridAccessException e) {
            // no grid? use local...
            return super.getFunnelPowerDemand(maxReceived);
        }
    }

    @Override
    protected double funnelPowerIntoStorage(final double power, final Actionable mode) {
        try {
            final IEnergyService grid = this.getMainNode().getEnergy();
            final double leftOver = grid.injectPower(power, mode);

            return leftOver;
        } catch (final GridAccessException e) {
            // no grid? use local...
            return super.funnelPowerIntoStorage(power, mode);
        }
    }

    @Override
    protected void PowerEvent(final PowerEventType x) {
        try {
            this.getMainNode().getGridOrThrow().postEvent(new GridPowerStorageStateChanged(this, x));
        } catch (final GridAccessException e) {
            // not ready!
        }
    }

    @Override
    public IItemHandler getInternalInventory() {
        return EmptyHandler.INSTANCE;
    }

    @Override
    public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc,
            final ItemStack removed, final ItemStack added) {
    }

    /**
     * Check for a controller at this coordinates as well as is it loaded.
     *
     * @return true if there is a loaded controller
     */
    private boolean checkController(final BlockPos pos) {
        if (this.world.getChunkProvider().canTick(pos)) {
            return this.world.getTileEntity(pos) instanceof ControllerTileEntity;
        }

        return false;
    }
}
