/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.core.features.registries;


import appeng.api.features.IInscriberRecipe;
import appeng.api.features.IInscriberRecipeBuilder;
import appeng.api.features.IInscriberRegistry;
import appeng.api.features.InscriberProcessType;
import appeng.core.features.registries.entries.InscriberRecipe;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.*;


/**
 * @author thatsIch
 * @version rv3
 * @since rv2
 */
public final class InscriberRegistry implements IInscriberRegistry
{
	private final Set<IInscriberRecipe> recipes;
	private final Set<ItemStack> optionals;
	private final Set<ItemStack> inputs;

	public InscriberRegistry()
	{
		this.inputs = new HashSet<ItemStack>();
		this.optionals = new HashSet<ItemStack>();
		this.recipes = new HashSet<IInscriberRecipe>();
	}

	@Nonnull
	@Override
	public Collection<IInscriberRecipe> getRecipes()
	{
		return Collections.unmodifiableCollection( this.recipes );
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
	public void addRecipe( final IInscriberRecipe recipe )
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
	public void removeRecipe( final IInscriberRecipe toBeRemovedRecipe )
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
		public Builder withInputs( @Nonnull final Collection<ItemStack> inputs )
		{
			this.inputs = new ArrayList<ItemStack>( inputs.size() );
			this.inputs.addAll( inputs );

			return this;
		}

		@Nonnull
		@Override
		public Builder withOutput( @Nonnull final ItemStack output )
		{
			this.output = output;

			return this;
		}

		@Nonnull
		@Override
		public Builder withTopOptional( @Nonnull final ItemStack topOptional )
		{
			this.topOptional = topOptional;

			return this;
		}

		@Nonnull
		@Override
		public Builder withBottomOptional( @Nonnull final ItemStack bottomOptional )
		{
			this.bottomOptional = bottomOptional;

			return this;
		}

		@Nonnull
		@Override
		public Builder withProcessType( @Nonnull final InscriberProcessType type )
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
			if( this.inputs.isEmpty() )
			{
				throw new IllegalStateException( "Input must have a size." );
			}
			if( this.output == null )
			{
				throw new IllegalStateException( "Output must be defined." );
			}
			if( this.topOptional == null && this.bottomOptional == null )
			{
				throw new IllegalStateException( "One optional must be defined." );
			}
			if( this.type == null )
			{
				throw new IllegalStateException( "Process type must be defined." );
			}

			return new InscriberRecipe( this.inputs, this.output, this.topOptional, this.bottomOptional, this.type );
		}
	}
}
