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

package appeng.crafting;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.me.cache.CraftingGridCache;

public class CraftingLinkNexus
{

	public CraftingLinkNexus(String craftID) {
		this.CraftID = craftID;
	}

	public final String CraftID;

	boolean canceled = false;
	boolean done = false;

	int tickOfDeath = 0;

	CraftingLink req;
	CraftingLink cpu;

	public boolean isDead(IGrid g, CraftingGridCache craftingGridCache)
	{
		if ( isCanceled() || isDone() )
			return true;

		if ( req == null || cpu == null )
			tickOfDeath++;
		else
		{
			boolean hasCpu = craftingGridCache.hasCpu( cpu.cpu );
			boolean hasMachine = req.req.getActionableNode().getGrid() == g;

			if ( hasCpu && hasMachine )
				tickOfDeath = 0;
			else
				tickOfDeath += 60;
		}

		if ( tickOfDeath > 60 )
		{
			cancel();
			return true;
		}

		return false;
	}

	public void remove(CraftingLink craftingLink)
	{
		if ( req == craftingLink )
			req = null;
		else if ( cpu == craftingLink )
			cpu = null;
	}

	public void add(CraftingLink craftingLink)
	{
		if ( craftingLink.cpu != null )
			cpu = craftingLink;
		else if ( craftingLink.req != null )
			req = craftingLink;
	}

	public boolean isCanceled()
	{
		return canceled;
	}

	public boolean isDone()
	{
		return done;
	}

	public void markDone()
	{
		done = true;

		if ( req != null )
		{
			req.done = true;
			if ( req.req != null )
				req.req.jobStateChange( req );
		}

		if ( cpu != null )
			cpu.done = true;
	}

	public void cancel()
	{
		canceled = true;

		if ( req != null )
		{
			req.canceled = true;
			if ( req.req != null )
				req.req.jobStateChange( req );
		}

		if ( cpu != null )
			cpu.canceled = true;
	}

	public boolean isMachine(IGridHost machine)
	{
		return req == machine;
	}

	public void removeNode()
	{
		if ( req != null )
			req.setNexus( null );

		req = null;
		tickOfDeath = 0;
	}

}
