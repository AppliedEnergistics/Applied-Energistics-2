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

package appeng.blockentity.spatial;

import java.util.EnumSet;
import java.util.Iterator;

import com.google.common.collect.Iterators;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridMultiblock;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.blockentity.grid.AENetworkBlockEntity;
import appeng.me.cluster.IAEMultiBlock;
import appeng.me.cluster.implementations.SpatialPylonCalculator;
import appeng.me.cluster.implementations.SpatialPylonCluster;
import appeng.util.iterators.ChainedIterator;

public class SpatialPylonBlockEntity extends AENetworkBlockEntity implements IAEMultiBlock<SpatialPylonCluster> {

    public static final int DISPLAY_END_MIN = 0x01;
    public static final int DISPLAY_END_MAX = 0x02;
    public static final int DISPLAY_MIDDLE = 0x01 + 0x02;
    public static final int DISPLAY_X = 0x04;
    public static final int DISPLAY_Y = 0x08;
    public static final int DISPLAY_Z = 0x04 + 0x08;
    public static final int MB_STATUS = 0x01 + 0x02 + 0x04 + 0x08;

    public static final int DISPLAY_ENABLED = 0x10;
    public static final int DISPLAY_POWERED_ENABLED = 0x20;
    public static final int NET_STATUS = 0x10 + 0x20;

    private final SpatialPylonCalculator calc = new SpatialPylonCalculator(this);
    private int displayBits = 0;
    private SpatialPylonCluster cluster;

    public SpatialPylonBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.getMainNode().setFlags(GridFlags.REQUIRE_CHANNEL, GridFlags.MULTIBLOCK)
                .setIdlePowerUsage(0.5)
                .addService(IGridMultiblock.class, this::getMultiblockNodes);
    }

    @Override
    public void onChunkUnloaded() {
        this.disconnect(false);
        super.onChunkUnloaded();
    }

    @Override
    public void onReady() {
        super.onReady();
        if (level instanceof ServerLevel serverLevel) {
            this.calc.calculateMultiblock(serverLevel, worldPosition);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        // Ensure that this block entity is marked as removed before we try to dismantle the cluster, to prevent updates
        // to the block state.
        this.disconnect(false);
    }

    public void neighborChanged(BlockPos changedPos) {
        if (level instanceof ServerLevel serverLevel) {
            this.calc.updateMultiblockAfterNeighborUpdate(serverLevel, worldPosition, changedPos);
        }
    }

    @Override
    public void disconnect(boolean b) {
        if (this.cluster != null) {
            this.cluster.destroy();
            this.updateStatus(null);
        }
    }

    @Override
    public SpatialPylonCluster getCluster() {
        return this.cluster;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    public void updateStatus(SpatialPylonCluster c) {
        if (this.isRemoved()) {
            // Prevent updating the display, the block state or the node if the block entity was removed.
            // Otherwise, setting the block state will restore the block we just destroyed.
            // Trying to update the node just causes a crash because the node has been removed by now.
            return;
        }

        this.cluster = c;
        this.getMainNode()
                .setExposedOnSides(c == null ? EnumSet.noneOf(Direction.class) : EnumSet.allOf(Direction.class));
        this.recalculateDisplay();
    }

    public void recalculateDisplay() {
        final int oldBits = this.displayBits;

        this.displayBits = 0;

        if (this.cluster != null) {
            if (this.cluster.getBoundsMin().equals(this.worldPosition)) {
                this.displayBits = DISPLAY_END_MIN;
            } else if (this.cluster.getBoundsMax().equals(this.worldPosition)) {
                this.displayBits = DISPLAY_END_MAX;
            } else {
                this.displayBits = DISPLAY_MIDDLE;
            }

            switch (this.cluster.getCurrentAxis()) {
                case X -> this.displayBits |= DISPLAY_X;
                case Y -> this.displayBits |= DISPLAY_Y;
                case Z -> this.displayBits |= DISPLAY_Z;
                default -> this.displayBits = 0;
            }

            if (this.getMainNode().isPowered()) {
                this.displayBits |= DISPLAY_POWERED_ENABLED;
            }

            if (this.cluster.isValid() && this.getMainNode().isPassive()) {
                this.displayBits |= DISPLAY_ENABLED;
            }
        }

        if (oldBits != this.displayBits) {
            this.markForUpdate();
        }
    }

    @Override
    public boolean canBeRotated() {
        return false;
    }

    @Override
    protected boolean readFromStream(FriendlyByteBuf data) {
        final boolean c = super.readFromStream(data);
        final int old = this.displayBits;
        this.displayBits = data.readByte();
        return old != this.displayBits || c;
    }

    @Override
    protected void writeToStream(FriendlyByteBuf data) {
        super.writeToStream(data);
        data.writeByte(this.displayBits);
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        if (reason != IGridNodeListener.State.GRID_BOOT) {
            this.recalculateDisplay();
        }
    }

    public int getDisplayBits() {
        return this.displayBits;
    }

    @Override
    public Object getRenderAttachmentData() {
        return getDisplayBits();
    }

    private Iterator<IGridNode> getMultiblockNodes() {
        if (this.getCluster() == null) {
            return new ChainedIterator<>();
        }
        return Iterators.transform(this.getCluster().getBlockEntities(), SpatialPylonBlockEntity::getGridNode);
    }
}
