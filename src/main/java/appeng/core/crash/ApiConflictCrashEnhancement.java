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

package appeng.core.crash;


import javax.annotation.Nonnull;

import com.google.common.base.Function;
import com.google.common.base.Optional;


public class ApiConflictCrashEnhancement extends BaseCrashEnhancement
{

	private static final Function<String, String> TRANSFORM_CONFLICTING_MOD = new Function<String, String>()
	{
		@Override
		public String apply( @Nonnull String modContainer )
		{
			return ", Conflict: " + modContainer;
		}
	};

	public ApiConflictCrashEnhancement( boolean state, Optional<String> apiConflict )
	{
		super( "AE2 API", "Conflict Check: " + state + apiConflict.transform( TRANSFORM_CONFLICTING_MOD ).or( "" ) );
	}

}
