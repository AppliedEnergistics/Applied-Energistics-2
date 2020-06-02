package appeng.forge.data.providers.recipes;


import appeng.core.AppEng;
import appeng.forge.data.providers.IAE2DataProvider;
import com.google.gson.JsonObject;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;


public class SpecialRecipes extends RecipeProvider implements IAE2DataProvider
{

	public SpecialRecipes( DataGenerator generatorIn )
	{
		super( generatorIn );
	}

	@Override
	protected void registerRecipes( @Nonnull Consumer<IFinishedRecipe> consumer )
	{
//		ConditionalRecipe.builder() FIXME this works, but NPEs at runtime
//				.addCondition( new FeaturesEnabled( AEFeature.ENABLE_DISASSEMBLY_CRAFTING ) )
//				.addRecipe( new FinishedRecipe( DisassembleRecipe.SERIALIZER ) )
//				.build( consumer, AppEng.MOD_ID, "special/disassemble" );

//		ConditionalRecipe.builder() FIXME re-implement facades
//				.addCondition( new FeaturesEnabled( AEFeature.ENABLE_FACADE_CRAFTING ) )
//				.addRecipe( new FinishedRecipe( FacadeRecipe.SERIALIZER ) )
//				.build( consumer, AppEng.MOD_ID, "special/facade" );
	}

	private static class FinishedRecipe implements IFinishedRecipe
	{

		private final IRecipeSerializer<?> serializer;

		public FinishedRecipe( IRecipeSerializer<?> serializer )
		{
			this.serializer = serializer;
		}

		@Override public void serialize( @Nonnull JsonObject json )
		{
		}

		@Nonnull
		@Override
		public ResourceLocation getID()
		{
			return Objects.requireNonNull( serializer.getRegistryName() );
		}

		@Nonnull
		@Override
		public IRecipeSerializer<?> getSerializer()
		{
			return serializer;
		}

		@Nullable
		@Override
		public JsonObject getAdvancementJson()
		{
			return null;
		}

		@Nullable
		@Override
		public ResourceLocation getAdvancementID()
		{
			return null;
		}

	}

	@Nonnull
	@Override
	public String getName()
	{
		return AppEng.MOD_NAME + " Special Crafting Recipes";
	}

}
