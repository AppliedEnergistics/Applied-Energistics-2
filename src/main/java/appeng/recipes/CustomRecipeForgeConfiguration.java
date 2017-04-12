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

package appeng.recipes;


import com.google.common.base.Preconditions;
import net.minecraftforge.common.config.Configuration;

import javax.annotation.Nonnull;


/**
 * @author thatsIch
 * @version rv3 - 23.08.2015
 * @since rv3 23.08.2015
 */
public class CustomRecipeForgeConfiguration implements CustomRecipeConfig
{
	private final boolean isEnabled;

	public CustomRecipeForgeConfiguration( @Nonnull final Configuration config )
	{
		Preconditions.checkNotNull( config );

		this.isEnabled = config.getBoolean( "enabled", "general", true, "If true, the custom recipes are enabled. Acts as a master switch." );
	}

	@Override
	public final boolean isEnabled()
	{
		return this.isEnabled;
	}
}
