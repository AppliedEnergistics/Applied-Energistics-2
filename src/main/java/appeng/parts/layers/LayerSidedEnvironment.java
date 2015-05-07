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

package appeng.parts.layers;

import net.minecraftforge.common.util.ForgeDirection;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.SidedEnvironment;

import appeng.api.parts.IPart;
import appeng.api.parts.LayerBase;

public class LayerSidedEnvironment extends LayerBase implements SidedEnvironment
{
	@Override
	public Node sidedNode(ForgeDirection side)
	{
		final IPart part = getPart( side );
		if ( part instanceof SidedEnvironment )
			return ((SidedEnvironment) part).sidedNode(side);
		return null;
	}

	@Override
	public boolean canConnect(ForgeDirection side)
	{
		final IPart part = getPart( side );
		if ( part instanceof SidedEnvironment )
			return ((SidedEnvironment) part).canConnect(side);
		return false;
	}
}
