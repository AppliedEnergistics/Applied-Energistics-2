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

package appeng.me.cache.helpers;

import net.minecraft.crash.CrashReportCategory;

import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.util.DimensionalCoord;
import appeng.me.cache.TickManagerCache;
import appeng.parts.AEBasePart;

public class TickTracker implements Comparable<TickTracker>
{

	public final TickingRequest request;
	public final IGridTickable gt;
	public final IGridNode node;
	public final TickManagerCache host;

	public final long LastFiveTicksTime = 0;

	public long lastTick;
	public int current_rate;

	public TickTracker(TickingRequest req, IGridNode node, IGridTickable gt, long currentTick, TickManagerCache tickManagerCache) {
		this.request = req;
		this.gt = gt;
		this.node = node;
		this.current_rate = (req.minTickRate + req.maxTickRate) / 2;
		this.lastTick = currentTick;
		this.host = tickManagerCache;
	}

	public long getAvgNanos()
	{
		return (this.LastFiveTicksTime / 5);
	}

	public void setRate(int rate)
	{
		this.current_rate = rate;

		if ( this.current_rate < this.request.minTickRate )
			this.current_rate = this.request.minTickRate;

		if ( this.current_rate > this.request.maxTickRate )
			this.current_rate = this.request.maxTickRate;
	}

	@Override
	public int compareTo(TickTracker t)
	{
		int nextTick = (int) ((this.lastTick - this.host.getCurrentTick()) + this.current_rate);
		int ts_nextTick = (int) ((t.lastTick - this.host.getCurrentTick()) + t.current_rate);
		return nextTick - ts_nextTick;
	}

	public void addEntityCrashInfo(CrashReportCategory crashreportcategory)
	{
		if ( this.gt instanceof AEBasePart )
		{
			AEBasePart part = (AEBasePart)this.gt;
			part.addEntityCrashInfo( crashreportcategory );
		}
		
		crashreportcategory.addCrashSection( "CurrentTickRate", this.current_rate );
		crashreportcategory.addCrashSection( "MinTickRate", this.request.minTickRate );
		crashreportcategory.addCrashSection( "MaxTickRate", this.request.maxTickRate );
		crashreportcategory.addCrashSection( "MachineType", this.gt.getClass().getName() );
		crashreportcategory.addCrashSection( "GridBlockType", this.node.getGridBlock().getClass().getName() );
		crashreportcategory.addCrashSection( "ConnectedSides", this.node.getConnectedSides() );
		
		DimensionalCoord dc = this.node.getGridBlock().getLocation();
		if ( dc != null )
			crashreportcategory.addCrashSection( "Location", dc );
	}
}
