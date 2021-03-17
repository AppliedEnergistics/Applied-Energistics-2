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

import java.util.function.Predicate;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;

/**
 * Sets the rendering type for a block.
 */
public class RenderTypeComponent implements IClientSetupComponent {

    private final Block block;

    private final RenderType renderType;

    private final Predicate<RenderType> renderTypes;

    public RenderTypeComponent(Block block, RenderType renderType, Predicate<RenderType> renderTypes) {
        this.block = block;
        this.renderType = renderType;
        this.renderTypes = renderTypes;
    }

    @Override
    public void setup() {
        if (renderType != null) {
            RenderTypeLookup.setRenderLayer(block, renderType);
        }

        if (renderTypes != null) {
            RenderTypeLookup.setRenderLayer(block, renderTypes);
        }
    }

}
