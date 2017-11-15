
package appeng.recipes.factories.recipes;


import java.util.Iterator;

import javax.annotation.Nonnull;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.ShapelessOreRecipe;


public class DamagedRecipeFactory implements IRecipeFactory
{
	@Override
	public IRecipe parse( JsonContext context, JsonObject json )
	{
		String group = JsonUtils.getString( json, "group", "" );

		NonNullList<Ingredient> ings = NonNullList.create();
		for( JsonElement ele : JsonUtils.getJsonArray( json, "ingredients" ) )
		{
			ings.add( CraftingHelper.getIngredient( ele, context ) );
		}

		if( ings.isEmpty() )
		{
			throw new JsonParseException( "No ingredients for shapeless recipe" );
		}

		return new DamagedRecipe( group.isEmpty() ? null : new ResourceLocation( group ), ings, PartRecipeFactory.getResult( json, context ) );
	}

	private static class DamagedRecipe extends ShapelessOreRecipe
	{
		public DamagedRecipe( ResourceLocation group, NonNullList<Ingredient> input, ItemStack result )
		{
			super( group, input, result );
		}

		@Override
		public boolean matches( @Nonnull InventoryCrafting inv, @Nonnull World world )
		{
			NonNullList<Ingredient> required = NonNullList.create();
			required.addAll( input );

			for( int i = 0; i < inv.getSizeInventory(); ++i )
			{
				final ItemStack current = inv.getStackInSlot( i );
				if( !current.isEmpty() )
				{
					boolean inRecipe = false;
					final Iterator<Ingredient> req = required.iterator();

					while( req.hasNext() )
					{
						if( req.next().apply( current ) )
						{
							inRecipe = true;
							req.remove();
							break;
						}
					}
					if( !inRecipe )
					{
						return false;
					}
				}
			}

			return required.isEmpty();
		}
	}
}
