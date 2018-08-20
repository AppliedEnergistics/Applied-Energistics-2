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

package appeng.client.render.cablebus;


import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;

import appeng.api.util.AEColor;
import appeng.core.AELog;
import appeng.core.features.registries.PartModels;


/**
 * The built-in model for the cable bus block.
 */
public class CableBusModel implements IModel
{

	private final PartModels partModels;

	public CableBusModel( PartModels partModels )
	{
		this.partModels = partModels;
	}

	@Override
	public Collection<ResourceLocation> getDependencies()
	{
		this.partModels.setInitialized( true );
		return this.partModels.getModels();
	}

	@Override
	public Collection<ResourceLocation> getTextures()
	{
		return ImmutableList.<ResourceLocation>builder()
				.addAll( CableBuilder.getTextures() )
				.build();
	}

	@Override
	public IBakedModel bake( IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter )
	{
		Map<ResourceLocation, IBakedModel> partModels = this.loadPartModels( state, format, bakedTextureGetter );

		CableBuilder cableBuilder = new CableBuilder( format, bakedTextureGetter );
		FacadeBuilder facadeBuilder = new FacadeBuilder();

		// This should normally not be used, but we *have* to provide a particle texture or otherwise damage models will
		// crash
		TextureAtlasSprite particleTexture = cableBuilder.getCoreTexture( CableCoreType.GLASS, AEColor.TRANSPARENT );

		return new CableBusBakedModel( cableBuilder, facadeBuilder, partModels, particleTexture );
	}

	private Map<ResourceLocation, IBakedModel> loadPartModels( IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter )
	{
		ImmutableMap.Builder<ResourceLocation, IBakedModel> result = ImmutableMap.builder();

		for( ResourceLocation location : this.partModels.getModels() )
		{
			IModel model = this.tryLoadPartModel( location );
			IBakedModel bakedModel = model.bake( state, format, bakedTextureGetter );
			result.put( location, bakedModel );
		}

		return result.build();
	}

	private IModel tryLoadPartModel( ResourceLocation location )
	{
		try
		{
			return ModelLoaderRegistry.getModel( location );
		}
		catch( Exception e )
		{
			AELog.error( e, "Unable to load part model " + location );
			return ModelLoaderRegistry.getMissingModel();
		}
	}

	@Override
	public IModelState getDefaultState()
	{
		return TRSRTransformation.identity();
	}
}
