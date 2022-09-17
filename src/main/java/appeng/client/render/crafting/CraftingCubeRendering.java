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


import appeng.block.crafting.BlockCraftingUnit;
import appeng.bootstrap.BlockRenderingCustomizer;
import appeng.bootstrap.IBlockRendering;
import appeng.bootstrap.IItemRendering;
import appeng.core.AppEng;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;


/**
 * Rendering customization for the crafting cube.
 */
public class CraftingCubeRendering extends BlockRenderingCustomizer {

    private final String registryName;

    private final BlockCraftingUnit.CraftingUnitType type;

    public CraftingCubeRendering(String registryName, BlockCraftingUnit.CraftingUnitType type) {
        this.registryName = registryName;
        this.type = type;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void customize(IBlockRendering rendering, IItemRendering itemRendering) {
        ResourceLocation baseName = new ResourceLocation(AppEng.MOD_ID, this.registryName);

        // Disable auto-rotation
        if (this.type != BlockCraftingUnit.CraftingUnitType.MONITOR) {
            rendering.modelCustomizer((loc, model) -> model);
        }

        // This is the standard blockstate model
        ModelResourceLocation defaultModel = new ModelResourceLocation(baseName, "normal");

        // This is the built-in model
        String builtInName = "models/block/crafting/" + this.registryName + "/builtin";
        ModelResourceLocation builtInModelName = new ModelResourceLocation(new ResourceLocation(AppEng.MOD_ID, builtInName), "normal");

        rendering.builtInModel(builtInName, new CraftingCubeModel(this.type));

        rendering.stateMapper(block -> this.mapState(block, defaultModel, builtInModelName));

        if (this.type == BlockCraftingUnit.CraftingUnitType.MONITOR) {
            rendering.tesr(new CraftingMonitorTESR());
        }

    }

    private Map<IBlockState, ModelResourceLocation> mapState(Block block, ModelResourceLocation defaultModel, ModelResourceLocation formedModel) {
        Map<IBlockState, ModelResourceLocation> result = new HashMap<>();
        for (IBlockState state : block.getBlockState().getValidStates()) {
            if (state.getValue(BlockCraftingUnit.FORMED)) {
                // Always use the builtin model if the multiblock is formed
                result.put(state, formedModel);
            } else {
                // Use the default model
                result.put(state, defaultModel);
            }
        }
        return result;
    }
}
