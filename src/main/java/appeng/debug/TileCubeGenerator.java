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

package appeng.debug;


import appeng.core.CommonHelper;
import appeng.tile.AEBaseTile;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.util.Platform;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.util.ForgeDirection;


public class TileCubeGenerator extends AEBaseTile
{

	private int size = 3;
	private ItemStack is = null;
	private int countdown = 20 * 10;
	private EntityPlayer who = null;

	@TileEvent( TileEventType.TICK )
	public void onTickEvent()
	{
		if( this.is != null && Platform.isServer() )
		{
			this.countdown--;

			if( this.countdown % 20 == 0 )
			{
				for( final EntityPlayer e : CommonHelper.proxy.getPlayers() )
				{
					e.addChatMessage( new ChatComponentText( "Spawning in... " + ( this.countdown / 20 ) ) );
				}
			}

			if( this.countdown <= 0 )
			{
				this.spawn();
			}
		}
	}

	private void spawn()
	{
		this.worldObj.setBlock( this.xCoord, this.yCoord, this.zCoord, Platform.AIR_BLOCK, 0, 3 );

		final Item i = this.is.getItem();
		final int side = ForgeDirection.UP.ordinal();

		final int half = (int) Math.floor( this.size / 2 );

		for( int y = 0; y < this.size; y++ )
		{
			for( int x = -half; x < half; x++ )
			{
				for( int z = -half; z < half; z++ )
				{
					i.onItemUse( this.is.copy(), this.who, this.worldObj, x + this.xCoord, y + this.yCoord - 1, z + this.zCoord, side, 0.5f, 0.0f, 0.5f );
				}
			}
		}
	}

	void click( final EntityPlayer player )
	{
		if( Platform.isServer() )
		{
			final ItemStack hand = player.inventory.getCurrentItem();
			this.who = player;

			if( hand == null )
			{
				this.is = null;

				if( player.isSneaking() )
				{
					this.size--;
				}
				else
				{
					this.size++;
				}

				if( this.size < 3 )
				{
					this.size = 3;
				}
				if( this.size > 64 )
				{
					this.size = 64;
				}

				player.addChatMessage( new ChatComponentText( "Size: " + this.size ) );
			}
			else
			{
				this.countdown = 20 * 10;
				this.is = hand;
			}
		}
	}
}
