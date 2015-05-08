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

package appeng.recipes;


import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import appeng.api.recipes.ICraftHandler;


public class RecipeData
{

	public final Map<String, String> aliases = new HashMap<String, String>();
	public final Map<String, GroupIngredient> groups = new HashMap<String, GroupIngredient>();

	public final Collection<ICraftHandler> Handlers = new LinkedList<ICraftHandler>();
	public final Collection<String> knownItem = new HashSet<String>();
	public boolean crash = true;
	public boolean exceptions = true;
	public boolean errorOnMissing = true;
}
