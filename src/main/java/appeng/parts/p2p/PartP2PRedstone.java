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


import appeng.api.parts.IPartModel;
import appeng.items.parts.PartModels;
import appeng.me.GridAccessException;
import appeng.util.Platform;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;


public class PartP2PRedstone extends PartP2PTunnel<PartP2PRedstone> {

    private static final P2PModels MODELS = new P2PModels("part/p2p/p2p_tunnel_redstone");

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    private int power;
    private boolean recursive = false;

    public PartP2PRedstone(final ItemStack is) {
        super(is);
    }

    private void setNetworkReady() {
        if (this.isOutput()) {
            try {
                int power = 0;
                for (PartP2PRedstone in : this.getInputs()) {
                    if (in != null) {
                        power = Math.max(power, in.power);
                    }
                }
                this.putInput(power);
            } catch (GridAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void putInput(final Object o) {
        if (this.recursive) {
            return;
        }

        this.recursive = true;
        if (this.isOutput()) {
            if (this.getProxy().isActive()) {
                final int newPower = (Integer) o;
                if (this.power != newPower) {
                    this.power = newPower;
                    this.notifyNeighbors();
                }
            } else {
                this.power = 0;
                this.notifyNeighbors();
            }
        }
        this.recursive = false;
    }

    private void notifyNeighbors() {
        final World world = this.getTile().getWorld();

        Platform.notifyBlocksOfNeighbors(world, this.getTile().getPos());

        // and this cause sometimes it can go thought walls.
        for (final EnumFacing face : EnumFacing.VALUES) {
            Platform.notifyBlocksOfNeighbors(world, this.getTile().getPos().offset(face));
        }
    }

    @Override
    public void readFromNBT(final NBTTagCompound tag) {
        super.readFromNBT(tag);
        this.power = tag.getInteger("power");
    }

    @Override
    public void writeToNBT(final NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("power", this.power);
    }

    @Override
    public void onTunnelNetworkChange() {
        this.setNetworkReady();
    }

    public float getPowerDrainPerTick() {
        return 0.5f;
    }

    @Override
    public void onNeighborChanged(IBlockAccess w, BlockPos pos, BlockPos neighbor) {
        if (!this.isOutput()) {
            final BlockPos target = this.getTile().getPos().offset(this.getSide().getFacing());

            final IBlockState state = this.getTile().getWorld().getBlockState(target);
            final Block b = state.getBlock();
            if (b != null && !this.isOutput()) {
                EnumFacing srcSide = this.getSide().getFacing();
                if (b instanceof BlockRedstoneWire) {
                    srcSide = EnumFacing.UP;
                }

                this.power = b.getWeakPower(state, this.getTile().getWorld(), target, srcSide);
                this.power = Math.max(this.power, b.getWeakPower(state, this.getTile().getWorld(), target, srcSide));
                int powerOut = this.power;
                try {
                    for (PartP2PRedstone in : this.getInputs()) {
                        if (in != null) {
                            powerOut = Math.max(in.power, powerOut);
                        }
                    }
                } catch (GridAccessException e) {
                    e.printStackTrace();
                }
                this.sendToOutput(powerOut);
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
            for (final PartP2PRedstone rs : this.getOutputs()) {
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
