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
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.Material;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

import appeng.api.util.AEColor;
import appeng.core.features.registries.PartModels;


/**
 * The built-in model for the cable bus block.
 */
public class CableBusModel implements IUnbakedModel
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
	public Collection<Material> getTextures( Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors )
	{
		return Collections.unmodifiableList( CableBuilder.getTextures() );
	}

	@Nullable
	@Override
	public IBakedModel bakeModel( ModelBakery modelBakeryIn, Function<Material, TextureAtlasSprite> spriteGetterIn, IModelTransform transformIn, ResourceLocation locationIn )
	{
		Map<ResourceLocation, IBakedModel> partModels = this.loadPartModels( modelBakeryIn, spriteGetterIn, transformIn );

		CableBuilder cableBuilder = new CableBuilder( spriteGetterIn );
		FacadeBuilder facadeBuilder = new FacadeBuilder();

		// This should normally not be used, but we *have* to provide a particle texture or otherwise damage models will
		// crash
		TextureAtlasSprite particleTexture = cableBuilder.getCoreTexture( CableCoreType.GLASS, AEColor.TRANSPARENT );

		return new CableBusBakedModel( cableBuilder, facadeBuilder, partModels, particleTexture );
	}

	private Map<ResourceLocation, IBakedModel> loadPartModels( ModelBakery modelBakeryIn, Function<Material, TextureAtlasSprite> spriteGetterIn, IModelTransform transformIn )
	{
		ImmutableMap.Builder<ResourceLocation, IBakedModel> result = ImmutableMap.builder();

		for( ResourceLocation location : this.partModels.getModels() )
		{
			result.put( location, modelBakeryIn.getBakedModel( location, transformIn, spriteGetterIn ) );
		}

		return result.build();
	}
}
