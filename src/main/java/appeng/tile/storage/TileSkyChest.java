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


import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import appeng.tile.AEBaseInvTile;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;


public class TileSkyChest extends AEBaseInvTile
{

	final int[] sides = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35 };
	final AppEngInternalInventory inv = new AppEngInternalInventory( this, 9 * 4 );
	// server
	public int playerOpen;
	// client..
	public long lastEvent;
	public float lidAngle;

	@TileEvent( TileEventType.NETWORK_WRITE )
	public void writeToStream_TileSkyChest( ByteBuf data )
	{
		data.writeBoolean( this.playerOpen > 0 );
	}

	@TileEvent( TileEventType.NETWORK_READ )
	public boolean readFromStream_TileSkyChest( ByteBuf data )
	{
		int wasOpen = this.playerOpen;
		this.playerOpen = data.readBoolean() ? 1 : 0;

		if( wasOpen != this.playerOpen )
		{
			this.lastEvent = System.currentTimeMillis();
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
	public void openInventory( EntityPlayer player )
	{
		if( Platform.isClient() )
		{
			return;
		}

		this.playerOpen++;

		if( this.playerOpen == 1 )
		{
			this.getWorld().playSoundEffect( pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, "random.chestopen", 0.5F, this.getWorld().rand.nextFloat() * 0.1F + 0.9F );
			this.markForUpdate();
		}
	}

	@Override
	public void closeInventory( EntityPlayer player )
	{
		if( Platform.isClient() )
		{
			return;
		}

		this.playerOpen--;

		if( this.playerOpen < 0 )
		{
			this.playerOpen = 0;
		}

		if( this.playerOpen == 0 )
		{
			this.getWorld().playSoundEffect( pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, "random.chestclosed", 0.5F, this.getWorld().rand.nextFloat() * 0.1F + 0.9F );
			this.markForUpdate();
		}
	}

	@Override
	public void onChangeInventory( IInventory inv, int slot, InvOperation mc, ItemStack removed, ItemStack added )
	{

	}

	@Override
	public int[] getAccessibleSlotsBySide( EnumFacing side )
	{
		return this.sides;
	}
}
