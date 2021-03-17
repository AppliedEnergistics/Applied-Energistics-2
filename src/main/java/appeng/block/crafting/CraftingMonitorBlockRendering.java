/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.block.crafting;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.bootstrap.BlockRenderingCustomizer;
import appeng.bootstrap.IBlockRendering;
import appeng.bootstrap.IItemRendering;
import appeng.client.render.crafting.MonitorBakedModel;
import appeng.client.render.model.AutoRotatingBakedModel;

public class CraftingMonitorBlockRendering extends BlockRenderingCustomizer {

    @Override
    @OnlyIn(Dist.CLIENT)
    public void customize(IBlockRendering rendering, IItemRendering itemRendering) {
        rendering.renderType(RenderType.getCutout());
        rendering.modelCustomizer(CraftingMonitorBlockRendering::customizeModel);
    }

    @OnlyIn(Dist.CLIENT)
    private static IBakedModel customizeModel(ResourceLocation path, IBakedModel model) {
        // The formed model handles rotations itself, the unformed one does not
        if (model instanceof MonitorBakedModel) {
            return model;
        }
        return new AutoRotatingBakedModel(model);
    }

}
