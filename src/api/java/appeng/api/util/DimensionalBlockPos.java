/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 AlgorithmX2
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package appeng.api.util;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Represents a location in the Minecraft Universe
 */
public final class DimensionalBlockPos extends BlockPos {

    @Nonnull
    private final World world;

    public DimensionalBlockPos(DimensionalBlockPos coordinate) {
        this(coordinate.getWorld(), coordinate);
    }

    public DimensionalBlockPos(TileEntity tileEntity) {
        this(tileEntity.getWorld(), tileEntity.getPos());
    }

    public DimensionalBlockPos(World world, BlockPos pos) {
        this(world, pos.getX(), pos.getY(), pos.getZ());
    }

    public DimensionalBlockPos(World world, int x, int y, int z) {
        super(x, y, z);
        this.world = Objects.requireNonNull(world, "world");
    }

    @Override
    public DimensionalBlockPos toImmutable() {
        return this;
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ this.world.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof DimensionalBlockPos dimPos && dimPos.world == world && super.equals(obj);
    }

    @Override
    public String toString() {
        return getX() + "," + getY() + "," + getZ() + " in " + getWorld().getDimensionKey().getLocation();
    }

    public boolean isInWorld(final IWorld world) {
        return this.world == world;
    }

    public World getWorld() {
        return this.world;
    }

}
