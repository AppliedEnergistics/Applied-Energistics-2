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
import appeng.items.materials.ItemMultiMaterial;
import appeng.items.materials.MaterialType;
import appeng.items.misc.ItemCrystalSeed;
import appeng.items.parts.ItemMultiPart;
import appeng.items.parts.PartType;

public class AEItemResolver implements ISubItemResolver
{

	@Override
	public Object resolveItemByName(String nameSpace, String itemName)
	{

		if ( nameSpace.equals( AppEng.MOD_ID ) )
		{
			final IDefinitions definitions = AEApi.instance().definitions();
			final IItems items = definitions.items();
			final IParts parts = definitions.parts();

			if ( itemName.startsWith( "PaintBall." ) )
			{
				for ( AEColoredItemDefinition definition : items.coloredPaintBall().asSet() )
				{
					return this.paintBall( definition, itemName.substring( itemName.indexOf( '.' ) + 1 ), false  );
				}
			}

			if ( itemName.startsWith( "LumenPaintBall." ) )
			{
				for ( AEColoredItemDefinition definition : items.coloredLumenPaintBall().asSet() )
				{
					return this.paintBall( definition, itemName.substring( itemName.indexOf( '.' ) + 1 ), true  );
				}
			}

			if ( itemName.equals( "CableGlass" ) )
			{
				for ( AEColoredItemDefinition definition : parts.cableGlass().asSet() )
				{
					return new ResolverResultSet( "CableGlass", definition.allStacks( 1 ) );
				}
			}

			if ( itemName.startsWith( "CableGlass." ) )
			{
				for ( AEColoredItemDefinition definition : parts.cableGlass().asSet() )
				{
					return this.cableItem( definition, itemName.substring( itemName.indexOf( '.' ) + 1 ) );
				}
			}

			if ( itemName.equals( "CableCovered" ) )
			{
				for ( AEColoredItemDefinition definition : parts.cableCovered().asSet() )
				{
					return new ResolverResultSet( "CableCovered", definition.allStacks( 1 ) );
				}
			}

			if ( itemName.startsWith( "CableCovered." ) )
			{
				for ( AEColoredItemDefinition definition : parts.cableCovered().asSet() )
				{
					return this.cableItem( definition, itemName.substring( itemName.indexOf( '.' ) + 1 ) );
				}
			}

			if ( itemName.equals( "CableSmart" ) )
			{
				for ( AEColoredItemDefinition definition : parts.cableSmart().asSet() )
				{
					return new ResolverResultSet( "CableSmart", definition.allStacks( 1 ) );
				}
			}

			if ( itemName.startsWith( "CableSmart." ) )
			{
				for ( AEColoredItemDefinition definition : parts.cableSmart().asSet() )
				{
					return this.cableItem( definition, itemName.substring( itemName.indexOf( '.' ) + 1 ) );
				}
			}

			if ( itemName.equals( "CableDense" ) )
			{
				for ( AEColoredItemDefinition definition : parts.cableDense().asSet() )
				{
					return new ResolverResultSet( "CableDense", definition.allStacks( 1 ) );
				}
			}

			if ( itemName.startsWith( "CableDense." ) )
			{
				for ( AEColoredItemDefinition definition : parts.cableDense().asSet() )
				{
					return this.cableItem( definition, itemName.substring( itemName.indexOf( '.' ) + 1 ) );
				}
			}

			if ( itemName.startsWith( "ItemCrystalSeed." ) )
			{
				if ( itemName.equalsIgnoreCase( "ItemCrystalSeed.Certus" ) )
					return ItemCrystalSeed.getResolver( ItemCrystalSeed.Certus );
				if ( itemName.equalsIgnoreCase( "ItemCrystalSeed.Nether" ) )
					return new ResolverResult( "ItemCrystalSeed", ItemCrystalSeed.Nether );
				if ( itemName.equalsIgnoreCase( "ItemCrystalSeed.Fluix" ) )
					return new ResolverResult( "ItemCrystalSeed", ItemCrystalSeed.Fluix );

				return null;
			}

			if ( itemName.startsWith( "ItemMaterial." ) )
			{
				String materialName = itemName.substring( itemName.indexOf( '.' ) + 1 );
				MaterialType mt = MaterialType.valueOf( materialName );
				// itemName = itemName.substring( 0, itemName.indexOf( "." ) );
				if ( mt.itemInstance == ItemMultiMaterial.instance && mt.damageValue >= 0 && mt.isRegistered() )
					return new ResolverResult( "ItemMultiMaterial", mt.damageValue );
			}

			if ( itemName.startsWith( "ItemPart." ) )
			{
				String partName = itemName.substring( itemName.indexOf( '.' ) + 1 );
				PartType pt = PartType.valueOf( partName );
				// itemName = itemName.substring( 0, itemName.indexOf( "." ) );
				int dVal = ItemMultiPart.instance.getDamageByType( pt );
				if ( dVal >= 0 )
					return new ResolverResult( "ItemMultiPart", dVal );
			}
		}

		return null;
	}

	private Object paintBall(AEColoredItemDefinition partType, String substring, boolean lumen)
	{
		AEColor col;

		try
		{
			col = AEColor.valueOf( substring );
		}
		catch (Throwable t)
		{
			col = AEColor.Transparent;
		}

		if ( col == AEColor.Transparent )
			return null;

		ItemStack is = partType.stack( col, 1 );
		return new ResolverResult( "ItemPaintBall", (lumen ? 20 : 0) + is.getItemDamage() );
	}

	private Object cableItem(AEColoredItemDefinition partType, String substring)
	{
		AEColor col;

		try
		{
			col = AEColor.valueOf( substring );
		}
		catch (Throwable t)
		{
			col = AEColor.Transparent;
		}

		ItemStack is = partType.stack( col, 1 );
		return new ResolverResult( "ItemMultiPart", is.getItemDamage() );
	}
}
