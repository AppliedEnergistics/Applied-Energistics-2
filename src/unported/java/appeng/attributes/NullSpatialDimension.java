/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2017, AlgorithmX2, All rights reserved.
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

package appeng.attributes;

import java.util.List;

import net.minecraft.util.math.BlockPos;
import net.minecraft.text.Text;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.server.world.ServerWorld;

import appeng.api.storage.ISpatialDimension;

class NullSpatialDimension implements ISpatialDimension {
    @Override
    public DimensionType createNewCellDimension(BlockPos size) {
        return null;
    }

    @Override
    public void deleteCellDimension(DimensionType cellStorageId) {
    }

    @Override
    public BlockPos getCellDimensionOrigin(DimensionType cellStorageId) {
        return null;
    }

    @Override
    public BlockPos getCellDimensionSize(DimensionType cellDim) {
        return BlockPos.ORIGIN;
    }

    @Override
    public void addCellDimensionTooltip(DimensionType cellDim, List<Text> tooltip) {
    }

    @Override
    public ServerWorld getWorld(DimensionType cellStorageId) {
        return null;
    }

    @Override
    public boolean isCellDimension(DimensionType cellDimID) {
        return false;
    }
}
