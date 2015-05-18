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

package appeng.core.features.registries.entries;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Optional;

import net.minecraft.item.ItemStack;

import appeng.api.features.IInscriberRecipe;
import appeng.api.features.InscriberProcessType;


/**
 * Basic inscriber recipe
 *
 * @author thatsIch
 * @version rv2
 * @since rv2
 */
public class InscriberRecipe implements IInscriberRecipe
{
	@Nonnull
	private final List<ItemStack> inputs;

	@Nonnull
	private final ItemStack output;

	@Nonnull
	private final Optional<ItemStack> maybeTop;

	@Nonnull
	private final Optional<ItemStack> maybeBot;

	@Nonnull
	private final InscriberProcessType type;

	public InscriberRecipe( @Nonnull Collection<ItemStack> inputs, @Nonnull ItemStack output, @Nullable ItemStack top, @Nullable ItemStack bot, @Nonnull InscriberProcessType type )
	{
		this.inputs = new ArrayList<ItemStack>( inputs.size() );
		this.inputs.addAll( inputs );

		this.output = output;
		this.maybeTop = Optional.fromNullable( top );
		this.maybeBot = Optional.fromNullable( bot );

		this.type = type;
	}

	@Nonnull
	@Override
	public final List<ItemStack> getInputs()
	{
		return this.inputs;
	}

	@Nonnull
	@Override
	public final ItemStack getOutput()
	{
		return this.output;
	}

	@Nonnull
	@Override
	public final Optional<ItemStack> getTopOptional()
	{
		return this.maybeTop;
	}

	@Nonnull
	@Override
	public final Optional<ItemStack> getBottomOptional()
	{
		return this.maybeBot;
	}

	@Nonnull
	@Override
	public final InscriberProcessType getProcessType()
	{
		return this.type;
	}

	@Override
	public final boolean equals( Object o )
	{
		if( this == o )
		{
			return true;
		}
		if( !(o instanceof IInscriberRecipe) )
		{
			return false;
		}

		IInscriberRecipe that = (IInscriberRecipe) o;

		if( !this.inputs.equals( that.getInputs() ) )
		{
			return false;
		}
		if( !this.output.equals( that.getOutput() ) )
		{
			return false;
		}
		if( !this.maybeTop.equals( that.getTopOptional() ) )
		{
			return false;
		}
		if( !this.maybeBot.equals( that.getBottomOptional()) )
		{
			return false;
		}
		return this.type == that.getProcessType();
	}

	@Override
	public final int hashCode()
	{
		int result = this.inputs.hashCode();
		result = 31 * result + this.output.hashCode();
		result = 31 * result + this.maybeTop.hashCode();
		result = 31 * result + this.maybeBot.hashCode();
		result = 31 * result + this.type.hashCode();
		return result;
	}
}
