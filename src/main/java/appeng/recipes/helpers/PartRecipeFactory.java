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


import appeng.core.AppEng;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;


/**
 * @author GuntherDW
 */
public class PartRecipeFactory implements IRecipeFactory
{
	@Override
	public IRecipe parse( JsonContext context, JsonObject json )
	{
		String type = JsonUtils.getString( json, "type" );
		if( type.contains( "shaped" ) )
		{
			PartShapedCraftingFactory recipe = PartShapedCraftingFactory.factory( context, json );
			CraftingHelper.ShapedPrimer primer = new CraftingHelper.ShapedPrimer();
			primer.width = recipe.getWidth();
			primer.height = recipe.getHeight();
			primer.mirrored = JsonUtils.getBoolean( json, "mirrored", true );
			primer.input = recipe.getIngredients();

			return new PartShapedCraftingFactory( new ResourceLocation( AppEng.MOD_ID, "part_shaped_crafting" ), recipe.getRecipeOutput(), primer );
		}
		else if( type.contains( "shapeless" ) )
		{
			PartShapelessCraftingFactory recipe = PartShapelessCraftingFactory.factory( context, json );

			return new PartShapelessCraftingFactory( new ResourceLocation( AppEng.MOD_ID, "part_shapeless_crafting" ), recipe.getIngredients(), recipe
					.getRecipeOutput() );
		}
		else
		{
			throw new JsonSyntaxException( "Applied Energistics 2 was given a custom recipe that it does not know how to handle!\n" + "Type should either be '" + AppEng.MOD_ID + ":shapeless' or '" + AppEng.MOD_ID + ":shaped', got '" + type + "'!" );
		}
	}
}
