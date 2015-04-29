package appeng.core.features.registries;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import appeng.api.features.IInscriberRecipe;
import appeng.api.features.IInscriberRecipeBuilder;
import appeng.api.features.IInscriberRegistry;
import appeng.api.features.InscriberProcessType;
import appeng.core.features.registries.entries.InscriberRecipe;


/**
 * @author thatsIch
 * @version rv2
 * @since rv2
 */
public final class InscriberRegistry implements IInscriberRegistry
{
	private final List<IInscriberRecipe> recipes;
	private final Set<ItemStack> optionals;
	private final Set<ItemStack> inputs;

	public InscriberRegistry()
	{
		this.inputs = new HashSet<ItemStack>();
		this.optionals = new HashSet<ItemStack>();
		this.recipes = new ArrayList<IInscriberRecipe>();
	}

	@Nonnull
	@Override
	public List<IInscriberRecipe> getRecipes()
	{
		return this.recipes;
	}

	@Nonnull
	@Override
	public Set<ItemStack> getOptionals()
	{
		return this.optionals;
	}

	@Nonnull
	@Override
	public Set<ItemStack> getInputs()
	{
		return this.inputs;
	}

	@Nonnull
	@Override
	public IInscriberRecipeBuilder builder()
	{
		return new Builder();
	}

	@Override
	public void addRecipe( IInscriberRecipe recipe )
	{
		if( recipe == null )
		{
			throw new IllegalArgumentException( "Tried to add an invalid (null) inscriber recipe to the registry." );
		}

		this.recipes.add( recipe );

		this.optionals.addAll( recipe.getTopOptional().asSet() );
		this.optionals.addAll( recipe.getBottomOptional().asSet() );

		this.inputs.addAll( recipe.getInputs() );
	}

	@Override
	public void removeRecipe( IInscriberRecipe toBeRemovedRecipe )
	{
		for( final Iterator<IInscriberRecipe> iterator = this.recipes.iterator(); iterator.hasNext(); )
		{
			final IInscriberRecipe recipe = iterator.next();
			if( recipe.equals( toBeRemovedRecipe ) )
			{
				iterator.remove();
			}
		}
	}

	/**
	 * Internal {@link IInscriberRecipeBuilder} implementation.
	 * Needs to be adapted to represent a correct {@link IInscriberRecipe}
	 */
	private static final class Builder implements IInscriberRecipeBuilder
	{
		private List<ItemStack> inputs;
		private ItemStack output;
		private ItemStack topOptional;
		private ItemStack bottomOptional;
		private InscriberProcessType type;

		@Nonnull
		@Override
		public Builder withInputs( @Nonnull Collection<ItemStack> inputs )
		{
			this.inputs = new ArrayList<ItemStack>( inputs.size() );
			this.inputs.addAll( inputs );

			return this;
		}

		@Nonnull
		@Override
		public Builder withOutput( @Nonnull ItemStack output )
		{
			this.output = output;

			return this;
		}

		@Nonnull
		@Override
		public Builder withTopOptional( @Nonnull ItemStack topOptional )
		{
			this.topOptional = topOptional;

			return this;
		}

		@Nonnull
		@Override
		public Builder withBottomOptional( @Nonnull ItemStack bottomOptional )
		{
			this.bottomOptional = bottomOptional;

			return this;
		}

		@Nonnull
		@Override
		public Builder withProcessType( @Nonnull InscriberProcessType type )
		{
			this.type = type;

			return this;
		}

		@Nonnull
		@Override
		public IInscriberRecipe build()
		{
			if( this.inputs == null )
			{
				throw new IllegalStateException( "Input must be defined." );
			}
			if( this.inputs.size() == 0 )
			{
				throw new IllegalStateException( "Input must have a size." );
			}
			if( this.output == null )
			{
				throw new IllegalStateException( "Output must be defined." );
			}
			if ( this.topOptional == null && this.bottomOptional == null )
			{
				throw new IllegalStateException( "One optional must be defined." );
			}
			if ( this.type == null )
			{
				throw new IllegalStateException( "Process type must be defined." );
			}

			return new InscriberRecipe( this.inputs, this.output, this.topOptional, this.bottomOptional, this.type );
		}
	}
}
