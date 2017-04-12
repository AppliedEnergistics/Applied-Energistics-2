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


import appeng.api.recipes.ICraftHandler;

import java.util.*;


public class RecipeData
{

	final Map<String, String> aliases = new HashMap<String, String>();
	final Map<String, GroupIngredient> groups = new HashMap<String, GroupIngredient>();

	final List<ICraftHandler> handlers = new LinkedList<ICraftHandler>();
	final Set<String> knownItem = new HashSet<String>();
	boolean crash = true;
	boolean exceptions = true;
	boolean errorOnMissing = true;
}
