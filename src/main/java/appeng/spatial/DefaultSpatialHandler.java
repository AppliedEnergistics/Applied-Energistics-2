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

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import appeng.api.movable.IMovableHandler;

public class DefaultSpatialHandler implements IMovableHandler {

    @Override
    public boolean canHandle(BlockEntityType<?> type) {
        return true;
    }

    @Override
    public boolean moveBlockEntity(BlockEntity te, CompoundTag savedData, Level level, BlockPos newPosition) {
        var be = BlockEntity.loadStatic(newPosition, te.getBlockState(), savedData);
        if (be != null) {
            level.setBlockEntity(be);
            return true;
        } else {
            return false;
        }
    }

}
