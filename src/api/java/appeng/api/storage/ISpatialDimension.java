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

package appeng.api.storage;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public interface ISpatialDimension {
    ServerWorld getWorld(RegistryKey<World> cellDim);

    @Nullable
    RegistryKey<World> createNewCellDimension(BlockPos capacity);

    void deleteCellDimension(RegistryKey<World> cellDim);

    boolean isCellDimension(RegistryKey<World> cellDim);

    BlockPos getCellDimensionOrigin(RegistryKey<World> cellDim);

    BlockPos getCellDimensionSize(RegistryKey<World> cellDim);

    /**
     * Adds a user-facing tooltip that describes this dimension so that a player can
     * keep storage cells apart.
     */
    void addCellDimensionTooltip(RegistryKey<World> cellDim, List<ITextComponent> lines);

}
