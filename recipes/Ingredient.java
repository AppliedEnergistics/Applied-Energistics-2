package appeng.recipes;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import appeng.api.AEApi;
import appeng.api.exceptions.MissingIngredientError;
import appeng.api.exceptions.RecipeError;
import appeng.api.exceptions.RegistrationError;
import appeng.api.recipes.IIngredient;
import appeng.api.recipes.ResolverResult;
import appeng.api.recipes.ResolverResultSet;
import cpw.mods.fml.common.registry.GameRegistry;

public class Ingredient implements IIngredient
{

	final public boolean isAir;

	final public String nameSpace;
	final public String itemName;
	final public int meta;

	final public int qty;

	public Ingredient(RecipeHandler handler, String input, int qty) throws RecipeError, MissedIngredientSet {

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
				else
				{
					try
					{
						Object ro = AEApi.instance().registries().recipes().resolveItem( nameSpace, tmpName );
						if ( ro instanceof ResolverResult )
						{
							ResolverResult rr = (ResolverResult) ro;
							tmpName = rr.itemName;
							sel = rr.damageValue;
						}
						else if ( ro instanceof ResolverResultSet )
						{
							throw new MissedIngredientSet( (ResolverResultSet) ro );
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
		
		handler.data.knownItem.add( toString() );		
	}

	@Override
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
		{
			Item it = Item.getItemFromBlock( blk );
			if ( it != null )
				return new ItemStack( it, qty, meta );
		}

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

	@Override
	public ItemStack[] getItemStackSet() throws RegistrationError, MissingIngredientError
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

			if ( set.length == 0 )
				throw new MissingIngredientError( getItemName() + " - ore dictionary could not be resolved to any items." );

			return set;
		}

		return new ItemStack[] { getItemStack() };
	}

	@Override
	public String getNameSpace()
	{
		return nameSpace;
	}

	@Override
	public String getItemName()
	{
		return itemName;
	}

	@Override
	public int getDamageValue()
	{
		return meta;
	}

	@Override
	public int getQty()
	{
		return qty;
	}

	@Override
	public boolean isAir()
	{
		return isAir;
	}

}
