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

package appeng.block.qnb;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;

import appeng.tile.qnb.QuantumBridgeTileEntity;

public class QuantumRingBlock extends QuantumBaseBlock {

    private static final VoxelShape SHAPE = createShape(2.0 / 16.0);
    private static final VoxelShape SHAPE_CORNER = createShape(4.0 / 16.0);
    private static final VoxelShape SHAPE_FORMED = createShape(1.0 / 16.0);

    public QuantumRingBlock() {
        super(defaultProps(Material.METAL));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter w, BlockPos pos, CollisionContext context) {
        final QuantumBridgeTileEntity bridge = this.getTileEntity(w, pos);
        if (bridge != null && bridge.isCorner()) {
            return SHAPE_CORNER;
        } else if (bridge != null && bridge.isFormed()) {
            return SHAPE_FORMED;
        }
        return SHAPE;
    }

    private static net.minecraft.world.phys.shapes.VoxelShape createShape(double onePixel) {
        return Shapes.create(
                new AABB(onePixel, onePixel, onePixel, 1.0 - onePixel, 1.0 - onePixel, 1.0 - onePixel));
    }
}
