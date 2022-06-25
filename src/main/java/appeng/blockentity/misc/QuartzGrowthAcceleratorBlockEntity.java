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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.ids.AETags;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.implementations.blockentities.ICrystalGrowthAccelerator;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.util.AECableType;
import appeng.blockentity.grid.AENetworkBlockEntity;
import appeng.core.AEConfig;

public class QuartzGrowthAcceleratorBlockEntity extends AENetworkBlockEntity
        implements IPowerChannelState, ICrystalGrowthAccelerator {

    private boolean hasPower = false;

    public QuartzGrowthAcceleratorBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.getMainNode().setExposedOnSides(EnumSet.noneOf(Direction.class));
        this.getMainNode().setFlags();
        this.getMainNode().setIdlePowerUsage(8);
        this.getMainNode().addService(IGridTickable.class, new IGridTickable() {
            @Override
            public TickingRequest getTickingRequest(IGridNode node) {
                int speed = AEConfig.instance().getGrowthAcceleratorSpeed();
                return new TickingRequest(speed, speed, false, false);
            }

            @Override
            public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
                onTick();
                return TickRateModulation.SAME;
            }
        });
    }

    private void onTick() {
        if (!getMainNode().isPowered()) {
            return;
        }

        for (var direction : Direction.values()) {
            var adjPos = getBlockPos().relative(direction);
            var adjState = getLevel().getBlockState(adjPos);

            if (!adjState.getBlock().builtInRegistryHolder().is(AETags.GROWTH_ACCELERATABLE)) {
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
        data.writeBoolean(this.getMainNode().isPowered());
    }

    @Override
    public void setOrientation(Direction inForward, Direction inUp) {
        super.setOrientation(inForward, inUp);
        this.getMainNode().setExposedOnSides(EnumSet.of(this.getUp(), this.getUp().getOpposite()));
    }

    @Override
    public void onReady() {
        this.getMainNode().setExposedOnSides(EnumSet.of(this.getUp(), this.getUp().getOpposite()));
        super.onReady();
    }

    @Override
    public boolean isPowered() {
        if (!isClientSide()) {
            return this.getMainNode().isPowered();
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
}
