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

package appeng.block.networking;

import javax.annotation.Nullable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.color.block.BlockColorProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

import appeng.api.util.AEColor;
import appeng.parts.CableBusContainer;
import appeng.tile.networking.CableBusBlockEntity;

/**
 * Exposes the cable bus color as tint indices 0 (dark variant), 1 (medium
 * variant) and 2 (bright variant).
 */
@Environment(EnvType.CLIENT)
public class CableBusColor implements BlockColorProvider {

    @Override
    public int getColor(BlockState state, @Nullable BlockRenderView worldIn, @Nullable BlockPos pos, int color) {

        AEColor busColor = AEColor.TRANSPARENT;

        if (worldIn != null && pos != null) {
            BlockEntity tileEntity = worldIn.getBlockEntity(pos);
            if (tileEntity instanceof CableBusBlockEntity) {
                CableBusContainer container = ((CableBusBlockEntity) tileEntity).getCableBus();
                busColor = container.getColor();
            }
        }

        return busColor.getVariantByTintIndex(color);

    }
}
