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

package appeng.bootstrap.components;


import java.util.HashMap;
import java.util.Map;

import appeng.core.AppEng;
import com.google.common.base.Preconditions;

import net.minecraft.client.renderer.model.Model;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.ModelLoaderRegistry;

import appeng.client.render.model.BuiltInModelLoader;


@OnlyIn( Dist.CLIENT )
public class BuiltInModelComponent implements IPreInitComponent
{

	private final Map<String, Model> builtInModels = new HashMap<>();

	private boolean hasInitialized = false;

	public void addModel( String path, Model model )
	{
		Preconditions.checkState( !this.hasInitialized );
		this.builtInModels.put( path, model );
	}

	@Override
	public void preInitialize( Dist side )
	{
		this.hasInitialized = true;

		BuiltInModelLoader loader = new BuiltInModelLoader( this.builtInModels );

		// TODO find out how to actually do it
		ModelLoaderRegistry.registerLoader( new ResourceLocation( AppEng.MOD_ID, "model"), loader );
	}
}
