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

package appeng.util.inv;


import appeng.integration.IntegrationRegistry;
import appeng.integration.IntegrationType;
import appeng.integration.abstraction.IBuildCraftTransport;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;


public class WrapperBCPipe implements IInventory
{

	private final IBuildCraftTransport bc;
	private final TileEntity ad;
	private final ForgeDirection dir;

	public WrapperBCPipe( final TileEntity te, final ForgeDirection d )
	{
		this.bc = (IBuildCraftTransport) IntegrationRegistry.INSTANCE.getInstance( IntegrationType.BuildCraftTransport );
		this.ad = te;
		this.dir = d;
	}

	@Override
	public int getSizeInventory()
	{
		return 1;
	}

	@Override
	public ItemStack getStackInSlot( final int i )
	{
		return null;
	}

	@Override
	public ItemStack decrStackSize( final int i, final int j )
	{
		return null;
	}

	@Override
	public ItemStack getStackInSlotOnClosing( final int i )
	{
		return null;
	}

	@Override
	public void setInventorySlotContents( final int i, final ItemStack itemstack )
	{
		if( IntegrationRegistry.INSTANCE.isEnabled( IntegrationType.BuildCraftTransport ) )
		{
			this.bc.addItemsToPipe( this.ad, itemstack, this.dir );
		}
	}

	@Override
	public String getInventoryName()
	{
		return "BC Pipe Wrapper";
	}

	@Override
	public boolean hasCustomInventoryName()
	{
		return false;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public void markDirty()
	{

	}

	@Override
	public boolean isUseableByPlayer( final EntityPlayer entityplayer )
	{
		return false;
	}

	@Override
	public void openInventory()
	{

	}

	@Override
	public void closeInventory()
	{

	}

	@Override
	public boolean isItemValidForSlot( final int i, final ItemStack itemstack )
	{
		return this.bc.canAddItemsToPipe( this.ad, itemstack, this.dir );
	}
}
