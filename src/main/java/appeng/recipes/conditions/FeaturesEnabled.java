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

package appeng.recipes.conditions;


import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.api.features.AEFeature;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.StreamSupport;


public class FeaturesEnabled implements ICondition
{
	private static final ResourceLocation NAME = new ResourceLocation( AppEng.MOD_ID, "feature" );
	private final AEFeature[] features;

	public FeaturesEnabled( AEFeature... features )
	{
		this.features = features;
	}

	public FeaturesEnabled( Set<AEFeature> features ) {
		this(features.toArray(new AEFeature[0]));
	}

	@Override
	public ResourceLocation getID()
	{
		return NAME;
	}

	@Override
	public boolean test()
	{
		for( AEFeature feature : features )
		{
			if( !AEConfig.instance().isFeatureEnabled( feature ) )
			{
				return false;
			}
		}

		return true;
	}

	public static class Serializer implements IConditionSerializer<FeaturesEnabled>
	{
		private static final String JSON_FEATURES_KEY = "features";

		public static final Serializer INSTANCE = new Serializer();

		private Serializer()
		{
		}

		@Override
		public void write( JsonObject json, FeaturesEnabled value )
		{
			json.add( JSON_FEATURES_KEY, Arrays.stream( value.features )
					.map( AEFeature::toString )
					.reduce( new JsonArray(), ( JsonArray array, String string ) -> {
						array.add( string );
						return array;
					}, ( a, b ) -> b ) );
		}

		@Override
		public FeaturesEnabled read( JsonObject jsonObject )
		{
			AEFeature[] features;

			if( JSONUtils.isJsonArray( jsonObject, JSON_FEATURES_KEY ) )
			{
				final JsonArray featuresArray = JSONUtils.getJsonArray( jsonObject, JSON_FEATURES_KEY );

				features = StreamSupport.stream( featuresArray.spliterator(), false )
						.filter( JsonElement::isJsonPrimitive )
						.map( JsonElement::getAsString )
						.map( s -> s.toUpperCase( Locale.ENGLISH ) )
						.map( AEFeature::valueOf )
						.toArray( AEFeature[]::new );
			}
			else if( JSONUtils.isString( jsonObject, JSON_FEATURES_KEY ) )
			{
				final String featureName = JSONUtils.getString( jsonObject, JSON_FEATURES_KEY ).toUpperCase( Locale.ENGLISH );
				features = new AEFeature[] { AEFeature.valueOf( featureName ) };
			}
			else
			{
				features = new AEFeature[] {};
			}

			return new FeaturesEnabled( features );
		}

		@Override
		public ResourceLocation getID()
		{
			return NAME;
		}

	}

}