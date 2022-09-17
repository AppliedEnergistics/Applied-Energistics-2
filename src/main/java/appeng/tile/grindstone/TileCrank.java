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


import appeng.api.implementations.tiles.ICrankable;
import appeng.helpers.ICustomCollision;
import appeng.tile.AEBaseTile;
import appeng.util.Platform;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.io.IOException;
import java.util.Collections;
import java.util.List;


public class TileCrank extends AEBaseTile implements ICustomCollision, ITickable {

    private final int ticksPerRotation = 18;

    // sided values..
    private float visibleRotation = 0;
    private int charge = 0;

    private int hits = 0;
    private int rotation = 0;

    @Override
    public void update() {
        if (this.rotation > 0) {
            this.setVisibleRotation(this.getVisibleRotation() - 360 / (this.ticksPerRotation));
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
        if (Platform.isClient()) {
            return null;
        }

        final EnumFacing grinder = this.getUp().getOpposite();
        final TileEntity te = this.world.getTileEntity(this.pos.offset(grinder));
        if (te instanceof ICrankable) {
            return (ICrankable) te;
        }
        return null;
    }

    @Override
    protected boolean readFromStream(final ByteBuf data) throws IOException {
        final boolean c = super.readFromStream(data);
        this.rotation = data.readInt();
        return c;
    }

    @Override
    protected void writeToStream(final ByteBuf data) throws IOException {
        super.writeToStream(data);
        data.writeInt(this.rotation);
    }

    @Override
    public void setOrientation(final EnumFacing inForward, final EnumFacing inUp) {
        super.setOrientation(inForward, inUp);
        final IBlockState state = this.world.getBlockState(this.pos);
        this.getBlockType().neighborChanged(state, this.world, this.pos, state.getBlock(), this.pos);
    }

    @Override
    public boolean requiresTESR() {
        return true;
    }

    /**
     * return true if this should count towards stats.
     */
    public boolean power() {
        if (Platform.isClient()) {
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

    @Override
    public Iterable<AxisAlignedBB> getSelectedBoundingBoxesFromPool(final World w, final BlockPos pos, final Entity thePlayer, final boolean b) {
        final double xOff = -0.15 * this.getUp().getFrontOffsetX();
        final double yOff = -0.15 * this.getUp().getFrontOffsetY();
        final double zOff = -0.15 * this.getUp().getFrontOffsetZ();
        return Collections.singletonList(new AxisAlignedBB(xOff + 0.15, yOff + 0.15, zOff + 0.15, xOff + 0.85, yOff + 0.85, zOff + 0.85));
    }

    @Override
    public void addCollidingBlockToList(final World w, final BlockPos pos, final AxisAlignedBB bb, final List<AxisAlignedBB> out, final Entity e) {
        final double xOff = -0.15 * this.getUp().getFrontOffsetX();
        final double yOff = -0.15 * this.getUp().getFrontOffsetY();
        final double zOff = -0.15 * this.getUp().getFrontOffsetZ();
        out.add(new AxisAlignedBB(xOff + 0.15, yOff + 0.15, zOff + 0.15, // ahh
                xOff + 0.85, yOff + 0.85, zOff + 0.85));
    }

    public float getVisibleRotation() {
        return this.visibleRotation;
    }

    private void setVisibleRotation(final float visibleRotation) {
        this.visibleRotation = visibleRotation;
    }
}
