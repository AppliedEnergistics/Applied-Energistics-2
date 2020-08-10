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

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import appeng.api.networking.events.MENetworkBootingStatusChange;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.parts.IPartModel;
import appeng.items.parts.PartModels;
import appeng.me.GridAccessException;
import appeng.util.Platform;

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

    @MENetworkEventSubscribe
    public void changeStateA(final MENetworkBootingStatusChange bs) {
        this.setNetworkReady();
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
        if (this.isOutput() && this.getProxy().isActive()) {
            final int newPower = (Integer) o;
            if (this.power != newPower) {
                this.power = newPower;
                this.notifyNeighbors();
            }
        }
        this.recursive = false;
    }

    private void notifyNeighbors() {
        final World world = this.getTile().getWorld();

        Platform.notifyBlocksOfNeighbors(world, this.getTile().getPos());

        // and this cause sometimes it can go thought walls.
        for (final Direction face : Direction.values()) {
            Platform.notifyBlocksOfNeighbors(world, this.getTile().getPos().offset(face));
        }
    }

    @MENetworkEventSubscribe
    public void changeStateB(final MENetworkChannelsChanged bs) {
        this.setNetworkReady();
    }

    @MENetworkEventSubscribe
    public void changeStateC(final MENetworkPowerStatusChange bs) {
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
    public void onNeighborUpdate(BlockView w, BlockPos pos, BlockPos neighbor) {
        if (!this.isOutput()) {
            final BlockPos target = this.getTile().getPos().offset(this.getSide().getFacing());

            final BlockState state = this.getTile().getWorld().getBlockState(target);
            final Block b = state.getBlock();
            if (b != null && !this.isOutput()) {
                Direction srcSide = this.getSide().getFacing();
                if (b instanceof RedstoneWireBlock) {
                    srcSide = Direction.UP;
                }

                this.power = b.getWeakRedstonePower(state, this.getTile().getWorld(), target, srcSide);
                this.power = Math.max(this.power,
                        b.getWeakRedstonePower(state, this.getTile().getWorld(), target, srcSide));
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
        try {
            for (final RedstoneP2PTunnelPart rs : this.getOutputs()) {
                rs.putInput(power);
            }
        } catch (final GridAccessException e) {
            // :P
        }
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(this.isPowered(), this.isActive());
    }

}
