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
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import appeng.api.movable.IMovableHandler;

public class DefaultSpatialHandler implements IMovableHandler {

    /**
     * never called for the default.
     *
     * @param tile block entity
     * @return true
     */
    @Override
    public boolean canHandle(final Class<? extends BlockEntity> myClass, final BlockEntity tile) {
        return true;
    }

    @Override
    public void moveTile(final BlockEntity te, final World w, final BlockPos newPosition) {
        te.setLocation(w, newPosition);

        final Chunk c = w.getChunk(newPosition);
        c.setBlockEntity(newPosition, te);

        if (w.getChunkManager().shouldTickBlock(newPosition)) {
            final BlockState state = w.getBlockState(newPosition);
            w.addBlockEntity(te);
            w.updateListeners(newPosition, state, state, 1);
        }
    }
}
