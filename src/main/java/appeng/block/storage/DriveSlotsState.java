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

package appeng.block.storage;


import appeng.api.implementations.tiles.IChestOrDrive;


/**
 * Contains the full information about what the state of the slots in a BlockDrive is.
 */
public class DriveSlotsState
{

	private final DriveSlotState[] slots;

	private DriveSlotsState( DriveSlotState[] slots )
	{
		this.slots = slots;
	}

	public DriveSlotState getState( int index )
	{
		if( index >= slots.length )
		{
			return DriveSlotState.EMPTY;
		}
		return slots[index];
	}

	public int getSlotCount()
	{
		return slots.length;
	}

	/**
	 * Retrieve an array that describes the state of each slot in this drive or chest.
	 */
	public static DriveSlotsState fromChestOrDrive( IChestOrDrive chestOrDrive )
	{
		DriveSlotState[] slots = new DriveSlotState[chestOrDrive.getCellCount()];
		for( int i = 0; i < chestOrDrive.getCellCount(); i++ )
		{
			if( !chestOrDrive.isPowered() )
			{
				if( chestOrDrive.getCellStatus( i ) != 0 )
				{
					slots[i] = DriveSlotState.OFFLINE;
				}
				else
				{
					slots[i] = DriveSlotState.EMPTY;
				}
			}
			else
			{
				switch( chestOrDrive.getCellStatus( i ) )
				{
					default:
					case 0:
						slots[i] = DriveSlotState.EMPTY;
						break;
					case 1:
						slots[i] = DriveSlotState.ONLINE;
						break;
					case 2:
						slots[i] = DriveSlotState.TYPES_FULL;
						break;
					case 3:
						slots[i] = DriveSlotState.FULL;
						break;
				}
			}
		}
		return new DriveSlotsState( slots );
	}
}
