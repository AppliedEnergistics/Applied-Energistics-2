
package appeng.recipes.handlers;


import com.google.gson.JsonObject;

import net.minecraft.item.ItemStack;
import net.minecraft.util.JSONUtils;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;

import appeng.api.AEApi;
import appeng.api.features.IGrinderRecipeBuilder;
import appeng.api.features.IGrinderRegistry;
import appeng.recipes.IAERecipeFactory;
import appeng.recipes.factories.recipes.PartRecipeFactory;


public class GrinderHandler implements IAERecipeFactory
{

	@Override
	public void register( JsonObject json, JsonContext ctx )
	{
		// TODO only primary for now

		JsonObject result = JSONUtils.getJsonObject( json, "result" );
		ItemStack primary = PartRecipeFactory.getResult( result, ctx, "primary" );
		ItemStack[] input = CraftingHelper.getIngredient( json.get( "input" ), ctx ).getMatchingStacks();

		int turns = 5;
		if( json.has( "turns" ) )
		{
			turns = JSONUtils.getInt( json, "turns" );
		}

		final IGrinderRegistry reg = Api.INSTANCE.registries().grinder();
		for( int i = 0; i < input.length; ++i )
		{
			final IGrinderRecipeBuilder builder = reg.builder();

			builder.withOutput( primary );
			builder.withInput( input[i] );
			builder.withTurns( turns );

			reg.addRecipe( builder.build() );
		}
	}
}
