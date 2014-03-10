package appeng.recipes;

import net.minecraft.item.ItemStack;
import appeng.api.AEApi;
import appeng.api.recipes.ISubItemResolver;
import appeng.api.recipes.ResolverResult;
import appeng.api.recipes.ResolverResultSet;
import appeng.api.util.AEColor;
import appeng.api.util.AEColoredItemDefinition;
import appeng.core.AppEng;
import appeng.items.materials.MaterialType;
import appeng.items.misc.ItemCrystalSeed;
import appeng.items.parts.ItemPart;
import appeng.items.parts.PartType;

public class AEItemResolver implements ISubItemResolver
{

	@Override
	public Object resolveItemByName(String nameSpace, String itemName)
	{

		if ( nameSpace.equals( AppEng.modid ) )
		{
			if ( itemName.equals( "CableGlass" ) )
			{
				return new ResolverResultSet( "CableGlass", AEApi.instance().parts().partCableGlass.allStacks( 1 ) );
			}

			if ( itemName.startsWith( "CableGlass." ) )
			{
				return cableItem( AEApi.instance().parts().partCableGlass, itemName.substring( itemName.indexOf( "." ) + 1 ) );
			}

			if ( itemName.equals( "CableCovered" ) )
			{
				return new ResolverResultSet( "CableCovered", AEApi.instance().parts().partCableCovered.allStacks( 1 ) );
			}

			if ( itemName.startsWith( "CableCovered." ) )
			{
				return cableItem( AEApi.instance().parts().partCableCovered, itemName.substring( itemName.indexOf( "." ) + 1 ) );
			}

			if ( itemName.equals( "CableSmart" ) )
			{
				return new ResolverResultSet( "CableSmart", AEApi.instance().parts().partCableSmart.allStacks( 1 ) );
			}

			if ( itemName.startsWith( "CableSmart." ) )
			{
				return cableItem( AEApi.instance().parts().partCableSmart, itemName.substring( itemName.indexOf( "." ) + 1 ) );
			}

			if ( itemName.equals( "CableDense" ) )
			{
				return new ResolverResultSet( "CableDense", AEApi.instance().parts().partCableDense.allStacks( 1 ) );
			}

			if ( itemName.startsWith( "CableDense." ) )
			{
				return cableItem( AEApi.instance().parts().partCableDense, itemName.substring( itemName.indexOf( "." ) + 1 ) );
			}

			if ( itemName.startsWith( "ItemCrystalSeed." ) )
			{
				if ( itemName.equalsIgnoreCase( "ItemCrystalSeed.Certus" ) )
					return new ResolverResult( "ItemCrystalSeed", ItemCrystalSeed.Certus );
				if ( itemName.equalsIgnoreCase( "ItemCrystalSeed.Nether" ) )
					return new ResolverResult( "ItemCrystalSeed", ItemCrystalSeed.Nether );
				if ( itemName.equalsIgnoreCase( "ItemCrystalSeed.Fluix" ) )
					return new ResolverResult( "ItemCrystalSeed", ItemCrystalSeed.Fluix );

				return null;
			}

			if ( itemName.startsWith( "ItemMaterial." ) )
			{
				String materialName = itemName.substring( itemName.indexOf( "." ) + 1 );
				MaterialType mt = MaterialType.valueOf( materialName );
				itemName = itemName.substring( 0, itemName.indexOf( "." ) );
				return new ResolverResult( itemName, mt.damageValue );
			}

			if ( itemName.startsWith( "ItemPart." ) )
			{
				String partName = itemName.substring( itemName.indexOf( "." ) + 1 );
				PartType pt = PartType.valueOf( partName );
				itemName = itemName.substring( 0, itemName.indexOf( "." ) );
				return new ResolverResult( itemName, ItemPart.instance.getDamageByType( pt ) );
			}
		}

		return null;
	}

	private Object cableItem(AEColoredItemDefinition partType, String substring)
	{
		AEColor col = AEColor.Transparent;

		try
		{
			col = AEColor.valueOf( substring );
		}
		catch (Throwable t)
		{
			col = AEColor.Transparent;
		}

		ItemStack is = partType.stack( col, 1 );
		return new ResolverResult( "ItemPart", is.getItemDamage() );
	}
}
