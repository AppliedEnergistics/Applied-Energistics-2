/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2017, AlgorithmX2, All rights reserved.
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

package appeng.recipes.helpers;


import javax.annotation.Nonnull;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import appeng.api.recipes.ResolverResult;
import appeng.core.AELog;
import appeng.core.Api;
import appeng.core.AppEng;


/**
 * @author GuntherDW
 */
public class PartShapelessCraftingFactory extends ShapelessOreRecipe
{

	public PartShapelessCraftingFactory( ResourceLocation group, NonNullList<Ingredient> input, @Nonnull ItemStack result )
	{
		super( group, input, result );
	}

	// Copied from ShapelessOreRecipe.java, modified a bit.
	public static PartShapelessCraftingFactory factory( JsonContext context, JsonObject json )
	{
		String group = JsonUtils.getString( json, "group", "" );

		NonNullList<Ingredient> ings = NonNullList.create();
		for( JsonElement ele : JsonUtils.getJsonArray( json, "ingredients" ) )
			ings.add( CraftingHelper.getIngredient( ele, context ) );

		if( ings.isEmpty() )
			throw new JsonParseException( "No ingredients for shapeless recipe" );

		JsonObject resultObject = (JsonObject) json.get( "result" );
		int count = JsonUtils.getInt( resultObject, "count", 1 );

		String ingredient = resultObject.get( "part" ).getAsString();
		Object result = (Object) Api.INSTANCE.registries().recipes().resolveItem( AppEng.MOD_ID, ingredient );
		if( result instanceof ResolverResult )
		{
			ResolverResult resolverResult = (ResolverResult) result;

			Item item = Item.getByNameOrId( AppEng.MOD_ID + ":" + resolverResult.itemName );

			if( item == null )
			{
				AELog.warn( "item was null for " + resolverResult.itemName + " ( " + ingredient + " )!" );
				throw new JsonSyntaxException( "Got a null item for " + resolverResult.itemName + " ( " + ingredient + " ). This should never happen!" );
			}

			ItemStack itemStack = new ItemStack( item, count, resolverResult.damageValue, resolverResult.compound );

			return new PartShapelessCraftingFactory( group.isEmpty() ? null : new ResourceLocation( group ), ings, itemStack );
		}

		throw new JsonSyntaxException( "Couldn't find the resulting item in AE. This means AE was provided a recipe that it shouldn't be handling.\n" + "Was looking for : '" + ingredient + "'." );
	}
}
