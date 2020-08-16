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

import net.minecraft.block.BlockState;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;

import appeng.api.util.AEColor;
import appeng.block.crafting.CraftingMonitorBlock;
import appeng.client.render.cablebus.CubeBuilder;
import appeng.tile.crafting.CraftingCubeModelData;
import appeng.tile.crafting.CraftingMonitorModelData;

/**
 * The baked model for the crafting monitor. Please note that this model doesn't
 * handle the item being displayed. That is handled by a TESR. Instead, this
 * model adds 3 layered light textures using the [dark|medium|bright] color
 * variants of the attached bus color. The textures are full-bright if the cube
 * is powered.
 */
public class MonitorBakedModel extends CraftingCubeBakedModel {

    private final Sprite chassisTexture;

    private final Sprite baseTexture;

    private final Sprite lightDarkTexture;

    private final Sprite lightMediumTexture;

    private final Sprite lightBrightTexture;

    MonitorBakedModel(Sprite ringCorner, Sprite ringHor, Sprite ringVer, Sprite chassisTexture, Sprite baseTexture,
            Sprite lightDarkTexture, Sprite lightMediumTexture, Sprite lightBrightTexture) {
        super(ringCorner, ringHor, ringVer);
        this.chassisTexture = chassisTexture;
        this.baseTexture = baseTexture;
        this.lightDarkTexture = lightDarkTexture;
        this.lightMediumTexture = lightMediumTexture;
        this.lightBrightTexture = lightBrightTexture;
    }

    @Override
    protected void addInnerCube(Direction side, BlockState state, CraftingCubeModelData modelData, CubeBuilder builder,
            float x1, float y1, float z1, float x2, float y2, float z2) {
        Direction forward = modelData.getForward();

        // For sides other than the front, use the chassis texture
        if (side != forward) {
            builder.setTexture(this.chassisTexture);
            builder.addCube(x1, y1, z1, x2, y2, z2);
            return;
        }

        builder.setTexture(this.baseTexture);
        builder.addCube(x1, y1, z1, x2, y2, z2);

        // Now add the three layered light textures
        AEColor color = getColor(modelData);
        boolean powered = state.get(CraftingMonitorBlock.POWERED);

        builder.setEmissiveMaterial(powered);

        builder.setColorRGB(color.whiteVariant);
        builder.setTexture(this.lightBrightTexture);
        builder.addCube(x1, y1, z1, x2, y2, z2);

        builder.setColorRGB(color.mediumVariant);
        builder.setTexture(this.lightMediumTexture);
        builder.addCube(x1, y1, z1, x2, y2, z2);

        builder.setColorRGB(color.blackVariant);
        builder.setTexture(this.lightDarkTexture);
        builder.addCube(x1, y1, z1, x2, y2, z2);

        // Reset back to default
        builder.setEmissiveMaterial(false);
    }

    private static AEColor getColor(CraftingCubeModelData modelData) {
        if (modelData instanceof CraftingMonitorModelData) {
            return ((CraftingMonitorModelData) modelData).getColor();
        }
        return AEColor.TRANSPARENT;
    }

}
