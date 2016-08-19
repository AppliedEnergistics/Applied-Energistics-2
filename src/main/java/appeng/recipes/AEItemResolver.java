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

package appeng.recipes;


import net.minecraft.item.ItemStack;

import appeng.api.AEApi;
import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.IItems;
import appeng.api.definitions.IParts;
import appeng.api.recipes.ISubItemResolver;
import appeng.api.recipes.ResolverResult;
import appeng.api.recipes.ResolverResultSet;
import appeng.api.util.AEColor;
import appeng.api.util.AEColoredItemDefinition;
import appeng.core.AppEng;
import appeng.items.materials.MaterialType;
import appeng.items.materials.ItemMultiItem;
import appeng.items.misc.ItemCrystalSeed;
import appeng.items.parts.ItemMultiPart;
import appeng.items.parts.PartType;


public class AEItemResolver implements ISubItemResolver
{

	@Override
	public Object resolveItemByName( final String nameSpace, final String itemName )
	{

		if( nameSpace.equals( AppEng.MOD_ID ) )
		{
			final IDefinitions definitions = AEApi.instance().definitions();
			final IItems items = definitions.items();
			final IParts parts = definitions.parts();

			if( itemName.startsWith( "PaintBall." ) )
			{
				return this.paintBall( items.coloredPaintBall(), itemName.substring( itemName.indexOf( '.' ) + 1 ), false );
			}

			if( itemName.startsWith( "LumenPaintBall." ) )
			{
				return this.paintBall( items.coloredLumenPaintBall(), itemName.substring( itemName.indexOf( '.' ) + 1 ), true );
			}

			if( itemName.equals( "CableGlass" ) )
			{
				return new ResolverResultSet( "CableGlass", parts.cableGlass().allStacks( 1 ) );
			}

			if( itemName.startsWith( "CableGlass." ) )
			{
				return this.cableItem( parts.cableGlass(), itemName.substring( itemName.indexOf( '.' ) + 1 ) );
			}

			if( itemName.equals( "CableCovered" ) )
			{
				return new ResolverResultSet( "CableCovered", parts.cableCovered().allStacks( 1 ) );
			}

			if( itemName.startsWith( "CableCovered." ) )
			{
				return this.cableItem( parts.cableCovered(), itemName.substring( itemName.indexOf( '.' ) + 1 ) );
			}

			if( itemName.equals( "CableSmart" ) )
			{
				return new ResolverResultSet( "CableSmart", parts.cableSmart().allStacks( 1 ) );
			}

			if( itemName.startsWith( "CableSmart." ) )
			{
				return this.cableItem( parts.cableSmart(), itemName.substring( itemName.indexOf( '.' ) + 1 ) );
			}

			if( itemName.equals( "CableDense" ) )
			{
				return new ResolverResultSet( "CableDense", parts.cableDense().allStacks( 1 ) );
			}

			if( itemName.startsWith( "CableDense." ) )
			{
				return this.cableItem( parts.cableDense(), itemName.substring( itemName.indexOf( '.' ) + 1 ) );
			}

			if( itemName.startsWith( "ItemCrystalSeed." ) )
			{
				if( itemName.equalsIgnoreCase( "ItemCrystalSeed.Certus" ) )
				{
					return ItemCrystalSeed.getResolver( ItemCrystalSeed.CERTUS );
				}
				if( itemName.equalsIgnoreCase( "ItemCrystalSeed.Nether" ) )
				{
					return ItemCrystalSeed.getResolver( ItemCrystalSeed.NETHER );
				}
				if( itemName.equalsIgnoreCase( "ItemCrystalSeed.Fluix" ) )
				{
					return ItemCrystalSeed.getResolver( ItemCrystalSeed.FLUIX );
				}

				return null;
			}

			if( itemName.startsWith( "ItemMaterial." ) )
			{
				final String materialName = itemName.substring( itemName.indexOf( '.' ) + 1 );
				final MaterialType mt = MaterialType.valueOf( materialName );
				// itemName = itemName.substring( 0, itemName.indexOf( "." ) );
				if( mt.getItemInstance() == ItemMultiItem.instance && mt.getDamageValue() >= 0 && mt.isRegistered() )
				{
					return new ResolverResult( "ItemMultiMaterial", mt.getDamageValue() );
				}
			}

			if( itemName.startsWith( "ItemPart." ) )
			{
				final String partName = itemName.substring( itemName.indexOf( '.' ) + 1 );
				final PartType pt = PartType.valueOf( partName );
				// itemName = itemName.substring( 0, itemName.indexOf( "." ) );
				final int dVal = ItemMultiPart.instance.getDamageByType( pt );
				if( dVal >= 0 )
				{
					return new ResolverResult( "ItemMultiPart", dVal );
				}
			}
		}

		return null;
	}

	private Object paintBall( final AEColoredItemDefinition partType, final String substring, final boolean lumen )
	{
		AEColor col;

		try
		{
			col = AEColor.valueOf( substring );
		}
		catch( final Throwable t )
		{
			col = AEColor.Transparent;
		}

		if( col == AEColor.Transparent )
		{
			return null;
		}

		final ItemStack is = partType.stack( col, 1 );
		return new ResolverResult( "ItemPaintBall", ( lumen ? 20 : 0 ) + is.getItemDamage() );
	}

	private Object cableItem( final AEColoredItemDefinition partType, final String substring )
	{
		AEColor col;

		try
		{
			col = AEColor.valueOf( substring );
		}
		catch( final Throwable t )
		{
			col = AEColor.Transparent;
		}

		final ItemStack is = partType.stack( col, 1 );
		return new ResolverResult( "ItemMultiPart", is.getItemDamage() );
	}
}
