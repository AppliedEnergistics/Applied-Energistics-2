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

package appeng.tile.qnb;

import java.io.IOException;
import java.util.EnumSet;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNodeListener;
import appeng.api.util.AECableType;
import appeng.block.qnb.QnbFormedState;
import appeng.core.definitions.AEBlocks;
import appeng.me.cluster.IAEMultiBlock;
import appeng.me.cluster.implementations.QuantumCalculator;
import appeng.me.cluster.implementations.QuantumCluster;
import appeng.tile.grid.AENetworkInvTileEntity;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.inv.InvOperation;

public class QuantumBridgeTileEntity extends AENetworkInvTileEntity
        implements IAEMultiBlock<QuantumCluster>, ITickableTileEntity {

    public static final ModelProperty<QnbFormedState> FORMED_STATE = new ModelProperty<>();

    private final byte corner = 16;
    private final AppEngInternalInventory internalInventory = new AppEngInternalInventory(this, 1, 1);
    private final byte hasSingularity = 32;
    private final byte powered = 64;

    private final QuantumCalculator calc = new QuantumCalculator(this);
    private byte constructed = -1;
    private QuantumCluster cluster;
    private boolean updateStatus = false;

    public QuantumBridgeTileEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
        this.getMainNode().setExposedOnSides(EnumSet.noneOf(Direction.class));
        this.getMainNode().setFlags(GridFlags.DENSE_CAPACITY);
        this.getMainNode().setIdlePowerUsage(22);
    }

    @Override
    public void tick() {
        if (this.updateStatus) {
            this.updateStatus = false;
            if (this.cluster != null) {
                this.cluster.updateStatus(true);
            }
            this.markForUpdate();
        }
    }

    @Override
    protected void writeToStream(final PacketBuffer data) throws IOException {
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
    protected boolean readFromStream(final PacketBuffer data) throws IOException {
        final boolean c = super.readFromStream(data);
        final int oldValue = this.constructed;
        this.constructed = data.readByte();
        return this.constructed != oldValue || c;
    }

    @Override
    public IItemHandler getInternalInventory() {
        return this.internalInventory;
    }

    @Override
    public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc,
            final ItemStack removed, final ItemStack added) {
        if (this.cluster != null) {
            this.cluster.updateStatus(true);
        }
    }

    @Override
    protected IItemHandler getItemHandlerForSide(Direction side) {
        if (this.isCenter()) {
            return this.internalInventory;
        }
        return EmptyHandler.INSTANCE;
    }

    private boolean isCenter() {
        return getBlockState().matchesBlock(AEBlocks.QUANTUM_LINK.block());
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
    }

    @Override
    public void remove() {
        this.disconnect(false);
        super.remove();
    }

    @Override
    public void disconnect(final boolean affectWorld) {
        if (this.cluster != null) {
            if (!affectWorld) {
                this.cluster.setUpdateStatus(false);
            }

            this.cluster.destroy();
        }

        this.cluster = null;

        if (affectWorld) {
            this.getMainNode().setExposedOnSides(EnumSet.noneOf(Direction.class));
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

    public void updateStatus(final QuantumCluster c, final byte flags, final boolean affectWorld) {
        this.cluster = c;

        if (affectWorld) {
            if (this.constructed != flags) {
                this.constructed = flags;
                this.markForUpdate();
            }

            if (this.isCorner() || this.isCenter()) {
                EnumSet<Direction> sides = EnumSet.copyOf(this.getAdjacentQuantumBridges());
                this.getMainNode().setExposedOnSides(sides);
            } else {
                this.getMainNode().setExposedOnSides(EnumSet.allOf(Direction.class));
            }
        }
    }

    public boolean isCorner() {
        return (this.constructed & this.getCorner()) == this.getCorner() && this.constructed != -1;
    }

    public EnumSet<Direction> getAdjacentQuantumBridges() {
        final EnumSet<Direction> set = EnumSet.noneOf(Direction.class);

        for (final Direction d : Direction.values()) {
            final TileEntity te = this.world.getTileEntity(this.pos.offset(d));
            if (te instanceof QuantumBridgeTileEntity) {
                set.add(d);
            }
        }

        return set;
    }

    public long getQEFrequency() {
        final ItemStack is = this.internalInventory.getStackInSlot(0);
        if (!is.isEmpty()) {
            final CompoundNBT c = is.getTag();
            if (c != null) {
                return c.getLong("freq");
            }
        }
        return 0;
    }

    public boolean isPowered() {
        if (isRemote()) {
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
        if (world instanceof ServerWorld serverWorld) {
            this.calc.updateMultiblockAfterNeighborUpdate(serverWorld, this.pos, fromPos);
        }
    }

    public boolean hasQES() {
        if (this.constructed == -1) {
            return false;
        }
        return (this.constructed & this.hasSingularity) == this.hasSingularity;
    }

    public void breakCluster() {
        // Since breaking the cluster will most likely also update the TE's state,
        // it's essential that we're not working with outdated block-state information,
        // since this particular TE's block might already have been removed (state=air)
        updateContainingBlockInfo();

        if (this.cluster != null) {
            this.cluster.destroy();
        }
    }

    public byte getCorner() {
        return this.corner;
    }

    @Nonnull
    @Override
    public IModelData getModelData() {
        // FIXME must trigger model data updates

        return new ModelDataMap.Builder()
                .withInitial(FORMED_STATE, new QnbFormedState(getAdjacentQuantumBridges(), isCorner(), isPowered()))
                .build();

    }

}
