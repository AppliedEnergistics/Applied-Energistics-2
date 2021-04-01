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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import appeng.bootstrap.BlockRenderingCustomizer;
import appeng.bootstrap.IBlockRendering;
import appeng.bootstrap.IItemRendering;

/**
 * Customizes the rendering behavior for cable busses, which are the biggest multipart of AE2.
 */
public class CableBusRendering extends BlockRenderingCustomizer {

    @Override
    @Environment(EnvType.CLIENT)
    public void customize(IBlockRendering rendering, IItemRendering itemRendering) {
        // FIXME This is straight up impossible in Vanilla, and questionable if it's
        // actually needed.
        // FIXME rendering.renderType(rt -> true);
        rendering.renderType(RenderType.getCutout());

        rendering.blockColor(new CableBusColor());
        rendering.modelCustomizer((loc, model) -> model);
    }
}
