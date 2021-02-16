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

package appeng.tile.grindstone;

import java.io.IOException;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import appeng.api.implementations.tiles.ICrankable;
import appeng.tile.AEBaseBlockEntity;

public class CrankBlockEntity extends AEBaseBlockEntity implements BlockEntityTicker {

    private final int ticksPerRotation = 18;

    // sided values..
    private float visibleRotation = 0; // This is in degrees
    private int charge = 0;

    private int hits = 0;
    private int rotation = 0;

    public CrankBlockEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn, pos, state);
    }

    @Override
    public void tick(World world, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (this.rotation > 0) {
            this.setVisibleRotation(this.getVisibleRotation() - 360.0f / (this.ticksPerRotation));
            this.charge++;
            if (this.charge >= this.ticksPerRotation) {
                this.charge -= this.ticksPerRotation;
                final ICrankable g = this.getGrinder();
                if (g != null) {
                    g.applyTurn();
                }
            }

            this.rotation--;
        }
    }

    private ICrankable getGrinder() {
        if (isClient()) {
            return null;
        }

        final Direction grinder = this.getUp().getOpposite();
        final BlockEntity te = this.world.getBlockEntity(this.pos.offset(grinder));
        if (te instanceof ICrankable) {
            return (ICrankable) te;
        }
        return null;
    }

    @Override
    protected boolean readFromStream(final PacketByteBuf data) throws IOException {
        final boolean c = super.readFromStream(data);
        this.rotation = data.readInt();
        return c;
    }

    @Override
    protected void writeToStream(final PacketByteBuf data) throws IOException {
        super.writeToStream(data);
        data.writeInt(this.rotation);
    }

    @Override
    public void setOrientation(final Direction inForward, final Direction inUp) {
        super.setOrientation(inForward, inUp);
        final BlockState state = this.world.getBlockState(this.pos);
        state.getBlock().neighborUpdate(state, this.world, this.pos, state.getBlock(), this.pos, false);
    }

    /**
     * return true if this should count towards stats.
     */
    public boolean power() {
        if (isClient()) {
            return false;
        }

        if (this.rotation < 3) {
            final ICrankable g = this.getGrinder();
            if (g != null) {
                if (g.canTurn()) {
                    this.hits = 0;
                    this.rotation += this.ticksPerRotation;
                    this.markForUpdate();
                    return true;
                } else {
                    this.hits++;
                    if (this.hits > 10) {
                        this.world.breakBlock(this.pos, false);
                    }
                }
            }
        }

        return false;
    }

    public float getVisibleRotation() {
        return this.visibleRotation;
    }

    private void setVisibleRotation(final float visibleRotation) {
        this.visibleRotation = visibleRotation;
    }
}
