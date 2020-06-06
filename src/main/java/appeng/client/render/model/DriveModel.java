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


import appeng.block.storage.DriveSlotState;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import java.util.*;
import java.util.function.Function;

public class DriveModel implements IModelGeometry<DriveModel>
{

	private static final ResourceLocation MODEL_BASE = new ResourceLocation( "appliedenergistics2:block/drive_base" );

	private static final Map<DriveSlotState, ResourceLocation> MODELS_CELLS = ImmutableMap.of(
			DriveSlotState.EMPTY, new ResourceLocation( "appliedenergistics2:block/drive_cell_empty" ),
			DriveSlotState.OFFLINE, new ResourceLocation( "appliedenergistics2:block/drive_cell_off" ),
			DriveSlotState.ONLINE, new ResourceLocation( "appliedenergistics2:block/drive_cell_on" ),
			DriveSlotState.TYPES_FULL, new ResourceLocation( "appliedenergistics2:block/drive_cell_types_full" ),
			DriveSlotState.FULL, new ResourceLocation( "appliedenergistics2:block/drive_cell_full" ) );

	@Override
	public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation) {
		EnumMap<DriveSlotState, IBakedModel> cellModels = new EnumMap<>( DriveSlotState.class );

		// Load the base model and the model for each cell state.
		for( DriveSlotState slotState : MODELS_CELLS.keySet() )
		{
			IBakedModel cellModel = bakery.getBakedModel( MODELS_CELLS.get( slotState ), modelTransform, spriteGetter );
			cellModels.put( slotState, cellModel );
		}

		IBakedModel baseModel = bakery.getBakedModel( MODEL_BASE, modelTransform, spriteGetter );
		return new DriveBakedModel( baseModel, cellModels );
	}

	@Override
	public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
		return Collections.emptyList();
	}

}
