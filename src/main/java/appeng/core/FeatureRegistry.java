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

package appeng.core;


import java.util.HashSet;
import java.util.Set;

import appeng.core.features.IAEFeature;


public final class FeatureRegistry
{
	private final Set<IAEFeature> registry = new HashSet<IAEFeature>();

	public void addFeature( IAEFeature feature )
	{
		this.registry.add( feature );
	}

	public Set<IAEFeature> getRegisteredFeatures()
	{
		return this.registry;
	}
}
