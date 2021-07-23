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

package appeng.parts.p2p;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;

import appeng.api.networking.IGridNodeListener;
import appeng.api.parts.IPartModel;
import appeng.items.parts.PartModels;
import appeng.util.Platform;
import net.minecraft.world.level.block.Block;

public class RedstoneP2PTunnelPart extends P2PTunnelPart<RedstoneP2PTunnelPart> {

    private static final P2PModels MODELS = new P2PModels("part/p2p/p2p_tunnel_redstone");

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    private int power;
    private boolean recursive = false;

    public RedstoneP2PTunnelPart(final ItemStack is) {
        super(is);
    }

    @Override
    protected float getPowerDrainPerTick() {
        return 0.5f;
    }

    private void setNetworkReady() {
        if (this.isOutput()) {
            final RedstoneP2PTunnelPart in = this.getInput();
            if (in != null) {
                this.putInput(in.power);
            }
        }
    }

    private void putInput(final Object o) {
        if (this.recursive) {
            return;
        }

        this.recursive = true;
        if (this.isOutput() && this.getMainNode().isActive()) {
            final int newPower = (Integer) o;
            if (this.power != newPower) {
                this.power = newPower;
                this.notifyNeighbors();
            }
        }
        this.recursive = false;
    }

    private void notifyNeighbors() {
        final Level world = this.getTile().getLevel();

        Platform.notifyBlocksOfNeighbors(world, this.getTile().getBlockPos());

        // and this cause sometimes it can go thought walls.
        for (final Direction face : Direction.values()) {
            Platform.notifyBlocksOfNeighbors(world, this.getTile().getBlockPos().relative(face));
        }
    }

    @Override
    protected void onMainNodeStateChanged(IGridNodeListener.State reason) {
        super.onMainNodeStateChanged(reason);
        this.setNetworkReady();
    }

    @Override
    public void readFromNBT(final CompoundTag tag) {
        super.readFromNBT(tag);
        this.power = tag.getInt("power");
    }

    @Override
    public void writeToNBT(final CompoundTag tag) {
        super.writeToNBT(tag);
        tag.putInt("power", this.power);
    }

    @Override
    public void onTunnelNetworkChange() {
        this.setNetworkReady();
    }

    @Override
    public void onNeighborChanged(BlockGetter w, net.minecraft.core.BlockPos pos, BlockPos neighbor) {
        if (!this.isOutput()) {
            final BlockPos target = this.getTile().getBlockPos().relative(this.getSide().getDirection());

            final BlockState state = this.getTile().getLevel().getBlockState(target);
            final Block b = state.getBlock();
            if (b != null && !this.isOutput()) {
                Direction srcSide = this.getSide().getDirection();
                if (b instanceof RedStoneWireBlock) {
                    srcSide = Direction.UP;
                }

                this.power = b.getSignal(state, this.getTile().getLevel(), target, srcSide);
                this.power = Math.max(this.power, b.getSignal(state, this.getTile().getLevel(), target, srcSide));
                this.sendToOutput(this.power);
            } else {
                this.sendToOutput(0);
            }
        }
    }

    @Override
    public boolean canConnectRedstone() {
        return true;
    }

    @Override
    public int isProvidingStrongPower() {
        return this.isOutput() ? this.power : 0;
    }

    @Override
    public int isProvidingWeakPower() {
        return this.isOutput() ? this.power : 0;
    }

    private void sendToOutput(final int power) {
        for (final RedstoneP2PTunnelPart rs : this.getOutputs()) {
            rs.putInput(power);
        }
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(this.isPowered(), this.isActive());
    }

}
