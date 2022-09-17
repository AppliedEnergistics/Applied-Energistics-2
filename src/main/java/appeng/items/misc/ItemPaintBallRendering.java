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

package appeng.items.misc;


import appeng.api.util.AEColor;
import appeng.bootstrap.IItemRendering;
import appeng.bootstrap.ItemRenderingCustomizer;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;


public class ItemPaintBallRendering extends ItemRenderingCustomizer {

    private static final ModelResourceLocation MODEL_NORMAL = new ModelResourceLocation("appliedenergistics2:paint_ball");
    private static final ModelResourceLocation MODEL_SHIMMER = new ModelResourceLocation("appliedenergistics2:paint_ball_shimmer");

    @Override
    public void customize(IItemRendering rendering) {
        rendering.color(ItemPaintBallRendering::getColorFromItemstack);
        rendering.variants(MODEL_NORMAL, MODEL_SHIMMER);
        rendering.meshDefinition(is -> ItemPaintBall.isLumen(is) ? MODEL_SHIMMER : MODEL_NORMAL);
    }

    private static int getColorFromItemstack(ItemStack stack, int tintIndex) {
        final AEColor col = ((ItemPaintBall) stack.getItem()).getColor(stack);

        final int colorValue = col.mediumVariant;
        final int r = (colorValue >> 16) & 0xff;
        final int g = (colorValue >> 8) & 0xff;
        final int b = (colorValue) & 0xff;

        if (stack.getItemDamage() >= 20) {
            final float fail = 0.7f;
            final int full = (int) (255 * 0.3);
            return (int) (full + r * fail) << 16 | (int) (full + g * fail) << 8 | (int) (full + b * fail) | 0xff << 24;
        } else {
            return r << 16 | g << 8 | b | 0xff << 24;
        }
    }
}
