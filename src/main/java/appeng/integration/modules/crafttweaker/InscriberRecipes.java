
package appeng.integration.modules.crafttweaker;


import java.util.Collections;

import net.minecraft.item.ItemStack;

import crafttweaker.IAction;
import crafttweaker.api.item.IItemStack;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import appeng.api.AEApi;
import appeng.api.features.IInscriberRecipe;
import appeng.api.features.IInscriberRecipeBuilder;
import appeng.api.features.InscriberProcessType;


@ZenClass( "mods.appliedenergistics2.Inscriber" )
public class InscriberRecipes
{
	@ZenMethod
	public static void addRecipe( IItemStack output, IItemStack input, boolean inscribe, @Optional IItemStack top, @Optional IItemStack bottom )
	{

		IInscriberRecipeBuilder builder = AEApi.instance().registries().inscriber().builder();
		builder.withProcessType( inscribe ? InscriberProcessType.INSCRIBE : InscriberProcessType.PRESS )
				.withOutput( CTModule.toStack( output ) )
				.withInputs( Collections.singleton( CTModule.toStack( input ) ) );

		final ItemStack s1 = CTModule.toStack( top );
		if( !s1.isEmpty() )
		{
			builder.withTopOptional( s1 );
		}
		final ItemStack s2 = CTModule.toStack( bottom );
		if( !s2.isEmpty() )
		{
			builder.withBottomOptional( s2 );
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
		private final IInscriberRecipe entry;

		private Add( IInscriberRecipe entry )
		{
			this.entry = entry;
		}

		@Override
		public void apply()
		{
			AEApi.instance().registries().inscriber().addRecipe( entry );
		}

		@Override
		public String describe()
		{
			return "Adding Inscriber Entry for " + entry.getOutput().getDisplayName();
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
			IInscriberRecipe recipe = AEApi.instance()
					.registries()
					.inscriber()
					.getRecipes()
					.stream()
					.filter( r -> r.getOutput().isItemEqual( this.stack ) )
					.findFirst()
					.orElse( null );
			if( recipe != null )
			{
				AEApi.instance().registries().inscriber().removeRecipe( recipe );
			}
		}

		@Override
		public String describe()
		{
			return "Removing Inscriber Entry for " + stack.getDisplayName();
		}
	}

}
