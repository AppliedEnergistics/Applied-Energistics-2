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

package appeng.blockentity.misc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.netty.buffer.Unpooled;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import appeng.api.util.AEColor;
import appeng.block.paint.PaintSplotches;
import appeng.block.paint.PaintSplotchesBlock;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.helpers.Splotch;
import appeng.items.misc.PaintBallItem;

public class PaintSplotchesBlockEntity extends AEBaseBlockEntity {

    private List<Splotch> dots = null;

    public PaintSplotchesBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    @Override
    public boolean canBeRotated() {
        return false;
    }

    @Override
    public CompoundTag save(final CompoundTag data) {
        super.save(data);
        final FriendlyByteBuf myDat = new FriendlyByteBuf(Unpooled.buffer());
        this.writeBuffer(myDat);
        if (myDat.hasArray()) {
            data.putByteArray("dots", myDat.array());
        }
        return data;
    }

    private void writeBuffer(final FriendlyByteBuf out) {
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
    public void load(final CompoundTag data) {
        super.load(data);
        if (data.contains("dots")) {
            this.readBuffer(new FriendlyByteBuf(Unpooled.copiedBuffer(data.getByteArray("dots"))));
        }
    }

    private void readBuffer(final FriendlyByteBuf in) {
        final byte howMany = in.readByte();

        if (howMany == 0) {
            this.dots = null;
            return;
        }

        this.dots = new ArrayList<>(howMany);
        for (int x = 0; x < howMany; x++) {
            this.dots.add(new Splotch(in));
        }
    }

    @Override
    protected void writeToStream(final FriendlyByteBuf data) throws IOException {
        super.writeToStream(data);
        this.writeBuffer(data);
    }

    @Override
    protected boolean readFromStream(final FriendlyByteBuf data) throws IOException {
        super.readFromStream(data);
        this.readBuffer(data);
        return true;
    }

    public void neighborChanged() {
        if (this.dots == null) {
            return;
        }

        for (final Direction side : Direction.values()) {
            if (!this.isSideValid(side)) {
                this.removeSide(side);
            }
        }

        this.updateData();
    }

    public boolean isSideValid(final Direction side) {
        final BlockPos p = this.worldPosition.relative(side);
        final BlockState blk = this.level.getBlockState(p);
        return blk.isFaceSturdy(level, p, side.getOpposite());
    }

    private void removeSide(final Direction side) {
        this.dots.removeIf(s -> s.getSide() == side);

        this.markForUpdate();
        this.saveChanges();
    }

    private void updateData() {
        if (this.dots.isEmpty()) {
            this.dots = null;
        }

        if (this.dots == null) {
            this.level.removeBlock(this.worldPosition, false);
        } else {
            var lumenCount = 0;
            for (Splotch dot : dots) {
                if (dot.isLumen()) {
                    lumenCount++;
                    if (lumenCount >= 2) {
                        break;
                    }
                }
            }
            this.level.setBlockAndUpdate(getBlockPos(),
                    getBlockState().setValue(PaintSplotchesBlock.LIGHT_LEVEL, lumenCount));
        }
    }

    public void cleanSide(final Direction side) {
        if (this.dots == null) {
            return;
        }

        this.removeSide(side);
        this.updateData();
    }

    public void addBlot(final ItemStack type, final Direction side, final Vec3 hitVec) {
        final BlockPos p = this.worldPosition.relative(side);

        final BlockState blk = this.level.getBlockState(p);
        if (blk.isFaceSturdy(this.level, p, side.getOpposite())) {
            final PaintBallItem ipb = (PaintBallItem) type.getItem();

            final AEColor col = ipb.getColor();
            final boolean lit = ipb.isLumen();

            if (this.dots == null) {
                this.dots = new ArrayList<>();
            }

            if (this.dots.size() > 20) {
                this.dots.remove(0);
            }

            this.dots.add(new Splotch(col, lit, side, hitVec));

            updateData();
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

    @Override
    public Object getRenderAttachmentData() {
        return new PaintSplotches(getDots());
    }

}
