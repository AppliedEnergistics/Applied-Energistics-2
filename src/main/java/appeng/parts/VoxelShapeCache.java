/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.parts;

import java.util.List;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * While creation of a {@link net.minecraft.util.math.shapes.VoxelShape} with
 * {@link net.minecraft.util.math.shapes.VoxelShapes#create(AxisAlignedBB)} is fast enough, combining voxel shapes with
 * {@link net.minecraft.util.math.shapes.VoxelShapes#or(VoxelShape, VoxelShape)} or any other combination method, as well
 * as {@link VoxelShape#simplify()} are <b>extremely slow</b>. For example: Creating a VoxelShape for a list of 5
 * bounding boxes 10,000 times takes about 1.7 seconds.
 *
 * <p>
 * To reduce the impact of this on cables, we introduce a global voxel shape cache so that cables can share their
 * combined voxel shapes better.
 */
final class VoxelShapeCache {

    // Why using a List here should not make much of a difference vs. using a Set:
    // The part's bounding box depends on the side it is attached to, and the sides
    // are iterated over in a fixed order, meaning the order of bounding boxes
    // should
    // be the same for a same set of parts.
    private static final LoadingCache<List<AxisAlignedBB>, VoxelShape> CACHE = CacheBuilder.newBuilder()//
            .maximumSize(10000L)//
            .build(new CacheLoader<List<AxisAlignedBB>, VoxelShape>() {
                @Override
                public VoxelShape load(List<AxisAlignedBB> key) {
                    return create(key);
                }
            });

    private VoxelShapeCache() {
    }

    public static VoxelShape get(List<AxisAlignedBB> boxes) {
        return CACHE.getUnchecked(boxes);
    }

    private static VoxelShape create(List<AxisAlignedBB> boxes) {
        if (boxes.isEmpty()) {
            return VoxelShapes.empty();
        }

        int i = 0;
        VoxelShape shape = VoxelShapes.create(boxes.get(i));
        for (; i < boxes.size(); i++) {
            AxisAlignedBB box = boxes.get(i);
            shape = VoxelShapes.combine(shape, VoxelShapes.create(box), IBooleanFunction.OR);
        }
        return shape.simplify();
    }

}
