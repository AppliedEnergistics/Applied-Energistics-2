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


import appeng.block.crafting.AbstractCraftingUnitBlock;
import appeng.bootstrap.BlockRenderingCustomizer;
import appeng.bootstrap.IBlockRendering;
import appeng.bootstrap.IItemRendering;
import appeng.core.AppEng;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


/**
 * Rendering customization for the crafting cube.
 */
public class CraftingCubeRendering extends BlockRenderingCustomizer
{

	private final String registryName;

	private final AbstractCraftingUnitBlock.CraftingUnitType type;

	public CraftingCubeRendering( String registryName, AbstractCraftingUnitBlock.CraftingUnitType type )
	{
		this.registryName = registryName;
		this.type = type;
	}

	@Override
	@OnlyIn( Dist.CLIENT )
	public void customize( IBlockRendering rendering, IItemRendering itemRendering )
	{
		if (type != AbstractCraftingUnitBlock.CraftingUnitType.MONITOR) {
			rendering.renderType(RenderType.getCutout());
		}

		ResourceLocation baseName = new ResourceLocation( AppEng.MOD_ID, this.registryName );

		// Disable auto-rotation
		if( this.type != AbstractCraftingUnitBlock.CraftingUnitType.MONITOR )
		{
			rendering.modelCustomizer( ( loc, model ) -> model );
		}

// FIXME		// This is the standard blockstate model
// FIXME		ModelResourceLocation defaultModel = new ModelResourceLocation( baseName, "normal" );
// FIXME
// FIXME		// This is the built-in model
// FIXME		String builtInName = "models/block/crafting/" + this.registryName + "/builtin";
// FIXME		ModelResourceLocation builtInModelName = new ModelResourceLocation( new ResourceLocation( AppEng.MOD_ID, builtInName ), "normal" );
// FIXME
// FIXME		rendering.builtInModel( builtInName, new CraftingCubeModel( this.type ) );
// FIXME
// FIXME		rendering.stateMapper( block -> this.mapState( block, defaultModel, builtInModelName ) );

	}

// FIXME	private Map<BlockState, ModelResourceLocation> mapState( Block block, ModelResourceLocation defaultModel, ModelResourceLocation formedModel )
// FIXME	{
// FIXME		Map<BlockState, ModelResourceLocation> result = new HashMap<>();
// FIXME		for( BlockState state : block.getBlockState().getValidStates() )
// FIXME		{
// FIXME			if( state.get( AbstractCraftingUnitBlock.FORMED ) )
// FIXME			{
// FIXME				// Always use the builtin model if the multiblock is formed
// FIXME				result.put( state, formedModel );
// FIXME			}
// FIXME			else
// FIXME			{
// FIXME				// Use the default model
// FIXME				result.put( state, defaultModel );
// FIXME			}
// FIXME		}
// FIXME		return result;
// FIXME	}
}
