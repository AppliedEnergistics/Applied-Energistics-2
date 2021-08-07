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

package appeng.blockentity.networking;

import java.util.EnumSet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;

import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.events.GridControllerChange;
import appeng.api.networking.events.GridPowerStorageStateChanged;
import appeng.api.networking.events.GridPowerStorageStateChanged.PowerEventType;
import appeng.api.networking.pathing.ControllerState;
import appeng.api.util.AECableType;
import appeng.block.networking.ControllerBlock;
import appeng.block.networking.ControllerBlock.ControllerBlockState;
import appeng.blockentity.grid.AENetworkPowerBlockEntity;
import appeng.core.Api;
import appeng.util.Platform;
import appeng.util.inv.InvOperation;

public class ControllerBlockEntity extends AENetworkPowerBlockEntity {

    static {
        Api.instance().grid().addNodeOwnerEventHandler(
                GridControllerChange.class,
                ControllerBlockEntity.class,
                ControllerBlockEntity::updateState);
    }

    private boolean isValid = false;

    public ControllerBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
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
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        this.updateState();
    }

    public void onNeighborChange(final boolean force) {
        final boolean xx = this.checkController(this.worldPosition.relative(Direction.EAST))
                && this.checkController(this.worldPosition.relative(Direction.WEST));
        final boolean yy = this.checkController(this.worldPosition.relative(Direction.UP))
                && this.checkController(this.worldPosition.relative(Direction.DOWN));
        final boolean zz = this.checkController(this.worldPosition.relative(Direction.NORTH))
                && this.checkController(this.worldPosition.relative(Direction.SOUTH));

        // int meta = level.getBlockMetadata( xCoord, yCoord, zCoord );
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

        var grid = getMainNode().getGrid();
        if (grid != null) {
            if (grid.getEnergyService().isNetworkPowered()) {
                metaState = ControllerBlockState.online;

                if (grid.getPathingService().getControllerState() == ControllerState.CONTROLLER_CONFLICT) {
                    metaState = ControllerBlockState.conflicted;
                }
            }
        } else {
            metaState = ControllerBlockState.offline;
        }

        if (this.checkController(this.worldPosition)
                && this.level.getBlockState(this.worldPosition)
                        .getValue(ControllerBlock.CONTROLLER_STATE) != metaState) {
            this.level.setBlockAndUpdate(this.worldPosition,
                    this.level.getBlockState(this.worldPosition).setValue(ControllerBlock.CONTROLLER_STATE, metaState));
        }

    }

    @Override
    protected double getFunnelPowerDemand(final double maxReceived) {
        var grid = getMainNode().getGrid();
        if (grid != null) {
            return grid.getEnergyService().getEnergyDemand(maxReceived);
        } else {
            // no grid? use local...
            return super.getFunnelPowerDemand(maxReceived);
        }
    }

    @Override
    protected double funnelPowerIntoStorage(final double power, final Actionable mode) {
        var grid = getMainNode().getGrid();
        if (grid != null) {
            return grid.getEnergyService().injectPower(power, mode);
        } else {
            // no grid? use local...
            return super.funnelPowerIntoStorage(power, mode);
        }
    }

    @Override
    protected void PowerEvent(final PowerEventType x) {
        getMainNode().ifPresent(grid -> grid.postEvent(new GridPowerStorageStateChanged(this, x)));
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
        return Platform.getTickingBlockEntity(getLevel(), pos) instanceof ControllerBlockEntity;
    }
}
