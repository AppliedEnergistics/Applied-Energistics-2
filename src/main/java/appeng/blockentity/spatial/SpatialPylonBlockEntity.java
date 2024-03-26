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
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.Iterators;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridMultiblock;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.orientation.BlockOrientation;
import appeng.blockentity.grid.AENetworkBlockEntity;
import appeng.me.cluster.IAEMultiBlock;
import appeng.me.cluster.implementations.SpatialPylonCalculator;
import appeng.me.cluster.implementations.SpatialPylonCluster;
import appeng.util.iterators.ChainedIterator;

public class SpatialPylonBlockEntity extends AENetworkBlockEntity implements IAEMultiBlock<SpatialPylonCluster> {

    // The lower 6 bits are used
    public static final ModelProperty<ClientState> STATE = new ModelProperty<>(Objects::nonNull);

    private final SpatialPylonCalculator calc = new SpatialPylonCalculator(this);
    private SpatialPylonCluster cluster;
    private ClientState clientState = ClientState.DEFAULT;

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
        onGridConnectableSidesChanged();
        this.recalculateDisplay();
    }

    @Override
    public Set<Direction> getGridConnectableSides(BlockOrientation orientation) {
        return this.cluster == null ? EnumSet.noneOf(Direction.class) : EnumSet.allOf(Direction.class);
    }

    public void recalculateDisplay() {
        var pos = AxisPosition.NONE;
        var axis = Direction.Axis.X;
        var powered = false;
        var online = false;
        if (this.cluster != null) {
            if (this.cluster.getBoundsMin().equals(this.worldPosition)) {
                pos = AxisPosition.START;
            } else if (this.cluster.getBoundsMax().equals(this.worldPosition)) {
                pos = AxisPosition.END;
            } else {
                pos = AxisPosition.MIDDLE;
            }

            axis = switch (this.cluster.getCurrentAxis()) {
                case X -> Direction.Axis.X;
                case Y -> Direction.Axis.Y;
                case Z -> Direction.Axis.Z;
                default -> axis;
            };

            if (this.getMainNode().isPowered()) {
                powered = true;
            }

            if (this.cluster.isValid() && this.getMainNode().isOnline()) {
                online = true;
            }
        }

        var state = new ClientState(powered, online, pos, axis);
        if (!clientState.equals(state)) {
            this.clientState = state;
            this.markForUpdate();
        }
    }

    @Override
    protected boolean readFromStream(RegistryFriendlyByteBuf data) {
        final boolean c = super.readFromStream(data);
        var state = ClientState.readFromStream(data);
        if (!clientState.equals(state)) {
            clientState = state;
            return true;
        }
        return c;
    }

    @Override
    protected void writeToStream(RegistryFriendlyByteBuf data) {
        super.writeToStream(data);
        clientState.writeToStream(data);
    }

    @Override
    protected void saveVisualState(CompoundTag data) {
        super.saveVisualState(data);
        clientState.writeToNbt(data);
    }

    @Override
    protected void loadVisualState(CompoundTag data) {
        super.loadVisualState(data);
        clientState = ClientState.readFromNbt(data);
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        if (reason != IGridNodeListener.State.GRID_BOOT) {
            this.recalculateDisplay();
        }
    }

    public ClientState getClientState() {
        return clientState;
    }

    @Override
    public ModelData getModelData() {
        // FIXME: Must force model data update on changes, should potentially be moved
        // to block state (?)
        return ModelData.builder().with(STATE, getClientState()).build();
    }

    private Iterator<IGridNode> getMultiblockNodes() {
        if (this.getCluster() == null) {
            return new ChainedIterator<>();
        }
        return Iterators.transform(this.getCluster().getBlockEntities(), SpatialPylonBlockEntity::getGridNode);
    }

    public record ClientState(boolean powered,
            boolean online,

            AxisPosition axisPosition,
            Direction.Axis axis) {

        public static final ClientState DEFAULT = new ClientState(false, false, AxisPosition.NONE, Direction.Axis.X);

        public void writeToStream(FriendlyByteBuf buf) {
            buf.writeBoolean(powered);
            buf.writeBoolean(online);
            buf.writeEnum(axisPosition);
            buf.writeEnum(axis);
        }

        public static ClientState readFromStream(FriendlyByteBuf buf) {
            return new ClientState(
                    buf.readBoolean(),
                    buf.readBoolean(),
                    buf.readEnum(AxisPosition.class),
                    buf.readEnum(Direction.Axis.class));
        }

        public void writeToNbt(CompoundTag tag) {
            tag.putBoolean("powered", powered);
            tag.putBoolean("online", online);
            tag.putString("axisPosition", axisPosition.name());
            tag.putString("axis", axis.name());
        }

        public static ClientState readFromNbt(CompoundTag tag) {
            var powered = tag.getBoolean("powered");
            var online = tag.getBoolean("online");
            var axisPositionName = tag.getString("axisPosition");
            AxisPosition axisPosition;
            try {
                axisPosition = Enum.valueOf(AxisPosition.class, axisPositionName);
            } catch (IllegalArgumentException ignored) {
                axisPosition = DEFAULT.axisPosition;
            }
            var axisName = tag.getString("axis");
            Direction.Axis axis;
            try {
                axis = Enum.valueOf(Direction.Axis.class, axisName);
            } catch (IllegalArgumentException ignored) {
                axis = DEFAULT.axis;
            }
            return new ClientState(
                    powered,
                    online,
                    axisPosition,
                    axis);
        }
    }

    public enum AxisPosition {
        NONE,
        START,
        MIDDLE,
        END
    }
}
