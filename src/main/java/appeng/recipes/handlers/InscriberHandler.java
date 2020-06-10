
package appeng.recipes.handlers;


import com.google.gson.JsonObject;


public class InscriberHandler
{

//	@Override
//	public void register( JsonObject json, JsonContext ctx )
//	{
//		ItemStack result = PartRecipeFactory.getResult( json, ctx );
//		String mode = JSONUtils.getString( json, "mode" );
//
//		JsonObject ingredients = JSONUtils.getJsonObject( json, "ingredients" );
//
//		List<ItemStack> middle = Arrays.asList( CraftingHelper.getIngredient( ingredients.get( "middle" ), ctx ).getMatchingStacks() );
//		ItemStack[] top = new ItemStack[] { null };
//		if( ingredients.has( "top" ) )
//		{
//			top = CraftingHelper.getIngredient( JSONUtils.getJsonObject( ingredients, "top" ), ctx ).getMatchingStacks();
//		}
//
//		ItemStack[] bottom = new ItemStack[] { null };
//		if( ingredients.has( "bottom" ) )
//		{
//			bottom = CraftingHelper.getIngredient( JSONUtils.getJsonObject( ingredients, "bottom" ), ctx ).getMatchingStacks();
//		}
//
//		final IInscriberRegistry reg = AEApi.instance().registries().inscriber();
//		for( int i = 0; i < top.length; ++i )
//		{
//			for( int j = 0; j < bottom.length; ++j )
//			{
//				final IInscriberRecipeBuilder builder = reg.builder();
//				builder.withOutput( result );
//				builder.withProcessType( "press".equals( mode ) ? InscriberProcessType.PRESS : InscriberProcessType.INSCRIBE );
//				builder.withInputs( middle );
//
//				if( top[i] != null )
//				{
//					builder.withTopOptional( top[i] );
//				}
//				if( bottom[j] != null )
//				{
//					builder.withBottomOptional( bottom[j] );
//				}
//
//				reg.addRecipe( builder.build() );
//			}
//		}
//	}
//
}
