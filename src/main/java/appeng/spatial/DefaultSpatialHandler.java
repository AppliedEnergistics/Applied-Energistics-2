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

package appeng.spatial;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import appeng.api.movable.IMovableHandler;

public class DefaultSpatialHandler implements IMovableHandler {

    /**
     * never called for the default.
     *
     * @param tile tile entity
     * @return true
     */
    @Override
    public boolean canHandle(final Class<? extends TileEntity> myClass, final TileEntity tile) {
        return true;
    }

    @Override
    public void moveTile(final TileEntity te, final World w, final BlockPos newPosition) {
        te.setWorldAndPos(w, newPosition);

        final Chunk c = w.getChunkAt(newPosition);
        c.addTileEntity(newPosition, te);

        if (w.getChunkProvider().canTick(newPosition)) {
            final BlockState state = w.getBlockState(newPosition);
            w.addTileEntity(te);
            w.notifyBlockUpdate(newPosition, state, state, 1);
            te.updateContainingBlockInfo();
        }
    }
}
