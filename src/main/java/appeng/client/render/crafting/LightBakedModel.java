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

package appeng.client.render.crafting;

import appeng.tile.crafting.CraftingCubeModelData;
import net.minecraft.block.BlockState;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;


import appeng.block.crafting.AbstractCraftingUnitBlock;
import appeng.client.render.cablebus.CubeBuilder;

/**
 * Crafting cube baked model that adds a full-bright light texture on top of a
 * normal base texture onto the inner cube. The light texture is only drawn
 * fullbright if the multiblock is currently powered.
 */
class LightBakedModel extends CraftingCubeBakedModel {

    private final Sprite baseTexture;

    private final Sprite lightTexture;

    LightBakedModel(Sprite ringCorner, Sprite ringHor, Sprite ringVer,
                    Sprite baseTexture, Sprite lightTexture) {
        super(ringCorner, ringHor, ringVer);
        this.baseTexture = baseTexture;
        this.lightTexture = lightTexture;
    }

    @Override
    protected void addInnerCube(Direction facing, BlockState state, CraftingCubeModelData modelData, CubeBuilder builder, float x1,
                                float y1, float z1, float x2, float y2, float z2) {
        builder.setTexture(this.baseTexture);
        builder.addCube(x1, y1, z1, x2, y2, z2);

        boolean powered = state.get(AbstractCraftingUnitBlock.POWERED);
        builder.setRenderFullBright(powered);
        builder.setTexture(this.lightTexture);
        builder.addCube(x1, y1, z1, x2, y2, z2);
    }
}
