/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.parts.automation;


import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;

import com.mojang.datafixers.util.Pair;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.Material;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;


/**
 * Built-in model for annihilation planes that supports connected textures.
 */
public class PlaneModel implements IUnbakedModel
{

	private final Material frontTexture;
	private final Material sidesTexture;
	private final Material backTexture;
	private final PlaneConnections connections;

	public PlaneModel( ResourceLocation frontTexture, ResourceLocation sidesTexture, ResourceLocation backTexture, PlaneConnections connections )
	{
		this.frontTexture = new Material( AtlasTexture.LOCATION_BLOCKS_TEXTURE, frontTexture );
		this.sidesTexture = new Material( AtlasTexture.LOCATION_BLOCKS_TEXTURE, sidesTexture );
		this.backTexture = new Material( AtlasTexture.LOCATION_BLOCKS_TEXTURE, backTexture );
		this.connections = connections;
	}

	@Override
	public Collection<ResourceLocation> getDependencies()
	{
		return Collections.emptyList();
	}

	@Override
	public Collection<Material> getTextures( Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors )
	{
		return Arrays.asList( frontTexture, sidesTexture, backTexture );
	}

	@Nullable
	@Override
	public IBakedModel bakeModel( ModelBakery modelBakeryIn, Function<Material, TextureAtlasSprite> spriteGetterIn, IModelTransform transformIn, ResourceLocation locationIn )
	{
		TextureAtlasSprite frontSprite = spriteGetterIn.apply( this.frontTexture );
		TextureAtlasSprite sidesSprite = spriteGetterIn.apply( this.sidesTexture );
		TextureAtlasSprite backSprite = spriteGetterIn.apply( this.backTexture );

		return new PlaneBakedModel( frontSprite, sidesSprite, backSprite, this.connections );
	}
}
