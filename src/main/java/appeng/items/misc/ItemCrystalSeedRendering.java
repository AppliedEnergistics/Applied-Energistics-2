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


import appeng.bootstrap.IItemRendering;
import appeng.bootstrap.ItemRenderingCustomizer;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class ItemCrystalSeedRendering extends ItemRenderingCustomizer {

    private static final ModelResourceLocation[] MODELS_CERTUS = {
            new ModelResourceLocation("appliedenergistics2:crystal_seed_certus"),
            new ModelResourceLocation("appliedenergistics2:crystal_seed_certus2"),
            new ModelResourceLocation("appliedenergistics2:crystal_seed_certus3")
    };
    private static final ModelResourceLocation[] MODELS_FLUIX = {
            new ModelResourceLocation("appliedenergistics2:crystal_seed_fluix"),
            new ModelResourceLocation("appliedenergistics2:crystal_seed_fluix2"),
            new ModelResourceLocation("appliedenergistics2:crystal_seed_fluix3")
    };
    private static final ModelResourceLocation[] MODELS_NETHER = {
            new ModelResourceLocation("appliedenergistics2:crystal_seed_nether"),
            new ModelResourceLocation("appliedenergistics2:crystal_seed_nether2"),
            new ModelResourceLocation("appliedenergistics2:crystal_seed_nether3")
    };

    @Override
    @SideOnly(Side.CLIENT)
    public void customize(IItemRendering rendering) {
        rendering.variants(ImmutableList.<ResourceLocation>builder().add(MODELS_CERTUS).add(MODELS_FLUIX).add(MODELS_NETHER).build());
        rendering.meshDefinition(this.getItemMeshDefinition());
    }

    private ItemMeshDefinition getItemMeshDefinition() {
        return is ->
        {
            int damage = ItemCrystalSeed.getProgress(is);

            // Split the damage value into crystal type and growth level
            int type = damage / ItemCrystalSeed.SINGLE_OFFSET;
            int level = (damage % ItemCrystalSeed.SINGLE_OFFSET) / ItemCrystalSeed.LEVEL_OFFSET;

            // Determine which list of models to use based on the type of crystal
            ModelResourceLocation[] models;
            switch (type) {
                case 0:
                    models = MODELS_CERTUS;
                    break;
                case 1:
                    models = MODELS_NETHER;
                    break;
                case 2:
                    models = MODELS_FLUIX;
                    break;
                default:
                    // We use this as the fallback for broken items
                    models = MODELS_CERTUS;
                    break;
            }

            // Return one of the 3 models based on the level
            if (level < 0) {
                level = 0;
            } else if (level >= models.length) {
                level = models.length - 1;
            }

            return models[level];
        };
    }
}
