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

package appeng.tile.misc;


import appeng.api.util.AEColor;
import appeng.helpers.Splotch;
import appeng.items.misc.ItemPaintBall;
import appeng.tile.AEBaseTile;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumSkyBlock;

import java.io.IOException;
import java.util.*;


public class TilePaint extends AEBaseTile {

    private static final int LIGHT_PER_DOT = 12;

    private int isLit = 0;
    private List<Splotch> dots = null;

    @Override
    public boolean canBeRotated() {
        return false;
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound data) {
        super.writeToNBT(data);
        final ByteBuf myDat = Unpooled.buffer();
        this.writeBuffer(myDat);
        if (myDat.hasArray()) {
            data.setByteArray("dots", myDat.array());
        }
        return data;
    }

    private void writeBuffer(final ByteBuf out) {
        if (this.dots == null) {
            out.writeByte(0);
            return;
        }

        out.writeByte(this.dots.size());

        for (final Splotch s : this.dots) {
            s.writeToStream(out);
        }
    }

    @Override
    public void readFromNBT(final NBTTagCompound data) {
        super.readFromNBT(data);
        if (data.hasKey("dots")) {
            this.readBuffer(Unpooled.copiedBuffer(data.getByteArray("dots")));
        }
    }

    private void readBuffer(final ByteBuf in) {
        final byte howMany = in.readByte();

        if (howMany == 0) {
            this.isLit = 0;
            this.dots = null;
            return;
        }

        this.dots = new ArrayList(howMany);
        for (int x = 0; x < howMany; x++) {
            this.dots.add(new Splotch(in));
        }

        this.isLit = 0;
        for (final Splotch s : this.dots) {
            if (s.isLumen()) {
                this.isLit += LIGHT_PER_DOT;
            }
        }

        this.maxLit();
    }

    private void maxLit() {
        if (this.isLit > 14) {
            this.isLit = 14;
        }

        if (this.world != null) {
            this.world.getLightFor(EnumSkyBlock.BLOCK, this.pos);
        }
    }

    @Override
    protected void writeToStream(final ByteBuf data) throws IOException {
        super.writeToStream(data);
        this.writeBuffer(data);
    }

    @Override
    protected boolean readFromStream(final ByteBuf data) throws IOException {
        super.readFromStream(data);
        this.readBuffer(data);
        return true;
    }

    public void neighborChanged() {
        if (this.dots == null) {
            return;
        }

        for (final EnumFacing side : EnumFacing.VALUES) {
            if (!this.isSideValid(side)) {
                this.removeSide(side);
            }
        }

        this.updateData();
    }

    public boolean isSideValid(final EnumFacing side) {
        final BlockPos p = this.pos.offset(side);
        final IBlockState blk = this.world.getBlockState(p);
        return blk.getBlock().isSideSolid(this.world.getBlockState(p), this.world, p, side.getOpposite());
    }

    private void removeSide(final EnumFacing side) {
        final Iterator<Splotch> i = this.dots.iterator();
        while (i.hasNext()) {
            final Splotch s = i.next();
            if (s.getSide() == side) {
                i.remove();
            }
        }

        this.markForUpdate();
        this.saveChanges();
    }

    private void updateData() {
        this.isLit = 0;
        for (final Splotch s : this.dots) {
            if (s.isLumen()) {
                this.isLit += LIGHT_PER_DOT;
            }
        }

        this.maxLit();

        if (this.dots.isEmpty()) {
            this.dots = null;
        }

        if (this.dots == null) {
            this.world.setBlockToAir(this.pos);
        }
    }

    public void cleanSide(final EnumFacing side) {
        if (this.dots == null) {
            return;
        }

        this.removeSide(side);

        this.updateData();
    }

    public int getLightLevel() {
        return this.isLit;
    }

    public void addBlot(final ItemStack type, final EnumFacing side, final Vec3d hitVec) {
        final BlockPos p = this.pos.offset(side);

        final IBlockState blk = this.world.getBlockState(p);
        if (blk.getBlock().isSideSolid(this.world.getBlockState(p), this.world, p, side.getOpposite())) {
            final ItemPaintBall ipb = (ItemPaintBall) type.getItem();

            final AEColor col = ipb.getColor(type);
            final boolean lit = ItemPaintBall.isLumen(type);

            if (this.dots == null) {
                this.dots = new ArrayList<>();
            }

            if (this.dots.size() > 20) {
                this.dots.remove(0);
            }

            this.dots.add(new Splotch(col, lit, side, hitVec));
            if (lit) {
                this.isLit += LIGHT_PER_DOT;
            }

            this.maxLit();
            this.markForUpdate();
            this.saveChanges();
        }
    }

    public Collection<Splotch> getDots() {
        if (this.dots == null) {
            return Collections.emptyList();
        }

        return this.dots;
    }
}
