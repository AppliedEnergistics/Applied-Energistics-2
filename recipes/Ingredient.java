package appeng.recipes;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import appeng.core.AppEng;
import appeng.items.materials.MaterialType;
import appeng.items.parts.ItemPart;
import appeng.items.parts.PartType;

public class Ingredient
{

	final public boolean isAir;

	final public String nameSpace;
	final public String itemName;
	final public int meta;

	public Ingredient(RecipeHandler handler, String input) throws RecipeError {

		if ( input.equals( "_" ) )
		{
			isAir = true;
			nameSpace = "";
			itemName = "";
			meta = OreDictionary.WILDCARD_VALUE;
			return;
		}

		isAir = false;
		String[] parts = input.split( ":" );
		if ( parts.length >= 2 )
		{
			nameSpace = handler.alias( parts[0] );
			String tmpName = handler.alias( parts[1] );

			if ( parts.length != 3 )
			{
				int sel = 0;

				if ( nameSpace.equals( "oreDictionary" ) )
				{
					if ( parts.length == 3 )
						throw new RecipeError( "Cannot specify meta when using ore dictionary." );
					sel = OreDictionary.WILDCARD_VALUE;
				}
				else if ( nameSpace.equals( AppEng.modid ) )
				{
					try
					{
						if ( tmpName.startsWith( "ItemMaterial." ) )
						{
							String materialName = tmpName.substring( tmpName.indexOf( "." ) + 1 );
							MaterialType mt = MaterialType.valueOf( materialName );
							tmpName = tmpName.substring( 0, tmpName.indexOf( "." ) );
							sel = mt.damageValue;
						}

						if ( tmpName.startsWith( "ItemPart." ) )
						{
							String partName = tmpName.substring( tmpName.indexOf( "." ) + 1 );
							PartType pt = PartType.valueOf( partName );
							tmpName = tmpName.substring( 0, tmpName.indexOf( "." ) );
							sel = ItemPart.instance.getDamageByType( pt );
						}
					}
					catch (IllegalArgumentException e)
					{
						throw new RecipeError( tmpName + " is not a valid ae2 item defintion." );
					}
				}

				meta = sel;
			}
			else
			{
				if ( parts[2].equals( "*" ) )
				{
					meta = OreDictionary.WILDCARD_VALUE;
				}
				else
				{
					try
					{
						meta = Integer.parseInt( parts[2] );
					}
					catch (NumberFormatException e)
					{
						throw new RecipeError( "Invalid Metadata." );
					}
				}
			}
			itemName = tmpName;
		}
		else
			throw new RecipeError( input + " : Needs at least Namespace and Name." );
	}

	public ItemStack getItemStack() throws RegistrationError
	{
		if ( isAir )
			throw new RegistrationError( "Found blank item and expected a real item." );

		Object o = Item.itemRegistry.getObject( nameSpace + ":" + itemName );
		if ( o instanceof Item )
			return new ItemStack( (Item) o, 1, meta );
		if ( o instanceof Block )
			return new ItemStack( (Block) o, 1, meta );

		o = Item.itemRegistry.getObject( nameSpace + ":item." + itemName );
		if ( o instanceof Item )
			return new ItemStack( (Item) o, 1, meta );

		o = Item.itemRegistry.getObject( nameSpace + ":tile." + itemName );
		if ( o instanceof Block )
			return new ItemStack( (Block) o, 1, meta );

		throw new RegistrationError( "Unable to find item: " + toString() );
	}

	@Override
	public String toString()
	{
		return nameSpace + ":" + itemName + ":" + meta;
	}

}
