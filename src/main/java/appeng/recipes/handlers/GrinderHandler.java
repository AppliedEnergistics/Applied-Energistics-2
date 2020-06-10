
package appeng.recipes.handlers;


import com.google.gson.JsonObject;

import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
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
	public void register( JsonObject json )
	{
		// TODO only primary for now

		JsonObject result = json.get( "result" ).getAsJsonObject();
		ItemStack primary = PartRecipeFactory.getResult( result, ctx, "primary" );
		ItemStack[] input = CraftingHelper.getIngredient( json.get( "input" ), ctx ).getMatchingStacks();

		int turns = 5;
		if( json.has( "turns" ) )
		{
			turns = json.get( "turns" ).getAsInt();
		}

		final IGrinderRegistry reg = AEApi.instance().registries().grinder();
		for( ItemStack itemStack : input )
		{
			final IGrinderRecipeBuilder builder = reg.builder();

			builder.withOutput( primary );
			builder.withInput( itemStack );
			builder.withTurns( turns );

			reg.addRecipe( builder.build() );
		}
	}
}
