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

package appeng.parts.layers;

import javax.annotation.Nullable;

import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.common.event.FMLInitializationEvent;

import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.SidedEnvironment;

import appeng.api.parts.IPart;
import appeng.api.parts.LayerBase;
import appeng.core.Registration;
import appeng.helpers.Reflected;


/**
 * Reflected in {@link Registration#initialize(FMLInitializationEvent)}
 */
@Reflected
public class LayerSidedEnvironment extends LayerBase implements SidedEnvironment
{
	@Nullable
	@Override
	public final Node sidedNode( ForgeDirection side )
	{
		final IPart part = this.getPart( side );
		if ( part instanceof SidedEnvironment )
		{
			return ( (SidedEnvironment) part ).sidedNode( side );
		}
		return null;
	}

	@Override
	public final boolean canConnect( ForgeDirection side )
	{
		final IPart part = this.getPart( side );
		if ( part instanceof SidedEnvironment )
		{
			return ( (SidedEnvironment) part ).canConnect( side );
		}
		return false;
	}
}
