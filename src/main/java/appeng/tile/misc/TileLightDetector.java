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

package appeng.tile.misc;


import net.minecraft.server.gui.IUpdatePlayerListBox;
import appeng.tile.AEBaseTile;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.util.Platform;


public class TileLightDetector extends AEBaseTile implements IUpdatePlayerListBox
{

	int lastCheck = 30;
	int lastLight = 0;

	public boolean isReady()
	{
		return this.lastLight > 0;
	}

	@TileEvent( TileEventType.TICK )
	public void Tick_TileLightDetector()
	{
		this.lastCheck++;
		if( this.lastCheck > 30 )
		{
			this.lastCheck = 0;
			this.updateLight();
		}
	}

	public void updateLight()
	{
		final int val = this.worldObj.getLight( pos );

		if( this.lastLight != val )
		{
			this.lastLight = val;
			Platform.notifyBlocksOfNeighbors( this.worldObj, pos );
		}
	}

	@Override
	public boolean canBeRotated()
	{
		return false;
	}
}
