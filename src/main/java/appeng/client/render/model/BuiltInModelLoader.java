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

package appeng.client.render.model;


import java.util.Map;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableMap;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelLoader;

import appeng.core.AppEng;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.resource.IResourceType;


/**
 * Manages built-in models.
 */
public class BuiltInModelLoader implements IModelLoader<OBJModel>
{

	private final Map<String, OBJModel> builtInModels;

	public BuiltInModelLoader( Map<String, OBJModel> builtInModels )
	{
		this.builtInModels = ImmutableMap.copyOf( builtInModels );
	}

	@Override
	public boolean accepts( ResourceLocation modelLocation )
	{
		if( !modelLocation.getNamespace().equals( AppEng.MOD_ID ) )
		{
			return false;
		}

		return this.builtInModels.containsKey( modelLocation.getPath() );
	}

	public OBJModel loadModel(OBJModel.ModelSettings settings)
	{
		return this.builtInModels.get( settings.modelLocation.getPath() );
	}

	@Override
	public void onResourceManagerReload( IResourceManager resourceManager )
	{
		for( OBJModel model : this.builtInModels.values() )
		{
			if( model instanceof IResourceManagerReloadListener )
			{
				( (IResourceManagerReloadListener) model ).onResourceManagerReload( resourceManager );
			}
		}
	}
}
