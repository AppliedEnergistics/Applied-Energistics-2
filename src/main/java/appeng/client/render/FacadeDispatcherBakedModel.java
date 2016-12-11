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


import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import appeng.items.parts.ItemFacade;


/**
 * This baked model class is used as a dispatcher to redirect the renderer to the *real* model that should be used based
 * on the item stack.
 * A custom Item Override List is used to accomplish this.
 */
public class FacadeDispatcherBakedModel implements IBakedModel
{

	private final IBakedModel baseModel;

	private final VertexFormat format;

	public FacadeDispatcherBakedModel( IBakedModel baseModel, VertexFormat format )
	{
		this.baseModel = baseModel;
		this.format = format;
	}

	// This is never used. See the item override list below.
	@Override
	public List<BakedQuad> getQuads( @Nullable IBlockState state, @Nullable EnumFacing side, long rand )
	{
		return Collections.emptyList();
	}

	@Override
	public boolean isAmbientOcclusion()
	{
		return baseModel.isAmbientOcclusion();
	}

	@Override
	public boolean isGui3d()
	{
		return baseModel.isGui3d();
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
		return new ItemOverrideList( Collections.emptyList() ){
			@Override
			public IBakedModel handleItemState( IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity )
			{
				if( !( stack.getItem() instanceof ItemFacade ) )
				{
					return originalModel;
				}

				ItemFacade itemFacade = (ItemFacade) stack.getItem();

				IBlockState state = itemFacade.getTextureBlockState( stack );
				ItemStack textureItem = itemFacade.getTextureItem( stack );

				return new FacadeWithBlockBakedModel( baseModel, state, textureItem, format );
			}
		};
	}
}
