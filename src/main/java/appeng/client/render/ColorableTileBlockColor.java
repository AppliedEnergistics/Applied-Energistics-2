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

package appeng.client.render;

import javax.annotation.Nullable;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;

import appeng.api.implementations.tiles.IColorableTile;
import appeng.api.util.AEColor;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Automatically exposes the color of a colorable tile using tint indices 0-2
 */
public class ColorableTileBlockColor implements BlockColor {

    public static final ColorableTileBlockColor INSTANCE = new ColorableTileBlockColor();

    @Override
    public int getColor(BlockState state, @Nullable BlockAndTintGetter worldIn, @Nullable net.minecraft.core.BlockPos pos,
                        int tintIndex) {
        AEColor color = AEColor.TRANSPARENT; // Default to a neutral color

        if (worldIn != null && pos != null) {
            BlockEntity te = worldIn.getBlockEntity(pos);
            if (te instanceof IColorableTile) {
                color = ((IColorableTile) te).getColor();
            }
        }

        return color.getVariantByTintIndex(tintIndex);
    }
}
