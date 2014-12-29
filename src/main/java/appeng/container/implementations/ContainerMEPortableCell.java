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

package appeng.container.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.implementations.guiobjects.IPortableCell;
import appeng.util.Platform;

public class ContainerMEPortableCell extends ContainerMEMonitorable
{

	double powerMultiplier = 0.5;
	final IPortableCell civ;

	public ContainerMEPortableCell(InventoryPlayer ip, IPortableCell monitorable) {
		super( ip, monitorable, false );
		this.lockPlayerInventorySlot( ip.currentItem );
		this.civ = monitorable;
		this.bindPlayerInventory( ip, 0, 0 );
	}

	int ticks = 0;

	@Override
	public void detectAndSendChanges()
	{
		ItemStack currentItem = this.getPlayerInv().getCurrentItem();

		if ( this.civ != null )
		{
			if ( currentItem != this.civ.getItemStack() )
			{
				if ( currentItem != null )
				{
					if ( Platform.isSameItem( this.civ.getItemStack(), currentItem ) )
						this.getPlayerInv().setInventorySlotContents( this.getPlayerInv().currentItem, this.civ.getItemStack() );
					else
						this.isContainerValid = false;
				}
				else
					this.isContainerValid = false;
			}
		}
		else
			this.isContainerValid = false;

		// drain 1 ae t
		this.ticks++;
		if ( this.ticks > 10 )
		{
			this.civ.extractAEPower( this.powerMultiplier * this.ticks, Actionable.MODULATE, PowerMultiplier.CONFIG );
			this.ticks = 0;
		}
		super.detectAndSendChanges();
	}
}
