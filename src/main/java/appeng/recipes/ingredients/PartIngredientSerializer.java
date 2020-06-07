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

package appeng.recipes.ingredients;


import appeng.api.AEApi;
import appeng.api.recipes.ResolverResult;
import appeng.api.recipes.ResolverResultSet;
import appeng.core.AELog;
import appeng.core.AppEng;
import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;


public class PartIngredientSerializer implements IIngredientSerializer<PartIngredient>
{
	public static PartIngredientSerializer INSTANCE = new PartIngredientSerializer();

	private PartIngredientSerializer()
	{
	}

	@Nonnull
	@Override
	public PartIngredient parse( JsonObject json )
	{
		final String partName = json.get( "part" ).getAsString();
		return getPart( partName );
	}

	@Nonnull
	@Override
	public PartIngredient parse( PacketBuffer buffer )
	{
		return getPart( buffer.readString() );
	}

	@Override
	public void write( @Nonnull PacketBuffer buffer, @Nonnull PartIngredient ingredient )
	{
		buffer.writeString( ingredient.getPartName() );
	}

	private PartIngredient getPart( String partName )
	{
		final Object result = AEApi.instance().registries().recipes().resolveItem( AppEng.MOD_ID, partName );

		if( result instanceof ResolverResultSet )
		{
			final ResolverResultSet resolverResultSet = (ResolverResultSet) result;

			return PartIngredient
					.fromStacks( partName, resolverResultSet.results.toArray( new ItemStack[0] ) );
		}
		else if( result instanceof ResolverResult )
		{
			final ResolverResult resolverResult = (ResolverResult) result;

			final Item item = ForgeRegistries.ITEMS.getValue( new ResourceLocation( AppEng.MOD_ID, resolverResult.itemName ) );
			final ItemStack itemStack = new ItemStack( item, 1, resolverResult.compound );

			itemStack.setDamage( resolverResult.damageValue );

			return PartIngredient.fromStacks( partName, itemStack );
		}

		AELog.warn( "Looking for ingredient with name '" + partName + "' ended up with a null item!" );
		return PartIngredient.empty( partName );
	}

}