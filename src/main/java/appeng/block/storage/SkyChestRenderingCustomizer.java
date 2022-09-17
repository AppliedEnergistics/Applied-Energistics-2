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

package appeng.block.storage;


import appeng.bootstrap.BlockRenderingCustomizer;
import appeng.bootstrap.IBlockRendering;
import appeng.bootstrap.IItemRendering;
import appeng.client.render.tesr.SkyChestTESR;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class SkyChestRenderingCustomizer extends BlockRenderingCustomizer {

    private final BlockSkyChest.SkyChestType type;

    public SkyChestRenderingCustomizer(BlockSkyChest.SkyChestType type) {
        this.type = type;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void customize(IBlockRendering rendering, IItemRendering itemRendering) {
        rendering.tesr(new SkyChestTESR());

        // Register a custom non-tesr item model
        String modelName = this.getModelFromType();
        ModelResourceLocation model = new ModelResourceLocation("appliedenergistics2:" + modelName, "inventory");
        itemRendering.model(model).variants(model);
    }

    private String getModelFromType() {
        final String modelName;
        switch (this.type) {
            default:
            case STONE:
                modelName = "sky_stone_chest";
                break;
            case BLOCK:
                modelName = "smooth_sky_stone_chest";
                break;
        }
        return modelName;
    }
}
