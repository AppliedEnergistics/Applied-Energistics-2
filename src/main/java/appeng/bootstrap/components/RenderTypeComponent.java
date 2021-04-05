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

package appeng.bootstrap.components;

import com.google.common.base.Preconditions;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderType;

/**
 * Sets the rendering type for a block.
 */
public class RenderTypeComponent implements IClientSetupComponent {

    private final Block block;

    private final RenderType renderType;

    public RenderTypeComponent(Block block, RenderType renderType) {
        this.block = block;
        this.renderType = Preconditions.checkNotNull(renderType);
    }

    @Override
    public void setup() {
        BlockRenderLayerMap.INSTANCE.putBlock(block, renderType);
    }

}
