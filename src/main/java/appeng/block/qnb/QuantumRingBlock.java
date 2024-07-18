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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import appeng.blockentity.qnb.QuantumBridgeBlockEntity;

public class QuantumRingBlock extends QuantumBaseBlock {

    private static final VoxelShape SHAPE = createShape(2.0 / 16.0);
    private static final VoxelShape SHAPE_CORNER = createShape(2.0 / 16.0);
    private static final VoxelShape SHAPE_CENTER = createCenterShape(2.0 / 16.0);

    private static final double DEFAULT_MODEL_MIN = 2.0 / 16.0;
    private static final double DEFAULT_MODEL_MAX = 14.0 / 16.0;

    public QuantumRingBlock() {
        super(metalProps());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        final QuantumBridgeBlockEntity bridge = this.getBlockEntity(level, pos);
        if (bridge != null && bridge.isCorner()) {
            return SHAPE_CORNER;
        } else if (bridge != null && bridge.isFormed()) {
            return SHAPE_CENTER;
        }
        return SHAPE;
    }

    private static VoxelShape createShape(double onePixel) {
        return Shapes.create(
                new AABB(onePixel, onePixel, onePixel, 1.0 - onePixel, 1.0 - onePixel, 1.0 - onePixel));
    }

    private static VoxelShape createCenterShape(double offset) {
        VoxelShape centerShape = SHAPE;
        for (Direction facing : Direction.values()) {
            double xOffset = Math.abs(facing.getStepX() * offset);
            double yOffset = Math.abs(facing.getStepY() * offset);
            double zOffset = Math.abs(facing.getStepZ() * offset);

            VoxelShape extrusion = Shapes.create(
                    new AABB(DEFAULT_MODEL_MIN - xOffset, DEFAULT_MODEL_MIN - yOffset,
                            DEFAULT_MODEL_MIN - zOffset, DEFAULT_MODEL_MAX + xOffset,
                            DEFAULT_MODEL_MAX + yOffset, DEFAULT_MODEL_MAX + zOffset));

            centerShape = Shapes.or(centerShape, extrusion);
        }

        return centerShape;
    }
}
