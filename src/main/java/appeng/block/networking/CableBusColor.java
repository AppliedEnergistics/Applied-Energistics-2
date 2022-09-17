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


import appeng.api.util.AEColor;
import appeng.client.render.cablebus.CableBusRenderState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


/**
 * Exposes the cable bus color as tint indices 0 (dark variant), 1 (medium variant) and 2 (bright variant).
 */
@SideOnly(Side.CLIENT)
public class CableBusColor implements IBlockColor {

    @Override
    public int colorMultiplier(IBlockState state, IBlockAccess worldIn, BlockPos pos, int color) {

        AEColor busColor = AEColor.TRANSPARENT;

        if (state instanceof IExtendedBlockState) {
            CableBusRenderState renderState = ((IExtendedBlockState) state).getValue(BlockCableBus.RENDER_STATE_PROPERTY);
            if (renderState != null) {
                busColor = renderState.getCableColor();
            }
        }

        return busColor.getVariantByTintIndex(color);

    }
}
