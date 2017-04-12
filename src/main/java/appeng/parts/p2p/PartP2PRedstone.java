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


import appeng.api.networking.events.MENetworkBootingStatusChange;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.me.GridAccessException;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;


public class PartP2PRedstone extends PartP2PTunnel<PartP2PRedstone>
{

	private int power;
	private boolean recursive = false;

	public PartP2PRedstone( final ItemStack is )
	{
		super( is );
	}

	@MENetworkEventSubscribe
	public void changeStateA( final MENetworkBootingStatusChange bs )
	{
		this.setNetworkReady();
	}

	private void setNetworkReady()
	{
		if( this.isOutput() )
		{
			final PartP2PRedstone in = this.getInput();
			if( in != null )
			{
				this.putInput( in.power );
			}
		}
	}

	private void putInput( final Object o )
	{
		if( this.recursive )
		{
			return;
		}

		this.recursive = true;
		if( this.isOutput() && this.getProxy().isActive() )
		{
			final int newPower = (Integer) o;
			if( this.power != newPower )
			{
				this.power = newPower;
				this.notifyNeighbors();
			}
		}
		this.recursive = false;
	}

	private void notifyNeighbors()
	{
		final World worldObj = this.getTile().getWorldObj();

		final int xCoord = this.getTile().xCoord;
		final int yCoord = this.getTile().yCoord;
		final int zCoord = this.getTile().zCoord;

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
	public void changeStateB( final MENetworkChannelsChanged bs )
	{
		this.setNetworkReady();
	}

	@MENetworkEventSubscribe
	public void changeStateC( final MENetworkPowerStatusChange bs )
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
	public void readFromNBT( final NBTTagCompound tag )
	{
		super.readFromNBT( tag );
		this.power = tag.getInteger( "power" );
	}

	@Override
	public void writeToNBT( final NBTTagCompound tag )
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
		if( !this.isOutput() )
		{
			final int x = this.getTile().xCoord + this.getSide().offsetX;
			final int y = this.getTile().yCoord + this.getSide().offsetY;
			final int z = this.getTile().zCoord + this.getSide().offsetZ;

			final Block b = this.getTile().getWorldObj().getBlock( x, y, z );
			if( b != null && !this.isOutput() )
			{
				int srcSide = this.getSide().ordinal();
				if( b instanceof BlockRedstoneWire )
				{
					srcSide = 1;
				}
				this.power = b.isProvidingStrongPower( this.getTile().getWorldObj(), x, y, z, srcSide );
				this.power = Math.max( this.power, b.isProvidingWeakPower( this.getTile().getWorldObj(), x, y, z, srcSide ) );
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
		return this.isOutput() ? this.power : 0;
	}

	@Override
	public int isProvidingWeakPower()
	{
		return this.isOutput() ? this.power : 0;
	}

	private void sendToOutput( final int power )
	{
		try
		{
			for( final PartP2PRedstone rs : this.getOutputs() )
			{
				rs.putInput( power );
			}
		}
		catch( final GridAccessException e )
		{
			// :P
		}
	}
}
