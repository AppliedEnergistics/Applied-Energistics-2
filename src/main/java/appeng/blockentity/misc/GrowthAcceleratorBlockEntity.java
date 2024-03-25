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

package appeng.blockentity.misc;

import java.util.EnumSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.config.Actionable;
import appeng.api.ids.AETags;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.implementations.blockentities.ICrankable;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.orientation.BlockOrientation;
import appeng.api.orientation.RelativeSide;
import appeng.api.util.AECableType;
import appeng.block.misc.GrowthAcceleratorBlock;
import appeng.blockentity.grid.AENetworkPowerBlockEntity;
import appeng.core.AEConfig;

public class GrowthAcceleratorBlockEntity extends AENetworkPowerBlockEntity implements IPowerChannelState {

    // Allow storage of up to 10 cranks
    public static final int MAX_STORED_POWER = 10 * CrankBlockEntity.POWER_PER_CRANK_TURN;
    // AE per tick
    private static final int POWER_PER_TICK = 8;

    public GrowthAcceleratorBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        setInternalMaxPower(MAX_STORED_POWER);
        setPowerSides(getGridConnectableSides(getOrientation()));
        getMainNode().setFlags();
        getMainNode().setIdlePowerUsage(POWER_PER_TICK);
        getMainNode().addService(IGridTickable.class, new IGridTickable() {
            @Override
            public TickingRequest getTickingRequest(IGridNode node) {
                int speed = AEConfig.instance().getGrowthAcceleratorSpeed();
                return new TickingRequest(speed, speed, false);
            }

            @Override
            public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
                onTick(ticksSinceLastCall);
                return TickRateModulation.SAME;
            }
        });
    }

    @Override
    public InternalInventory getInternalInventory() {
        return InternalInventory.empty();
    }

    @Override
    public Set<Direction> getGridConnectableSides(BlockOrientation orientation) {
        return orientation.getSides(EnumSet.of(RelativeSide.FRONT, RelativeSide.BACK));
    }

    @Override
    protected void onOrientationChanged(BlockOrientation orientation) {
        super.onOrientationChanged(orientation);
        setPowerSides(getGridConnectableSides(getOrientation()));
    }

    private void onTick(int ticksSinceLastCall) {
        var powered = isPowered();
        if (powered != getBlockState().getValue(GrowthAcceleratorBlock.POWERED)) {
            markForUpdate();
        }

        if (!powered) {
            return;
        }

        // We drain local power in *addition* to network power, which is handled via idle power consumption
        extractAEPower(POWER_PER_TICK * ticksSinceLastCall, Actionable.MODULATE);

        for (var direction : Direction.values()) {
            var adjPos = getBlockPos().relative(direction);
            var adjState = getLevel().getBlockState(adjPos);

            if (!adjState.is(AETags.GROWTH_ACCELERATABLE)) {
                continue;
            }

            adjState.randomTick((ServerLevel) getLevel(), adjPos, getLevel().getRandom());
        }
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        if (reason == IGridNodeListener.State.POWER) {
            this.markForUpdate();
        }
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.COVERED;
    }

    @Override
    public boolean isPowered() {
        if (!isClientSide()) {
            return getMainNode().isPowered() || extractAEPower(POWER_PER_TICK, Actionable.SIMULATE) >= POWER_PER_TICK;
        }

        return this.getBlockState().getValue(GrowthAcceleratorBlock.POWERED);
    }

    @Override
    public boolean isActive() {
        return this.isPowered();
    }

    @org.jetbrains.annotations.Nullable
    public ICrankable getCrankable(Direction direction) {
        if (getPowerSides().contains(direction)) {
            return new Crankable();
        }
        return null;
    }
}
