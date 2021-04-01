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

import appeng.tile.AEBaseTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import appeng.api.implementations.tiles.ICrankable;

public class CrankTileEntity extends AEBaseTileEntity implements ITickableTileEntity {

    private final int ticksPerRotation = 18;

    // sided values..
    private float visibleRotation = 0; // This is in degrees
    private int charge = 0;

    private int hits = 0;
    private int rotation = 0;

    public CrankTileEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    @Override
    public void tick() {
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
        if (isRemote()) {
            return null;
        }

        final Direction grinder = this.getUp().getOpposite();
        final TileEntity te = this.world.getTileEntity(this.pos.offset(grinder));
        if (te instanceof ICrankable) {
            return (ICrankable) te;
        }
        return null;
    }

    @Override
    protected boolean readFromStream(final PacketBuffer data) throws IOException {
        final boolean c = super.readFromStream(data);
        this.rotation = data.readInt();
        return c;
    }

    @Override
    protected void writeToStream(final PacketBuffer data) throws IOException {
        super.writeToStream(data);
        data.writeInt(this.rotation);
    }

    @Override
    public void setOrientation(final Direction inForward, final Direction inUp) {
        super.setOrientation(inForward, inUp);
        final BlockState state = this.world.getBlockState(this.pos);
        state.getBlock().neighborChanged(state, this.world, this.pos, state.getBlock(), this.pos, false);
    }

    /**
     * return true if this should count towards stats.
     */
    public boolean power() {
        if (isRemote()) {
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
                        this.world.destroyBlock(this.pos, false);
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
