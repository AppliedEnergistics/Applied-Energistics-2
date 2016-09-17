/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

package appeng.api.features;


import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;


/**
 * Registration Records for {@link IInscriberRegistry}
 *
 * You have to pay attention though, that recipes are not mirrored,
 * where the top and bottom slots are switching places.
 *
 * This is applied on runtime.
 *
 * @author thatsIch
 * @version rv2
 * @since rv2
 */
public interface IInscriberRecipe
{
	/**
	 * the current inputs
	 *
	 * @return inputs the inscriber will accept
	 */
	@Nonnull
	List<ItemStack> getInputs();

	/**
	 * gets the current output
	 *
	 * @return output that the recipe will produce
	 */
	@Nonnull
	ItemStack getOutput();

	/**
	 * gets the top optional
	 *
	 * @return item which is used top
	 */
	@Nonnull
	Optional<ItemStack> getTopOptional();

	/**
	 * gets the bottom optional
	 *
	 * @return item which is used bottom
	 */
	@Nonnull
	Optional<ItemStack> getBottomOptional();

	/**
	 * type of inscriber process
	 *
	 * @return type of process the inscriber is doing
	 */
	@Nonnull
	InscriberProcessType getProcessType();
}
