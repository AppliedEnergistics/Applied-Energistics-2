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
import net.minecraft.inventory.Slot;
import net.minecraft.world.World;

import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.PlayerSource;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotInaccessible;
import appeng.tile.inventory.AppEngInternalInventory;


public class ContainerCraftAmount extends AEBaseContainer
{

	public final Slot craftingItem;
	final ITerminalHost priHost;
	public IAEItemStack whatToMake;

	public ContainerCraftAmount( InventoryPlayer ip, ITerminalHost te )
	{
		super( ip, te );
		this.priHost = te;

		this.craftingItem = new SlotInaccessible( new AppEngInternalInventory( null, 1 ), 0, 34, 53 );
		this.addSlotToContainer( this.craftingItem );
	}

	@Override
	public void detectAndSendChanges()
	{
		super.detectAndSendChanges();
		this.verifyPermissions( SecurityPermissions.CRAFT, false );
	}

	public IGrid getGrid()
	{
		IActionHost h = ( (IActionHost) this.getTarget() );
		return h.getActionableNode().getGrid();
	}

	public World getWorld()
	{
		return this.getPlayerInv().player.worldObj;
	}

	public BaseActionSource getActionSrc()
	{
		return new PlayerSource( this.getPlayerInv().player, (IActionHost) this.getTarget() );
	}
}
