package appeng.recipes;

import appeng.api.recipes.ISubItemResolver;
import appeng.api.recipes.ResolveResult;
import appeng.core.AppEng;
import appeng.items.materials.MaterialType;
import appeng.items.misc.ItemCrystalSeed;
import appeng.items.parts.ItemPart;
import appeng.items.parts.PartType;

public class AEItemResolver implements ISubItemResolver
{

	@Override
	public ResolveResult resolveItemByName(String nameSpace, String itemName)
	{

		if ( nameSpace.equals( AppEng.modid ) )
		{
			if ( itemName.startsWith( "ItemCrystalSeed." ) )
			{
				if ( itemName.equalsIgnoreCase( "ItemCrystalSeed.Certus" ) )
					return new ResolveResult( "ItemCrystalSeed", ItemCrystalSeed.Certus );
				if ( itemName.equalsIgnoreCase( "ItemCrystalSeed.Nether" ) )
					return new ResolveResult( "ItemCrystalSeed", ItemCrystalSeed.Nether );
				if ( itemName.equalsIgnoreCase( "ItemCrystalSeed.Fluix" ) )
					return new ResolveResult( "ItemCrystalSeed", ItemCrystalSeed.Fluix );

				return null;
			}

			if ( itemName.startsWith( "ItemMaterial." ) )
			{
				String materialName = itemName.substring( itemName.indexOf( "." ) + 1 );
				MaterialType mt = MaterialType.valueOf( materialName );
				itemName = itemName.substring( 0, itemName.indexOf( "." ) );
				return new ResolveResult( itemName, mt.damageValue );
			}

			if ( itemName.startsWith( "ItemPart." ) )
			{
				String partName = itemName.substring( itemName.indexOf( "." ) + 1 );
				PartType pt = PartType.valueOf( partName );
				itemName = itemName.substring( 0, itemName.indexOf( "." ) );
				return new ResolveResult( itemName, ItemPart.instance.getDamageByType( pt ) );
			}
		}

		return null;
	}

}
