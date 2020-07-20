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


import appeng.client.render.cablebus.CubeBuilder;

/**
 * A simple crafting unit model that uses an un-lit texture for the inner block.
 */
class UnitBakedModel extends CraftingCubeBakedModel {

    private final Sprite unitTexture;

    UnitBakedModel(Sprite ringCorner, Sprite ringHor, Sprite ringVer,
                   Sprite unitTexture) {
        super(ringCorner, ringHor, ringVer);
        this.unitTexture = unitTexture;
    }

    @Override
    protected void addInnerCube(Direction facing, BlockState state, CraftingCubeModelData modelData, CubeBuilder builder, float x1,
                                float y1, float z1, float x2, float y2, float z2) {
        builder.setTexture(this.unitTexture);
        builder.addCube(x1, y1, z1, x2, y2, z2);
    }
}
