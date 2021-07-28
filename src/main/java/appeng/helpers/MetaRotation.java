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

package appeng.helpers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import appeng.api.util.IOrientable;
import appeng.decorative.solid.QuartzPillarBlock;

public class MetaRotation implements IOrientable {

    private final Property<Direction> facingProp;
    private final BlockGetter w;
    private final BlockPos pos;

    public MetaRotation(final BlockGetter world, final BlockPos pos, final Property<Direction> facingProp) {
        this.w = world;
        this.pos = pos;
        this.facingProp = facingProp;
    }

    @Override
    public boolean canBeRotated() {
        return true;
    }

    @Override
    public Direction getForward() {
        if (this.getUp().getStepY() == 0) {
            return Direction.UP;
        }
        return Direction.SOUTH;
    }

    @Override
    public Direction getUp() {
        final BlockState state = this.w.getBlockState(this.pos);

        if (this.facingProp != null && state.hasProperty(this.facingProp)) {
            return state.getValue(this.facingProp);
        }

        // TODO 1.10.2-R - Temp
        if (state.hasProperty(QuartzPillarBlock.AXIS)) {
            Axis a = state.getValue(QuartzPillarBlock.AXIS);
            switch (a) {
                case X:
                    return Direction.EAST;
                case Z:
                    return Direction.SOUTH;
                default:
                case Y:
                    return Direction.UP;
            }
        }

        return Direction.UP;
    }

    @Override
    public void setOrientation(final Direction forward, final Direction up) {
        if (this.w instanceof Level) {
            if (this.facingProp != null) {
                ((Level) this.w).setBlockAndUpdate(this.pos,
                        this.w.getBlockState(this.pos).setValue(this.facingProp, up));
            } else {
                // TODO 1.10.2-R - Temp
                ((Level) this.w).setBlockAndUpdate(this.pos,
                        this.w.getBlockState(this.pos).setValue(QuartzPillarBlock.AXIS, up.getAxis()));
            }
        } else {
            throw new IllegalStateException(this.w.getClass().getName() + " received, expected World");
        }
    }
}
