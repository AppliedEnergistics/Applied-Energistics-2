
package appeng.integration.modules.crafttweaker;


import net.minecraft.item.ItemStack;

import crafttweaker.IAction;
import crafttweaker.api.item.IItemStack;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import appeng.api.AEApi;
import appeng.api.features.IGrinderRecipe;
import appeng.api.features.IGrinderRecipeBuilder;


@ZenClass( "mods.appliedenergistics2.Grinder" )
public class GrinderRecipes
{
	@ZenMethod
	public static void addRecipe( IItemStack output, IItemStack input, int turns, @Optional IItemStack secondary1Output, @Optional Float secondary1Chance, @Optional IItemStack secondary2Output, @Optional Float secondary2Chance )
	{

		IGrinderRecipeBuilder builder = AEApi.instance().registries().grinder().builder();
		builder.withInput( CTModule.toStack( input ) )
				.withOutput( CTModule.toStack( output ) )
				.withTurns( turns );

		final ItemStack s1 = CTModule.toStack( secondary1Output );
		if( !s1.isEmpty() )
		{
			builder.withFirstOptional( s1, secondary1Chance == null ? 1.0f : secondary1Chance );
		}
		final ItemStack s2 = CTModule.toStack( secondary2Output );
		if( !s2.isEmpty() )
		{
			builder.withFirstOptional( s2, secondary2Chance == null ? 1.0f : secondary2Chance );
		}

		CTModule.ADDITIONS.add( new Add( builder.build() ) );
	}

	@ZenMethod
	public static void removeRecipe( IItemStack input )
	{
		CTModule.REMOVALS.add( new Remove( (ItemStack) input.getInternal() ) );
	}

	private static class Add implements IAction
	{
		private final IGrinderRecipe entry;

		private Add( IGrinderRecipe entry )
		{
			this.entry = entry;
		}

		@Override
		public void apply()
		{
			AEApi.instance().registries().grinder().addRecipe( entry );
		}

		@Override
		public String describe()
		{
			return "Adding Grinder Entry for " + entry.getInput().getDisplayName();
		}
	}

	private static class Remove implements IAction
	{
		private final ItemStack stack;

		private Remove( ItemStack stack )
		{
			this.stack = stack;
		}

		@Override
		public void apply()
		{
			IGrinderRecipe recipe = AEApi.instance().registries().grinder().getRecipeForInput( stack );
			if( recipe != null )
			{
				AEApi.instance().registries().grinder().removeRecipe( recipe );
			}
		}

		@Override
		public String describe()
		{
			return "Removing Grinder Entry for " + stack.getDisplayName();
		}
	}
}
