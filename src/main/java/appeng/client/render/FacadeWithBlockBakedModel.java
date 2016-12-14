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

package appeng.client.render;


import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import appeng.client.render.cablebus.CubeBuilder;
import appeng.client.render.cablebus.FacadeBuilder;


/**
 * This is the actual baked model that will combine the north face of a given block state
 * with the base facade item model to achieve what is then actually rendered on screen.
 */
public class FacadeWithBlockBakedModel implements IBakedModel
{

	private final IBakedModel baseModel;

	private final IBlockState blockState;

	private final IBakedModel textureModel;

	private final VertexFormat format;

	private final ItemStack textureItem;

	public FacadeWithBlockBakedModel( IBakedModel baseModel, IBlockState blockState, ItemStack textureItem, VertexFormat format )
	{
		this.baseModel = baseModel;
		this.blockState = blockState;
		this.textureItem = textureItem;
		this.textureModel = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState( blockState );
		this.format = format;
	}

	@Override
	public List<BakedQuad> getQuads( @Nullable IBlockState state, @Nullable EnumFacing side, long rand )
	{
		// Only the north side is actually read from the base model for item models
		if( side == EnumFacing.NORTH )
		{
			List<BakedQuad> quads = new ArrayList<>( 1 );
			CubeBuilder builder = new CubeBuilder( format, quads );
			FacadeBuilder.TextureAtlasAndTint sprite = FacadeBuilder.getSprite( textureModel, blockState, side, rand );
			if ( sprite != null && sprite.getSprite() != null )
			{
				if( sprite.getTint() != -1 )
				{
					builder.setColor( Minecraft.getMinecraft().getItemColors().getColorFromItemstack( textureItem, sprite.getTint() ) );
				}
				builder.setTexture( sprite.getSprite() );
				builder.setDrawFaces( EnumSet.of( EnumFacing.NORTH ) );
				builder.addCube( 0, 0, 0, 16, 16, 16 );
				return quads;
			}
		}

		return baseModel.getQuads( state, side, rand );
	}

	@Override
	public boolean isAmbientOcclusion()
	{
		return baseModel.isAmbientOcclusion();
	}

	@Override
	public boolean isGui3d()
	{
		return false;
	}

	@Override
	public boolean isBuiltInRenderer()
	{
		return false;
	}

	@Override
	public TextureAtlasSprite getParticleTexture()
	{
		return baseModel.getParticleTexture();
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms()
	{
		return baseModel.getItemCameraTransforms();
	}

	@Override
	public ItemOverrideList getOverrides()
	{
		return ItemOverrideList.NONE;
	}
}
