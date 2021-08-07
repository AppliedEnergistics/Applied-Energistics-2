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
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;

import io.netty.buffer.Unpooled;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;

import appeng.api.util.AEColor;
import appeng.block.paint.PaintSplotches;
import appeng.helpers.Splotch;
import appeng.items.misc.PaintBallItem;
import appeng.blockentity.AEBaseBlockEntity;

public class PaintSplotchesBlockEntity extends AEBaseBlockEntity {

    public static final ModelProperty<PaintSplotches> SPLOTCHES = new ModelProperty<>();

    private static final int LIGHT_PER_DOT = 12;

    private int isLit = 0;
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

        if (this.level != null) {
            this.level.getBrightness(LightLayer.BLOCK, this.worldPosition);
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
            this.level.removeBlock(this.worldPosition, false);
        }
    }

    public void cleanSide(final Direction side) {
        if (this.dots == null) {
            return;
        }

        this.removeSide(side);

        this.updateData();
    }

    public int getLightLevel() {
        return this.isLit;
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

    @Nonnull
    @Override
    public IModelData getModelData() {
        // FIXME update trigger
        return new ModelDataMap.Builder().withInitial(SPLOTCHES, new PaintSplotches(getDots())).build();
    }
}
