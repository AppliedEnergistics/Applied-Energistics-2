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
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.ids.AETags;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.implementations.blockentities.ICrankable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.util.AECableType;
import appeng.block.orientation.BlockOrientation;
import appeng.block.orientation.RelativeSide;
import appeng.blockentity.grid.AENetworkBlockEntity;
import appeng.core.AEConfig;

public class QuartzGrowthAcceleratorBlockEntity extends AENetworkBlockEntity implements IPowerChannelState {

    // Allow storage of up to 10 cranks
    public static final int MAX_STORED_POWER = 10 * CrankBlockEntity.POWER_PER_CRANK_TURN;
    private static final int POWER_PER_TICK = 8;

    private boolean hasPower = false;

    // For cranking!
    private float storedPower;

    public QuartzGrowthAcceleratorBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        getMainNode().setFlags();
        getMainNode().setIdlePowerUsage(POWER_PER_TICK);
        getMainNode().addService(IGridTickable.class, new IGridTickable() {
            @Override
            public TickingRequest getTickingRequest(IGridNode node) {
                int speed = AEConfig.instance().getGrowthAcceleratorSpeed();
                return new TickingRequest(speed, speed, false, false);
            }

            @Override
            public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
                onTick(ticksSinceLastCall);
                return TickRateModulation.SAME;
            }
        });
    }

    @Override
    public Set<Direction> getGridConnectableSides(BlockOrientation orientation) {
        return orientation.getSides(EnumSet.of(RelativeSide.FRONT, RelativeSide.BACK));
    }

    private void onTick(int ticksSinceLastCall) {
        // We drain local power in *addition* to network power, which is handled via idle power consumption
        if (storedPower > 0) {
            storedPower -= POWER_PER_TICK * Math.max(1, ticksSinceLastCall);
            if (storedPower <= 0) {
                storedPower = 0;
                markForUpdate();
            }
        } else if (!getMainNode().isPowered()) {
            return;
        }

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
    public boolean readFromStream(FriendlyByteBuf data) {
        final boolean c = super.readFromStream(data);
        final boolean hadPower = this.isPowered();
        this.setPowered(data.readBoolean());
        return this.isPowered() != hadPower || c;
    }

    @Override
    public void writeToStream(FriendlyByteBuf data) {
        super.writeToStream(data);
        data.writeBoolean(getMainNode().isPowered());
    }

    @Override
    public boolean isPowered() {
        if (!isClientSide()) {
            return getMainNode().isPowered() || storedPower > 0;
        }

        return this.hasPower;
    }

    @Override
    public boolean isActive() {
        return this.isPowered();
    }

    private void setPowered(boolean hasPower) {
        this.hasPower = hasPower;
    }

    /**
     * Allow cranking from the top or bottom.
     */
    @org.jetbrains.annotations.Nullable
    public ICrankable getCrankable(Direction direction) {
        if (direction == getUp() || direction == getUp().getOpposite()) {
            return new Crankable();
        }
        return null;
    }

    class Crankable implements ICrankable {
        @Override
        public boolean canTurn() {
            return storedPower < MAX_STORED_POWER;
        }

        @Override
        public void applyTurn() {
            if (isClientSide()) {
                return; // Only apply crank-turns server-side
            }

            // Cranking will always add enough power for at least one tick,
            // so we should send a transition from unpowered to powered to clients.
            boolean needsUpdate = !isPowered();

            storedPower = Math.min(MAX_STORED_POWER, storedPower + CrankBlockEntity.POWER_PER_CRANK_TURN);

            if (needsUpdate) {
                markForUpdate();
            }
        }
    }
}
