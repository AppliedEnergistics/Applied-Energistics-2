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

package appeng.tile.storage;


import appeng.tile.AEBaseInvTile;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;
import io.netty.buffer.ByteBuf;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;


public class TileSkyChest extends AEBaseInvTile
{

	private final int[] sides = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35 };
	private final AppEngInternalInventory inv = new AppEngInternalInventory( this, 9 * 4 );
	// server
	private int playerOpen;
	// client..
	private long lastEvent;
	private float lidAngle;

	@TileEvent( TileEventType.NETWORK_WRITE )
	public void writeToStream_TileSkyChest( final ByteBuf data )
	{
		data.writeBoolean( this.getPlayerOpen() > 0 );
	}

	@TileEvent( TileEventType.NETWORK_READ )
	public boolean readFromStream_TileSkyChest( final ByteBuf data )
	{
		final int wasOpen = this.getPlayerOpen();
		this.setPlayerOpen( data.readBoolean() ? 1 : 0 );

		if( wasOpen != this.getPlayerOpen() )
		{
			this.setLastEvent( System.currentTimeMillis() );
		}

		return false; // TESR yo!
	}

	@Override
	public boolean requiresTESR()
	{
		return true;
	}

	@Override
	public IInventory getInternalInventory()
	{
		return this.inv;
	}

	@Override
	public void openInventory()
	{
		if( Platform.isClient() )
		{
			return;
		}

		this.setPlayerOpen( this.getPlayerOpen() + 1 );

		if( this.getPlayerOpen() == 1 )
		{
			this.getWorldObj().playSoundEffect( this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D, "random.chestopen", 0.5F, this.getWorldObj().rand.nextFloat() * 0.1F + 0.9F );
			this.markForUpdate();
		}
	}

	@Override
	public void closeInventory()
	{
		if( Platform.isClient() )
		{
			return;
		}

		this.setPlayerOpen( this.getPlayerOpen() - 1 );

		if( this.getPlayerOpen() < 0 )
		{
			this.setPlayerOpen( 0 );
		}

		if( this.getPlayerOpen() == 0 )
		{
			this.getWorldObj().playSoundEffect( this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D, "random.chestclosed", 0.5F, this.getWorldObj().rand.nextFloat() * 0.1F + 0.9F );
			this.markForUpdate();
		}
	}

	@Override
	public void onChangeInventory( final IInventory inv, final int slot, final InvOperation mc, final ItemStack removed, final ItemStack added )
	{

	}

	@Override
	public int[] getAccessibleSlotsBySide( final ForgeDirection side )
	{
		return this.sides;
	}

	public float getLidAngle()
	{
		return this.lidAngle;
	}

	public void setLidAngle( final float lidAngle )
	{
		this.lidAngle = lidAngle;
	}

	public int getPlayerOpen()
	{
		return this.playerOpen;
	}

	private void setPlayerOpen( final int playerOpen )
	{
		this.playerOpen = playerOpen;
	}

	public long getLastEvent()
	{
		return this.lastEvent;
	}

	private void setLastEvent( final long lastEvent )
	{
		this.lastEvent = lastEvent;
	}
}
