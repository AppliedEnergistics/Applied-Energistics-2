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

package appeng.blockentity.qnb;

import java.util.EnumSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNodeListener;
import appeng.api.orientation.BlockOrientation;
import appeng.api.util.AECableType;
import appeng.block.qnb.QnbFormedState;
import appeng.blockentity.ServerTickingBlockEntity;
import appeng.blockentity.grid.AENetworkInvBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.me.cluster.IAEMultiBlock;
import appeng.me.cluster.implementations.QuantumCalculator;
import appeng.me.cluster.implementations.QuantumCluster;
import appeng.util.inv.AppEngInternalInventory;

public class QuantumBridgeBlockEntity extends AENetworkInvBlockEntity
        implements IAEMultiBlock<QuantumCluster>, ServerTickingBlockEntity {

    private final byte corner = 16;
    private final AppEngInternalInventory internalInventory = new AppEngInternalInventory(this, 1, 1);
    private final byte hasSingularity = 32;
    private final byte powered = 64;

    private final QuantumCalculator calc = new QuantumCalculator(this);
    private byte constructed = -1;
    private QuantumCluster cluster;
    private boolean updateStatus = false;

    public QuantumBridgeBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.getMainNode().setFlags(GridFlags.DENSE_CAPACITY);
        this.getMainNode().setIdlePowerUsage(22);
    }

    @Override
    public Set<Direction> getGridConnectableSides(BlockOrientation orientation) {
        if (!isFormed()) {
            return EnumSet.noneOf(Direction.class);
        }

        if (this.isCorner() || this.isCenter()) {
            return this.getAdjacentQuantumBridges();
        } else {
            return EnumSet.allOf(Direction.class);
        }
    }

    @Override
    public void serverTick() {
        if (this.updateStatus) {
            this.updateStatus = false;
            if (this.cluster != null) {
                this.cluster.updateStatus(true);
            }
            this.markForUpdate();
        }
    }

    @Override
    protected void writeToStream(FriendlyByteBuf data) {
        super.writeToStream(data);
        int out = this.constructed;

        if (!this.internalInventory.getStackInSlot(0).isEmpty() && this.constructed != -1) {
            out |= this.hasSingularity;
        }

        if (this.getMainNode().isActive() && this.constructed != -1) {
            out |= this.powered;
        }

        data.writeByte((byte) out);
    }

    @Override
    protected boolean readFromStream(FriendlyByteBuf data) {
        final boolean c = super.readFromStream(data);
        final int oldValue = this.constructed;
        this.constructed = data.readByte();
        return this.constructed != oldValue || c;
    }

    @Override
    public InternalInventory getInternalInventory() {
        return this.internalInventory;
    }

    @Override
    public void onChangeInventory(InternalInventory inv, int slot) {
        if (this.cluster != null) {
            this.cluster.updateStatus(true);
        }
    }

    @Override
    public InternalInventory getExposedInventoryForSide(Direction side) {
        if (this.isCenter()) {
            return this.internalInventory;
        }
        return InternalInventory.empty();
    }

    private boolean isCenter() {
        return getBlockState().is(AEBlocks.QUANTUM_LINK.block());
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        this.updateStatus = true;
    }

    @Override
    public void onChunkUnloaded() {
        this.disconnect(false);
        super.onChunkUnloaded();
    }

    @Override
    public void onReady() {
        super.onReady();

        var quantumRing = AEBlocks.QUANTUM_RING;

        if (this.getBlockState().getBlock() == quantumRing.block()) {
            this.getMainNode().setVisualRepresentation(quantumRing.stack());
        }

        this.updateStatus = true;
    }

    @Override
    public void setRemoved() {
        this.disconnect(false);
        super.setRemoved();
    }

    @Override
    public void disconnect(boolean affectWorld) {
        if (this.cluster != null) {
            if (!affectWorld) {
                this.cluster.setUpdateStatus(false);
            }

            this.cluster.destroy();
        }

        this.cluster = null;

        if (affectWorld) {
            onGridConnectableSidesChanged();
        }
    }

    @Override
    public QuantumCluster getCluster() {
        return this.cluster;
    }

    @Override
    public boolean isValid() {
        return !this.isRemoved();
    }

    public void updateStatus(QuantumCluster c, byte flags, boolean affectWorld) {
        this.cluster = c;

        if (affectWorld) {
            if (this.constructed != flags) {
                this.constructed = flags;
                this.markForUpdate();
            }

            onGridConnectableSidesChanged();
        }
    }

    public boolean isCorner() {
        return (this.constructed & this.getCorner()) == this.getCorner() && this.constructed != -1;
    }

    public EnumSet<Direction> getAdjacentQuantumBridges() {
        var set = EnumSet.noneOf(Direction.class);

        if (level != null) {
            for (var d : Direction.values()) {
                var te = this.level.getBlockEntity(this.worldPosition.relative(d));
                if (te instanceof QuantumBridgeBlockEntity) {
                    set.add(d);
                }
            }
        }

        return set;
    }

    public long getQEFrequency() {
        final ItemStack is = this.internalInventory.getStackInSlot(0);
        if (!is.isEmpty()) {
            final CompoundTag c = is.getTag();
            if (c != null) {
                return c.getLong("freq");
            }
        }
        return 0;
    }

    public boolean isPowered() {
        if (isClientSide()) {
            return (this.constructed & this.powered) == this.powered && this.constructed != -1;
        }

        var node = getMainNode().getNode();
        return node != null && node.isPowered();
    }

    public boolean isFormed() {
        return this.constructed != -1;
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.DENSE_SMART;
    }

    public void neighborUpdate(BlockPos fromPos) {
        if (level instanceof ServerLevel serverLevel) {
            this.calc.updateMultiblockAfterNeighborUpdate(serverLevel, this.worldPosition, fromPos);
        }
    }

    public boolean hasQES() {
        if (this.constructed == -1) {
            return false;
        }
        return (this.constructed & this.hasSingularity) == this.hasSingularity;
    }

    public void breakClusterOnRemove() {
        if (this.cluster != null) {
            // Prevents cluster.destroy() from changing the block state back to an unformed QNB,
            // because that would undo the removal.
            this.remove = true;
            this.cluster.destroy();
        }
    }

    public byte getCorner() {
        return this.corner;
    }

    @Override
    public QnbFormedState getRenderAttachmentData() {
        return new QnbFormedState(getAdjacentQuantumBridges(), isCorner(), isPowered());
    }

}
