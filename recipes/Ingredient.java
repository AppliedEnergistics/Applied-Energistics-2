package appeng.recipes;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import appeng.core.AppEng;
import appeng.items.materials.MaterialType;
import appeng.items.parts.ItemPart;
import appeng.items.parts.PartType;
import cpw.mods.fml.common.registry.GameRegistry;

public class Ingredient
{

	final public boolean isAir;

	final public String nameSpace;
	final public String itemName;
	final public int meta;

	final public int qty;

	public Ingredient(RecipeHandler handler, String input, int qty) throws RecipeError {

		// works no matter wat!
		this.qty = qty;

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

	public ItemStack getItemStack() throws RegistrationError, MissingIngredientError
	{
		if ( isAir )
			throw new RegistrationError( "Found blank item and expected a real item." );

		if ( nameSpace.equalsIgnoreCase( "oreDictionary" ) )
			throw new RegistrationError( "Recipe format expected a single item, but got a set of items." );

		Block blk = GameRegistry.findBlock( nameSpace, itemName );
		if ( blk == null )
			blk = GameRegistry.findBlock( nameSpace, "tile." + itemName );

		if ( blk != null )
			return new ItemStack( blk, qty, meta );

		Item it = GameRegistry.findItem( nameSpace, itemName );
		if ( it == null )
			it = GameRegistry.findItem( nameSpace, "item." + itemName );

		if ( it != null )
			return new ItemStack( it, qty, meta );

		/*
		 * Object o = Item.itemRegistry.getObject( nameSpace + ":" + itemName ); if ( o instanceof Item ) return new
		 * ItemStack( (Item) o, qty, meta );
		 * 
		 * if ( o instanceof Block ) return new ItemStack( (Block) o, qty, meta );
		 * 
		 * o = Item.itemRegistry.getObject( nameSpace + ":item." + itemName ); if ( o instanceof Item ) return new
		 * ItemStack( (Item) o, qty, meta );
		 * 
		 * o = Block.blockRegistry.getObject( nameSpace + ":tile." + itemName ); if ( o instanceof Block && (!(o
		 * instanceof BlockAir)) ) return new ItemStack( (Block) o, qty, meta );
		 */

		throw new MissingIngredientError( "Unable to find item: " + toString() );
	}

	@Override
	public String toString()
	{
		return nameSpace + ":" + itemName + ":" + meta;
	}

	public ItemStack[] getSet() throws RegistrationError, MissingIngredientError
	{
		if ( nameSpace.equalsIgnoreCase( "oreDictionary" ) )
		{
			List<ItemStack> ores = OreDictionary.getOres( itemName );
			ItemStack[] set = ores.toArray( new ItemStack[ores.size()] );

			// clone and set qty.
			for (int x = 0; x < set.length; x++)
			{
				ItemStack is = set[x].copy();
				is.stackSize = qty;
				set[x] = is;
			}

			return set;
		}

		return new ItemStack[] { getItemStack() };
	}

}
