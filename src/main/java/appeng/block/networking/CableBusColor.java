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

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.ILightReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.util.AEColor;
import appeng.client.render.cablebus.CableBusRenderState;
import appeng.parts.CableBusContainer;
import appeng.tile.networking.TileCableBus;

/**
 * Exposes the cable bus color as tint indices 0 (dark variant), 1 (medium
 * variant) and 2 (bright variant).
 */
@OnlyIn(Dist.CLIENT)
public class CableBusColor implements IBlockColor {

    @Override
    public int getColor(BlockState state, @Nullable ILightReader worldIn, @Nullable BlockPos pos, int color) {

        // FIXME: Once COLOR becomes part of the block state, change this
        AEColor busColor = AEColor.TRANSPARENT;

        if (worldIn != null && pos != null) {
            TileEntity tileEntity = worldIn.getTileEntity(pos);
            if (tileEntity instanceof TileCableBus) {
                CableBusContainer container = ((TileCableBus) tileEntity).getCableBus();
                busColor = container.getColor();
            }
        }

        return busColor.getVariantByTintIndex(color);

    }
}
