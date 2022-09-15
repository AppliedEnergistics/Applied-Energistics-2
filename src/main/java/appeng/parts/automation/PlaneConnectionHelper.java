/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.parts.automation;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.parts.AEBasePart;

/**
 * Helps plane parts (annihilation, formation) with determining and checking for connections to adjacent plane parts of
 * the same type to form a visually larger plane.
 */
public final class PlaneConnectionHelper {

    private final AEBasePart part;

    public PlaneConnectionHelper(AEBasePart part) {
        this.part = part;
    }

    /**
     * Gets on which sides this part has adjacent planes that it visually connects to
     */
    public PlaneConnections getConnections() {
        BlockEntity hostBlockEntity = getHostBlockEntity();
        Direction side = part.getSide();

        final Direction facingRight, facingUp;
        switch (side) {
            case UP:
                facingRight = Direction.EAST;
                facingUp = Direction.NORTH;
                break;
            case DOWN:
                facingRight = Direction.WEST;
                facingUp = Direction.NORTH;
                break;
            case NORTH:
                facingRight = Direction.WEST;
                facingUp = Direction.UP;
                break;
            case SOUTH:
                facingRight = Direction.EAST;
                facingUp = Direction.UP;
                break;
            case WEST:
                facingRight = Direction.SOUTH;
                facingUp = Direction.UP;
                break;
            case EAST:
                facingRight = Direction.NORTH;
                facingUp = Direction.UP;
                break;
            default:
                return PlaneConnections.of(false, false, false, false);
        }

        boolean left = false, right = false, down = false, up = false;

        if (hostBlockEntity != null) {
            Level level = hostBlockEntity.getLevel();
            BlockPos pos = hostBlockEntity.getBlockPos();

            if (isCompatiblePlaneAdjacent(level.getBlockEntity(pos.relative(facingRight.getOpposite())))) {
                left = true;
            }

            if (isCompatiblePlaneAdjacent(level.getBlockEntity(pos.relative(facingRight)))) {
                right = true;
            }

            if (isCompatiblePlaneAdjacent(level.getBlockEntity(pos.relative(facingUp.getOpposite())))) {
                down = true;
            }

            if (isCompatiblePlaneAdjacent(level.getBlockEntity(pos.relative(facingUp)))) {
                up = true;
            }
        }

        return PlaneConnections.of(up, right, down, left);
    }

    /**
     * Get the bounding boxes of this plane parts components.
     */
    public void getBoxes(IPartCollisionHelper bch) {
        int minX = 1;
        int minY = 1;
        int maxX = 15;
        int maxY = 15;

        BlockEntity hostEntity = getHostBlockEntity();
        if (hostEntity != null) {
            Level level = hostEntity.getLevel();

            final BlockPos pos = hostEntity.getBlockPos();

            final Direction e = bch.getWorldX();
            final Direction u = bch.getWorldY();

            if (isCompatiblePlaneAdjacent(level.getBlockEntity(pos.relative(e.getOpposite())))) {
                minX = 0;
            }

            if (isCompatiblePlaneAdjacent(level.getBlockEntity(pos.relative(e)))) {
                maxX = 16;
            }

            if (isCompatiblePlaneAdjacent(level.getBlockEntity(pos.relative(u.getOpposite())))) {
                minY = 0;
            }

            if (isCompatiblePlaneAdjacent(level.getBlockEntity(pos.relative(u)))) {
                maxY = 16;
            }
        }

        bch.addBox(5, 5, 14, 11, 11, 15);
        bch.addBox(minX, minY, 15, maxX, maxY, 16);
    }

    /**
     * Call this when an adjacent block has changed since the connections need to be recalculated.
     */
    public void updateConnections() {
        BlockEntity host = getHostBlockEntity();
        if (host != null) {
            host.requestModelDataUpdate();
        }
    }

    private boolean isCompatiblePlaneAdjacent(@Nullable BlockEntity adjacentBlockEntity) {
        if (adjacentBlockEntity instanceof IPartHost) {
            final IPart p = ((IPartHost) adjacentBlockEntity).getPart(part.getSide());
            return p != null && p.getClass() == part.getClass();
        }
        return false;
    }

    private BlockEntity getHostBlockEntity() {
        IPartHost host = part.getHost();
        if (host != null) {
            return host.getBlockEntity();
        }
        return null;
    }

}
