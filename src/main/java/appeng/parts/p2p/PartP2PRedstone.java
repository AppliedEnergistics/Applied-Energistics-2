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

package appeng.parts.p2p;


import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import appeng.api.networking.events.MENetworkBootingStatusChange;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.me.GridAccessException;
import appeng.util.Platform;


public class PartP2PRedstone extends PartP2PTunnel<PartP2PRedstone>
{

	int power;
	boolean recursive = false;

	public PartP2PRedstone( ItemStack is )
	{
		super( is );
	}

	@MENetworkEventSubscribe
	public void changeStateA( MENetworkBootingStatusChange bs )
	{
		this.setNetworkReady();
	}

	public void setNetworkReady()
	{
		if( this.output )
		{
			PartP2PRedstone in = this.getInput();
			if( in != null )
			{
				this.putInput( in.power );
			}
		}
	}

	protected void putInput( Object o )
	{
		if( this.recursive )
		{
			return;
		}

		this.recursive = true;
		if( this.output && this.proxy.isActive() )
		{
			int newPower = (Integer) o;
			if( this.power != newPower )
			{
				this.power = newPower;
				this.notifyNeighbors();
			}
		}
		this.recursive = false;
	}

	public void notifyNeighbors()
	{
		World worldObj = this.tile.getWorldObj();

		int xCoord = this.tile.xCoord;
		int yCoord = this.tile.yCoord;
		int zCoord = this.tile.zCoord;

		Platform.notifyBlocksOfNeighbors( worldObj, xCoord, yCoord, zCoord );

		// and this cause sometimes it can go thought walls.
		Platform.notifyBlocksOfNeighbors( worldObj, xCoord - 1, yCoord, zCoord );
		Platform.notifyBlocksOfNeighbors( worldObj, xCoord, yCoord - 1, zCoord );
		Platform.notifyBlocksOfNeighbors( worldObj, xCoord, yCoord, zCoord - 1 );
		Platform.notifyBlocksOfNeighbors( worldObj, xCoord, yCoord, zCoord + 1 );
		Platform.notifyBlocksOfNeighbors( worldObj, xCoord, yCoord + 1, zCoord );
		Platform.notifyBlocksOfNeighbors( worldObj, xCoord + 1, yCoord, zCoord );
	}

	@MENetworkEventSubscribe
	public void changeStateB( MENetworkChannelsChanged bs )
	{
		this.setNetworkReady();
	}

	@MENetworkEventSubscribe
	public void changeStateC( MENetworkPowerStatusChange bs )
	{
		this.setNetworkReady();
	}

	@Override
	@SideOnly( Side.CLIENT )
	public IIcon getTypeTexture()
	{
		return Blocks.redstone_block.getBlockTextureFromSide( 0 );
	}

	@Override
	public void readFromNBT( NBTTagCompound tag )
	{
		super.readFromNBT( tag );
		this.power = tag.getInteger( "power" );
	}

	@Override
	public void writeToNBT( NBTTagCompound tag )
	{
		super.writeToNBT( tag );
		tag.setInteger( "power", this.power );
	}

	@Override
	public void onTunnelNetworkChange()
	{
		this.setNetworkReady();
	}

	public float getPowerDrainPerTick()
	{
		return 0.5f;
	}

	@Override
	public void onNeighborChanged()
	{
		if( !this.output )
		{
			int x = this.tile.xCoord + this.side.offsetX;
			int y = this.tile.yCoord + this.side.offsetY;
			int z = this.tile.zCoord + this.side.offsetZ;

			Block b = this.tile.getWorldObj().getBlock( x, y, z );
			if( b != null && !this.output )
			{
				int srcSide = this.side.ordinal();
				if( b instanceof BlockRedstoneWire )
				{
					srcSide = 1;
				}
				this.power = b.isProvidingStrongPower( this.tile.getWorldObj(), x, y, z, srcSide );
				this.power = Math.max( this.power, b.isProvidingWeakPower( this.tile.getWorldObj(), x, y, z, srcSide ) );
				this.sendToOutput( this.power );
			}
			else
			{
				this.sendToOutput( 0 );
			}
		}
	}

	@Override
	public boolean canConnectRedstone()
	{
		return true;
	}

	@Override
	public int isProvidingStrongPower()
	{
		return this.output ? this.power : 0;
	}

	@Override
	public int isProvidingWeakPower()
	{
		return this.output ? this.power : 0;
	}

	private void sendToOutput( int power )
	{
		try
		{
			for( PartP2PRedstone rs : this.getOutputs() )
			{
				rs.putInput( power );
			}
		}
		catch( GridAccessException e )
		{
			// :P
		}
	}
}
