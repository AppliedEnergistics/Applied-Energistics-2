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

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.util.AEPartLocation;
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
        BlockEntity hostTileEntity = getHostTileEntity();
        AEPartLocation side = part.getSide();

        final net.minecraft.core.Direction facingRight, facingUp;
        switch (side) {
            case UP:
                facingRight = net.minecraft.core.Direction.EAST;
                facingUp = net.minecraft.core.Direction.NORTH;
                break;
            case DOWN:
                facingRight = net.minecraft.core.Direction.WEST;
                facingUp = net.minecraft.core.Direction.NORTH;
                break;
            case NORTH:
                facingRight = net.minecraft.core.Direction.WEST;
                facingUp = net.minecraft.core.Direction.UP;
                break;
            case SOUTH:
                facingRight = net.minecraft.core.Direction.EAST;
                facingUp = net.minecraft.core.Direction.UP;
                break;
            case WEST:
                facingRight = Direction.SOUTH;
                facingUp = net.minecraft.core.Direction.UP;
                break;
            case EAST:
                facingRight = net.minecraft.core.Direction.NORTH;
                facingUp = net.minecraft.core.Direction.UP;
                break;
            default:
            case INTERNAL:
                return PlaneConnections.of(false, false, false, false);
        }

        boolean left = false, right = false, down = false, up = false;

        if (hostTileEntity != null) {
            Level world = hostTileEntity.getLevel();
            BlockPos pos = hostTileEntity.getBlockPos();

            if (isCompatiblePlaneAdjacent(world.getBlockEntity(pos.relative(facingRight.getOpposite())))) {
                left = true;
            }

            if (isCompatiblePlaneAdjacent(world.getBlockEntity(pos.relative(facingRight)))) {
                right = true;
            }

            if (isCompatiblePlaneAdjacent(world.getBlockEntity(pos.relative(facingUp.getOpposite())))) {
                down = true;
            }

            if (isCompatiblePlaneAdjacent(world.getBlockEntity(pos.relative(facingUp)))) {
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

        BlockEntity hostTile = getHostTileEntity();
        if (hostTile != null) {
            Level world = hostTile.getLevel();

            final net.minecraft.core.BlockPos pos = hostTile.getBlockPos();

            final net.minecraft.core.Direction e = bch.getWorldX();
            final Direction u = bch.getWorldY();

            if (isCompatiblePlaneAdjacent(world.getBlockEntity(pos.relative(e.getOpposite())))) {
                minX = 0;
            }

            if (isCompatiblePlaneAdjacent(world.getBlockEntity(pos.relative(e)))) {
                maxX = 16;
            }

            if (isCompatiblePlaneAdjacent(world.getBlockEntity(pos.relative(u.getOpposite())))) {
                minY = 0;
            }

            if (isCompatiblePlaneAdjacent(world.getBlockEntity(pos.relative(u)))) {
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
        BlockEntity hostTile = getHostTileEntity();
        if (hostTile != null) {
            hostTile.requestModelDataUpdate();
        }
    }

    private boolean isCompatiblePlaneAdjacent(@Nullable BlockEntity adjacentTileEntity) {
        if (adjacentTileEntity instanceof IPartHost) {
            final IPart p = ((IPartHost) adjacentTileEntity).getPart(part.getSide());
            return p != null && p.getClass() == part.getClass();
        }
        return false;
    }

    private BlockEntity getHostTileEntity() {
        IPartHost host = part.getHost();
        if (host != null) {
            return host.getTile();
        }
        return null;
    }

}
